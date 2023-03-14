/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of DOWNCASTModel.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
package no.nibio.vips.model.downcastmodel;

import no.nibio.vips.model.downcastmodel.DOWNCASTModel;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static junit.framework.Assert.fail;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.model.ModelId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author treinar
 */
public class DOWNCASTModelTest {
    
    public DOWNCASTModelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getResult method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetResult() throws Exception {
        System.out.println("getResult");
        DOWNCASTModel instance = new DOWNCASTModel();
        ModelConfiguration config = getConfiguration("/lier_2015.json");
        instance.setConfiguration(config);
        List<Result> result = instance.getResult();
        assertNotNull(result);
        /*
        for(Result r:result)
        {
            System.out.println(r.toString());
        }*/
    }

    /**
     * Test of getModelId method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetModelId() {
        System.out.println("getModelId");
        DOWNCASTModel instance = new DOWNCASTModel();
        ModelId result = instance.getModelId();
        assertNotNull(result);
    }

    /**
     * Test of getModelName method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetModelName_0args() {
        System.out.println("getModelName");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getModelName();
        assertNotNull(result);
    }

   

    /**
     * Test of getLicense method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetLicense() {
        System.out.println("getLicense");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getLicense();
        assertNotNull(result);
    }

    /**
     * Test of getCopyright method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetCopyright() {
        System.out.println("getCopyright");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getCopyright();
        assertNotNull(result);
    }

    /**
     * Test of getModelDescription method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetModelDescription_0args() {
        System.out.println("getModelDescription");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getModelDescription();
        assertNotNull(result);
    }

    

    /**
     * Test of getWarningStatusInterpretation method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetWarningStatusInterpretation_0args() {
        System.out.println("getWarningStatusInterpretation");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getWarningStatusInterpretation();
        assertNotNull(result);
    }

    

    /**
     * Test of getModelUsage method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetModelUsage_0args() {
        System.out.println("getModelUsage");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getModelUsage();
        assertNotNull(result);
    }

    

    /**
     * Test of getSampleConfig method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testGetSampleConfig() {
        System.out.println("getSampleConfig");
        DOWNCASTModel instance = new DOWNCASTModel();
        String result = instance.getSampleConfig();
        assertNotNull(result);
    }

    /**
     * Test of setConfiguration method, of class DOWNCASTModel.
     */
    @org.junit.Test
    public void testSetConfiguration() throws Exception {
        System.out.println("setConfiguration");
        ModelConfiguration config = getConfiguration("/lier_2015.json");
        DOWNCASTModel instance = new DOWNCASTModel();
        instance.setConfiguration(config);
        
    }
    
    private ModelConfiguration getConfiguration(String fileName)
    {
        try {
            ModelConfiguration config = new ModelConfiguration();
            config.setModelId("BREMIALACT");
            config.setConfigParameter("timeZone", "Europe/Oslo");
            BufferedInputStream inputStream = new BufferedInputStream(this.getClass().getResourceAsStream(fileName));
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createParser(inputStream);
            JsonNode all = jp.readValueAsTree();
            List<WeatherObservation> observations = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            Date firstDate = null;
            Date lastDate = null;
            if(all.isArray())
            {
                for(JsonNode node : all){
                    Date timeMeasured = (Date)mapper.convertValue(node.get("timeMeasured").asText(), new TypeReference<Date>(){});
                    if(firstDate == null || firstDate.compareTo(timeMeasured) > 0)
                    {
                        firstDate = timeMeasured;
                    }
                    if(lastDate == null || lastDate.compareTo(timeMeasured) < 0)
                    {
                        lastDate = timeMeasured;
                    }
                    //System.out.println(node.toString());
                    WeatherObservation observation = new WeatherObservation();
                    observation.setTimeMeasured(timeMeasured);
                    observation.setLogIntervalId(node.get("logIntervalId").asInt());
                    observation.setElementMeasurementTypeId(node.get("elementMeasurementTypeId").asText());
                    observation.setValue(node.get("value").asDouble());
                    observations.add(observation);
                }

            }
            else
            {
                fail("Data input from file is not a JSON array");
            }
            config.setConfigParameter("observations", observations);

            
            return config;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } 
    }
}
