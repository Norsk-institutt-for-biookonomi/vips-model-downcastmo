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

import no.nibio.vips.util.DateMap;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DataMatrix extends DateMap{
    public final static String SPORULATION_VALUE = "SPORULATION_VALUE";
    public final static String INFECTION_DIRECTLY_AFTER_SPORULATION = "IDAS";
    public final static String INFECTION_TIME_AFTER_SPORULATION = "ITAS";
}
