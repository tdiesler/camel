/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.converter;


import java.util.Date;
import java.util.TimeZone;

import org.apache.camel.ContextTestSupport;

/**
 *
 */
public class DateTimeConverterTest extends ContextTestSupport {

    public void testToTimeZone() throws Exception {
        String id = TimeZone.getDefault().getID();

        TimeZone zone = context.getTypeConverter().convertTo(TimeZone.class, id);
        assertNotNull(zone);
        assertEquals(id, zone.getID());
    }
    
    public void testLongToDate() {
        long value = 0;
        Date date = context.getTypeConverter().convertTo(Date.class, value);
        Date expected = new Date(value);
        assertEquals(expected, date);
    }
    
    public void testDateToLong() {
        Date date = new Date(0);
        long l = context.getTypeConverter().convertTo(Long.class, date);
        assertEquals(date.getTime(), l);
    }
}
