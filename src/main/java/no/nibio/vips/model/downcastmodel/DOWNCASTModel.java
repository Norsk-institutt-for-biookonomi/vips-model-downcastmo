/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of DOWNCASTModel.
 * DOWNCASTModel is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * DOWNCASTModel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with DOWNCASTModel.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.model.downcastmodel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Calendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.ResultImpl;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.model.Model;
import no.nibio.vips.model.ModelExcecutionException;
import no.nibio.vips.model.ModelId;
import no.nibio.vips.util.CollectionsUtil;
import no.nibio.vips.util.FixedQueue;
import no.nibio.vips.util.ModelUtil;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;

/**
 * Based on the Downcast model by de Visser (1998)
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DOWNCASTModel extends I18nImpl implements Model {
    private final static ModelId MODEL_ID = new ModelId("DOWNCASTMO");
    
    private final ModelUtil modelUtil;
    private final WeatherUtil weatherUtil;
    private DataMatrix dataMatrix;
    private TimeZone timeZone;
    private SporulationTable sporulationTable;
    
    public DOWNCASTModel(){
        super("no.nibio.vips.model.downcastmodel.texts");
        this.modelUtil = new ModelUtil();
        this.weatherUtil = new WeatherUtil();
        this.sporulationTable = new SporulationTable();
    }

    @Override
    public List<Result> getResult() throws ModelExcecutionException {
        this.calculateResults();
        //System.out.println(this.dataMatrix.toCSV());
        Date currentDate = this.dataMatrix.getFirstDateWithParameterValue(DataMatrix.SPORULATION_VALUE);
        Date lastDate = this.dataMatrix.getLastDateWithParameterValue(DataMatrix.SPORULATION_VALUE);
        
        Calendar cal = Calendar.getInstance(timeZone);
        
        List<Result> retVal = new ArrayList<>();
        
        while(currentDate.compareTo(lastDate) <= 0)
        {
            Result result = new ResultImpl();
            result.setValidTimeStart(currentDate);
            
            Integer sporulationValue = this.dataMatrix.getParamIntValueForDate(currentDate, DataMatrix.SPORULATION_VALUE);
            Integer infectionDirectlyAfterSporulation = this.dataMatrix.getParamIntValueForDate(currentDate, DataMatrix.INFECTION_DIRECTLY_AFTER_SPORULATION);
            Date infectionTimeAfterSporulation = this.dataMatrix.getParamDateValueForDate(currentDate, DataMatrix.INFECTION_TIME_AFTER_SPORULATION);
            Boolean isInfectionPredictedToday = isInfectionPredictedToday(currentDate);
            
            result.setValue(DOWNCASTModel.MODEL_ID.toString(), DataMatrix.SPORULATION_VALUE, 
                    String.valueOf(sporulationValue));
            result.setValue(DOWNCASTModel.MODEL_ID.toString(), DataMatrix.INFECTION_DIRECTLY_AFTER_SPORULATION, 
                    String.valueOf(infectionDirectlyAfterSporulation));
            result.setValue(DOWNCASTModel.MODEL_ID.toString(), DataMatrix.INFECTION_TIME_AFTER_SPORULATION, 
                    infectionTimeAfterSporulation != null ? String.valueOf(infectionTimeAfterSporulation) : "");
            result.setValue(DOWNCASTModel.MODEL_ID.toString(), DataMatrix.INFECTION_LATER_AFTER_SPORULATION, 
                    isInfectionPredictedToday ? "1" : "0");
            
            Integer warningStatus = Result.WARNING_STATUS_NO_RISK;
            if(infectionDirectlyAfterSporulation == 1
                    || isInfectionPredictedToday // Checks today and three days back for prediction
                    )
            {
                warningStatus = Result.WARNING_STATUS_HIGH_RISK;
            }
            else if(sporulationValue > 0)
            {
                warningStatus = Result.WARNING_STATUS_MINOR_RISK;
            }
            result.setWarningStatus(warningStatus);
            retVal.add(result);
            
            cal.setTime(currentDate);
            cal.add(Calendar.DATE,1);
            currentDate = cal.getTime();
        }
        return retVal;
    }

    @Override
    public ModelId getModelId() {
        return DOWNCASTModel.MODEL_ID;
    }

    @Override
    public String getModelName() {
        return this.getModelName(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelName(String language) {
        return this.getText("name", language);
    }

    @Override
    public String getLicense() {
        return "\n" +
            "Copyright (c) 2016 NIBIO <http://www.nibio.no/>. \n" +
            "\n" +
            "This file is part of DOWNCASTModel.\n" +
            "DOWNCASTModel is free software: you can redistribute it and/or modify\n" +
            "it under the terms of the NIBIO Open Source License as published by \n" +
            "NIBIO, either version 1 of the License, or (at your option) any\n" +
            "later version.\n" +
            "\n" +
            "DOWNCASTModel is distributed in the hope that it will be useful,\n" +
            "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
            "NIBIO Open Source License for more details.\n" +
            "\n" +
            "You should have received a copy of the NIBIO Open Source License\n" +
            "along with DOWNCASTModel.  If not, see <http://www.nibio.no/licenses/>.";
    }

    @Override
    public String getCopyright() {
        return "(c) 2016 NIBIO (http://www.nibio.no/). Contact: post@nibio.no";
    }

    @Override
    public String getModelDescription() {
        return this.getModelDescription(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelDescription(String language) {
        try
        {
            return this.modelUtil.getTextWithBase64EncodedImages(this.getText("description", language), this.getClass());
        }
        catch(IOException ex)
        {
            return this.getText("description", language);
        }
    }

    @Override
    public String getWarningStatusInterpretation() {
        return this.getWarningStatusInterpretation(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getWarningStatusInterpretation(String language) {
        return this.getText("statusInterpretation", language);
    }

    @Override
    public String getModelUsage() {
        return this.getModelUsage(Model.DEFAULT_LANGUAGE);
    }

    @Override
    public String getModelUsage(String language) {
        return this.getText("usage", language);
    }

    @Override
    public String getSampleConfig() {
        return  "{\n" +
                "\t\"loginInfo\":{\n" +
                "\t\t\"username\":\"example\",\n" +
                "\t\t\"password\":\"example\"\n" +
                "\t},\n" +
                "\t\"modelId\":\"" + MODEL_ID.toString() + "\",\n" +
                "\t\"configParameters\":{\n" +
                "\t\t\"observations\":[\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2012-08-20T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"TM\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":13.77\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2012-08-20T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"BTg\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":48\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2012-08-20T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"RR\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":3.6\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\t\"timeMeasured\": \"2012-08-20T00:00:00+02:00\",\n" +
                "\t\t\t\t\"elementMeasurementTypeId\":\"UM\",\n" +
                "\t\t\t\t\"logIntervalId\":1,\n" +
                "\t\t\t\t\"value\":60\n" +
                "\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}\n";
    }

    @Override
    public void setConfiguration(ModelConfiguration config) throws ConfigValidationException {
        this.dataMatrix = new DataMatrix();
        ObjectMapper objectMapper = new ObjectMapper();
        this.timeZone = TimeZone.getTimeZone((String) config.getConfigParameter("timeZone"));
        // Getting weather data
        List<WeatherObservation> observations = objectMapper.convertValue(config.getConfigParameter("observations"), new TypeReference<List<WeatherObservation>>(){});
        for(WeatherObservation o:observations)
        {
            String paramCode = o.getElementMeasurementTypeId();
            // If we get BT, convert to BTg
            if(paramCode.equals("BT"))
            {
                paramCode = "BTg";
            }
            this.dataMatrix.setParamDoubleValueForDate(o.getTimeMeasured(), paramCode, o.getValue());   
        }
        //System.out.println(this.dataMatrix.toCSV());
    }

    private void calculateResults()
    {
        // We start calculating two days after first weather data
        Calendar cal = Calendar.getInstance(this.timeZone);
        cal.setTime(this.dataMatrix.getFirstDateWithParameterValue(WeatherElements.TEMPERATURE_MEAN));
        cal.add(Calendar.DATE, 2);
        Date currentDate = weatherUtil.normalizeToExactDate(cal.getTime(), timeZone);
        cal.setTime(this.dataMatrix.getLastDateWithParameterValue(WeatherElements.TEMPERATURE_MEAN));
        cal.add(Calendar.DATE, -1);
        Date endDate = cal.getTime();
        while(currentDate.compareTo(endDate) <= 0)
        {
            Integer sporulationValue = this.calculateSporulationValue(currentDate);
            this.dataMatrix.setParamIntValueForDate(currentDate, DataMatrix.SPORULATION_VALUE, sporulationValue);
            Boolean infectionDirectlyAfterSporulation = false;
            Date infectionTimeAfterSporulation = null;
            
            if(sporulationValue > 0)
            {
                infectionDirectlyAfterSporulation = this.isInfectionDirectlyAfterSporulation(currentDate);
                if(! infectionDirectlyAfterSporulation)
                {
                    infectionTimeAfterSporulation = getInfectionTimeAfterSporulation(currentDate);
                }
            }
            
            this.dataMatrix.setParamIntValueForDate(currentDate, DataMatrix.INFECTION_DIRECTLY_AFTER_SPORULATION, 
                    infectionDirectlyAfterSporulation ? 1 : 0);
            if(infectionTimeAfterSporulation != null)
            {
                this.dataMatrix.setParamDateValueForDate(currentDate, DataMatrix.INFECTION_TIME_AFTER_SPORULATION, infectionTimeAfterSporulation);
            }
            
            // Move forward
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, 1);
            currentDate = cal.getTime();
        }
    }
    
    private Integer calculateSporulationValue(Date aDay)
    {
        aDay = this.weatherUtil.normalizeToExactDate(aDay, timeZone);
        Calendar cal = Calendar.getInstance(this.timeZone);
        cal.setTime(aDay);
        //boolean DEBUG = (cal.get(Calendar.MONTH) == Calendar.JULY && cal.get(Calendar.DATE) == 15);
        boolean DEBUG = false;
        cal.add(Calendar.DATE, -1);
        Date aDayBefore = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 22);
        Date relHumTimeLimit = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 8);
        cal.add(Calendar.MINUTE, -1); 
        Date sporulationTimeLimit = cal.getTime(); // Setting time to aDay@05:59
        
        if(DEBUG)
        {
            System.out.println("relHumTimeLimit=" + relHumTimeLimit +", sporulationTimeLimit=" + sporulationTimeLimit);
        }
        
        Integer hoursAtHighRelHum = 0;
        Double temperatureSumAtHighRelHum = 0.0;
        Integer hoursOver27DegC = 0;
        Integer hoursOver28DegC = 0;
        Integer hoursOver29DegC = 0;
        
        Date currentHour = aDayBefore;
        Date firstHourOverRelHumThreshold = null;
        while(currentHour.before(sporulationTimeLimit))
        {
            if(DEBUG) System.out.println("currentHour=" + currentHour);
            // Checking temperature criterium
            Double TM = this.dataMatrix.getParamDoubleValueForDate(currentHour, WeatherElements.TEMPERATURE_MEAN);
            if(currentHour.before(aDay))
            {
                
                hoursOver27DegC += TM > 27 ? 1 : 0;
                hoursOver28DegC += TM > 28 ? 1 : 0;
                hoursOver29DegC += TM > 29 ? 1 : 0;
                // Sporulation has occured
                if(hoursOver29DegC >= 2 || hoursOver28DegC >= 4 || hoursOver27DegC >= 8)
                {
                    if(DEBUG) System.out.println(currentHour + ": Returning due to hot hours " + hoursOver27DegC + ";" + hoursOver28DegC + ";" + hoursOver29DegC);
                    return 0;
                }
            }
            
            
            // Checking precipitation criterium
            if(currentHour.compareTo(aDay) >=0 && currentHour.before(sporulationTimeLimit ))
            {
                Double RR = this.dataMatrix.getParamDoubleValueForDate(currentHour, WeatherElements.PRECIPITATION);
                if(DEBUG) System.out.println("RR=" + RR);
                // Sporulation has occurred
                if(RR > 0.3)
                {
                    return 0;
                }
            }
            
            // Checking relative humidity criterium
            if(currentHour.compareTo(relHumTimeLimit) >= 0 && currentHour.compareTo(sporulationTimeLimit) <= 0)
            {
                Double UM = this.dataMatrix.getParamDoubleValueForDate(currentHour, WeatherElements.RELATIVE_HUMIDITY_MEAN);
                if(DEBUG) System.out.println("UM at " + currentHour + "=" + UM);
                if(UM >= 92)
                {
                    temperatureSumAtHighRelHum += TM;
                    hoursAtHighRelHum ++;
                    if(firstHourOverRelHumThreshold == null)
                    {
                        firstHourOverRelHumThreshold = currentHour;
                    }
                }
                else // Reset list (TODO: Is this correct???)
                {
                    temperatureSumAtHighRelHum = 0.0;
                    hoursAtHighRelHum = 0;
                }
            }
            
            cal.setTime(currentHour);
            cal.add(Calendar.HOUR_OF_DAY, 1);
            currentHour = cal.getTime();
        }
        
        if(hoursAtHighRelHum > 0)
        {
            cal.setTime(firstHourOverRelHumThreshold);
            Integer hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            if(DEBUG)
            {
                System.out.println("hourOfDay=" + hourOfDay +", temperatureSumAtHighRelHum=" + temperatureSumAtHighRelHum + ", hoursAtHighRelHum=" + hoursAtHighRelHum);
                System.out.println("Got here! Value = " + this.sporulationTable.getSporulationValue(hourOfDay, temperatureSumAtHighRelHum/hoursAtHighRelHum));
            }
            return this.sporulationTable.getSporulationValue(hourOfDay, temperatureSumAtHighRelHum/hoursAtHighRelHum);
        }
        else
        {
            return 0;
        }
    }

    private Boolean isInfectionDirectlyAfterSporulation(Date currentDate) {
        currentDate = this.weatherUtil.normalizeToExactDate(currentDate, timeZone);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentDate);
        cal.set(Calendar.HOUR_OF_DAY, 6);
        Date currentPossibleInfectionHour = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        Date period1Limit = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        Date period2Limit = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        Date period3Limit = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 11);
        Date period4Limit = cal.getTime();
        
        
        Double temperatureSum = 0.0;
        Integer counter = 0;
        Double averageTemperature = 0.0;
        // Between 06:00 and 12:00 the current date
        while(currentPossibleInfectionHour.compareTo(period4Limit) <= 0)
        {
            Double BTg = this.dataMatrix.getParamDoubleValueForDate(currentPossibleInfectionHour, WeatherElements.LEAF_WETNESS_GROUND_LEVEL);
            
            if(BTg == null)
            {
                System.out.println("currentPossibleInfectionHour = " + currentPossibleInfectionHour);
            }
            Double TM = this.dataMatrix.getParamDoubleValueForDate(currentPossibleInfectionHour, WeatherElements.TEMPERATURE_MEAN);
            
            temperatureSum += TM;
            counter++;
            averageTemperature = temperatureSum / counter;
            
            // 06:00-08:00
            if(currentPossibleInfectionHour.compareTo(period1Limit) <= 0)
            {
                if(BTg < 48)
                {
                    return false;
                }
                // 08:00
                if(currentPossibleInfectionHour.compareTo(period1Limit) == 0)
                {
                    if(averageTemperature >= 7 && averageTemperature < 15)
                    {
                        return true;
                    }
                }
            }
            // 09:00
            if(currentPossibleInfectionHour.compareTo(period2Limit) == 0)
            {
                if(BTg < 48)
                {
                    return false;
                }
                if((averageTemperature >= 6 && averageTemperature < 7) || (averageTemperature >= 16 && averageTemperature < 16))
                {
                    return true;
                }
            }
            // 10:00-11:00
            if(currentPossibleInfectionHour.compareTo(period2Limit) > 0 || currentPossibleInfectionHour.compareTo(period3Limit) == 0)
            {
                if(BTg < 48)
                {
                    return false;
                }
                if(currentPossibleInfectionHour.compareTo(period3Limit) == 0)
                {
                    if(averageTemperature >= 16 && averageTemperature < 20)
                    {
                        return true;
                    }
                }
            }
            // 12:00
            if(currentPossibleInfectionHour.compareTo(period4Limit) == 0)
            {
                if(BTg < 48)
                {
                    return false;
                }
                if(averageTemperature >= 20 && averageTemperature < 24)
                {
                    return true;
                }
            }
            
            // Moving forward
            cal.setTime(currentPossibleInfectionHour);
            cal.add(Calendar.HOUR_OF_DAY, 1);
            currentPossibleInfectionHour = cal.getTime();
        }
        // No criteria met within given time period, hence no infection
        return false;
    }

    /**
     * <em>Later infections</em> may occur up to 72 hours after sporulation (from 0500 the night with sporulation incident)
     * <ul>
     *      <li>Start calculation of number of minutes with leaf wetness at BTg >= 12 min</li>
     *      <li>Calculate SUM1 - total minutes with leaf weatness during 5 consecutive hours</li>
     *      <ul>
     *          <li>SUM1 &lt; 90 min =&gt; spores survive. Calculate infection at next period with leaf wetness</li>
     *          <li>90 &lt;= SUM1 &lt; 150 min =&gt; spores mature and die</li>
     *          <li>SUM1 &gt;= 150 min =&gt; spores survive and mature if SUM2 is reached during or immediately after the period for SUM1</li>
     *      </ul>
     *      <li>If SUM1 &gt;= 150 min, calculate SUM2</li>
     *      <li>SUM2 = total minuts with leaf wetness during 3 consecutive hours. Start calculation of SUM2 at BTg &gt;= 48 min
     *          <ul>
     *              <li>SUM2 &gt;= 144 min -&gt; <em>infection</em>!</li>
     *          </ul>
     *      </li>
     * </ul>
     * @param currentDate
     * @return The time of infection or <strong>null</strong> if no infection occurs on the given day
     */
    private Date getInfectionTimeAfterSporulation(Date currentDate) {
        currentDate = this.weatherUtil.normalizeToExactDate(currentDate, timeZone);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(currentDate);
        cal.set(Calendar.HOUR_OF_DAY, 6);
        Date firstHourAfterSporulation = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 71);
        Date lastPossibleInfectionTimeAfterSporulation = cal.getTime();
        
        FixedQueue<Double> last5LeafWetnessValues = null;
        FixedQueue<Double> last3LeafWetnessValues = null;
        
        Date currentHour = firstHourAfterSporulation;
        while(currentHour.compareTo(lastPossibleInfectionTimeAfterSporulation) <= 0)
        {
            Double currentBTg = this.dataMatrix.getParamDoubleValueForDate(currentHour, WeatherElements.LEAF_WETNESS_GROUND_LEVEL);
            if(currentBTg >= 12 || last5LeafWetnessValues != null && !last5LeafWetnessValues.isEmpty())
            {
                if(last5LeafWetnessValues == null)
                {
                    last5LeafWetnessValues = new FixedQueue<>(5);
                }
                last5LeafWetnessValues.add(currentBTg);
                if(last5LeafWetnessValues.size() == 5)
                {
                    Double sum1 = CollectionsUtil.getSum(last5LeafWetnessValues);
                    if(sum1 < 90) // Could be infection further ahead
                    {
                        // Trying with the two next hour values
                        last5LeafWetnessValues.poll();
                        last5LeafWetnessValues.poll();
                        while(last5LeafWetnessValues.size() > 0 && last5LeafWetnessValues.peek() < 12)
                        {
                            last5LeafWetnessValues.poll();
                        }
                    }
                    else if(sum1 <= 90 && sum1 < 150) // No infection
                    {
                        return null;
                    }
                    else // Infection!!
                    {
                        // Starting calculation of SUM2 at BTg >= 48
                        // Moving 5 hours back
                        cal.setTime(currentHour);
                        cal.add(Calendar.HOUR_OF_DAY, -5);
                        currentHour = cal.getTime();
                        for(int j = 0; j < 8; j++)
                        {
                            currentBTg = this.dataMatrix.getParamDoubleValueForDate(currentHour, WeatherElements.LEAF_WETNESS_GROUND_LEVEL);
                            if(currentBTg >= 48 || last3LeafWetnessValues != null)
                            {
                                if(last3LeafWetnessValues == null)
                                {
                                    last3LeafWetnessValues = new FixedQueue<>(3);
                                }
                                last3LeafWetnessValues.add(currentBTg);
                                if(last3LeafWetnessValues.size() == 3)
                                {
                                    Double sum2 = CollectionsUtil.getSum(last3LeafWetnessValues);
                                    return (sum2 >= 144) ? currentHour: null;
                                }
                            }
                            
                            cal.setTime(currentHour);
                            cal.add(Calendar.HOUR_OF_DAY, 1);
                            currentHour = cal.getTime();
                        }
                        // If past this, the spores are presumed dead
                        return null;
                    }
                }
            }
            cal.setTime(currentHour);
            cal.add(Calendar.HOUR_OF_DAY, 1);
            currentHour = cal.getTime();
        }
        return null;
    }

    /**
     * It could be that infection time has been predicted to be "currentDate", 
     * from the period of three days ago and until today. This method checks for
     * this
     * @param currentDate
     * @return 
     */
    private boolean isInfectionPredictedToday(Date currentDate) {
        Calendar cal = Calendar.getInstance(this.timeZone);
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -3);
        Date investigationDate = cal.getTime();
        while(investigationDate.compareTo(currentDate) <= 0)
        {
            Date infectionTimeAfterSporulation = this.dataMatrix.getParamDateValueForDate(investigationDate, DataMatrix.INFECTION_TIME_AFTER_SPORULATION);
            if(infectionTimeAfterSporulation != null 
                    && this.weatherUtil.normalizeToExactDate(infectionTimeAfterSporulation, timeZone).compareTo(currentDate) == 0
                    )
            {
                return true;
            }
            cal.setTime(investigationDate);
            cal.add(Calendar.DATE, 1);
            investigationDate = cal.getTime();
        }
        return false;
    }
}
