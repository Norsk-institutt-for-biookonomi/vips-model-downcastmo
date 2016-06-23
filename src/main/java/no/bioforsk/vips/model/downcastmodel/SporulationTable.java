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

package no.bioforsk.vips.model.downcastmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SporulationTable {

    private final Map<Integer,Integer[]> sporulationTable;
    Integer[] a22 = {null,null,null,0,1,1,1,1,2,2,2,2,3,3,3,3,3,3,2,2,2,2,2,2,1,1,1,0};
    Integer[] a23 = {null,null,null,0,0,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,0,0};
    Integer[] a24 = {null,null,null,0,0,0,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,1,1,1,1,0,0,0};
    Integer[] a01 = {null,null,null,0,0,0,0,0,0,1,1,1,2,2,2,2,2,2,2,2,1,1,1,0,0,0,0,0};
    Integer[] a02 = {null,null,null,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0};
    Integer[] a03 = {null,null,null,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0};
    Integer[] a04 = {null,null,null,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    
    public SporulationTable()
    {
        sporulationTable = new HashMap();
        sporulationTable.put(22,a22);
        sporulationTable.put(23,a23);
        sporulationTable.put(0,a24);
        sporulationTable.put(1,a01);
        sporulationTable.put(2,a02);
        sporulationTable.put(3,a03);
        sporulationTable.put(4,a04);
    }
    
    public Integer getSporulationValue(Integer hourOfDay, Double averageTemperature)
    {
        try
        {
            return this.sporulationTable.get(hourOfDay)[new Double(Math.floor(averageTemperature)).intValue()];
        }
        catch(NullPointerException ex)
        {
            return 0;
        }
    }
}
