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

import org.apache.camel.ContextTestSupport;

/**
 * @version 
 */
public class ObjectConverterNaNTest extends ContextTestSupport {

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public void testDoubleToLongWithNaN() throws Exception {
        assertEquals(Long.valueOf("4"), context.getTypeConverter().convertTo(Long.class, Double.valueOf("4")));
        assertEquals(Long.valueOf("0"), context.getTypeConverter().convertTo(Long.class, Double.NaN));
        assertEquals(Long.valueOf("3"), context.getTypeConverter().convertTo(Long.class, Double.valueOf("3")));
    }

    public void testFloatToLongWithNaN() throws Exception {
        assertEquals(Long.valueOf("4"), context.getTypeConverter().convertTo(Long.class, Float.valueOf("4")));
        assertEquals(Long.valueOf("0"), context.getTypeConverter().convertTo(Long.class, Float.NaN));
        assertEquals(Long.valueOf("3"), context.getTypeConverter().convertTo(Long.class, Float.valueOf("3")));
    }

    public void testDoubleToIntegerWithNaN() throws Exception {
        assertEquals(Integer.valueOf("4"), context.getTypeConverter().convertTo(Integer.class, Double.valueOf("4")));
        assertEquals(Integer.valueOf("0"), context.getTypeConverter().convertTo(Integer.class, Double.NaN));
        assertEquals(Integer.valueOf("3"), context.getTypeConverter().convertTo(Integer.class, Double.valueOf("3")));
    }

    public void testFloatToIntegerWithNaN() throws Exception {
        assertEquals(Integer.valueOf("4"), context.getTypeConverter().convertTo(Integer.class, Float.valueOf("4")));
        assertEquals(Integer.valueOf("0"), context.getTypeConverter().convertTo(Integer.class, Float.NaN));
        assertEquals(Integer.valueOf("3"), context.getTypeConverter().convertTo(Integer.class, Float.valueOf("3")));
    }

    public void testDoubleToShortWithNaN() throws Exception {
        assertEquals(Short.valueOf("4"), context.getTypeConverter().convertTo(Short.class, Double.valueOf("4")));
        assertEquals(Short.valueOf("0"), context.getTypeConverter().convertTo(Short.class, Double.NaN));
        assertEquals(Short.valueOf("3"), context.getTypeConverter().convertTo(Short.class, Double.valueOf("3")));
    }

    public void testFloatToShortWithNaN() throws Exception {
        assertEquals(Short.valueOf("4"), context.getTypeConverter().convertTo(Short.class, Float.valueOf("4")));
        assertEquals(Short.valueOf("0"), context.getTypeConverter().convertTo(Short.class, Float.NaN));
        assertEquals(Short.valueOf("3"), context.getTypeConverter().convertTo(Short.class, Float.valueOf("3")));
    }

    public void testDoubleToByteWithNaN() throws Exception {
        assertEquals(Byte.valueOf("4"), context.getTypeConverter().convertTo(Byte.class, Double.valueOf("4")));
        assertEquals(Byte.valueOf("0"), context.getTypeConverter().convertTo(Byte.class, Double.NaN));
        assertEquals(Byte.valueOf("3"), context.getTypeConverter().convertTo(Byte.class, Double.valueOf("3")));
    }

    public void testFloatToByteWithNaN() throws Exception {
        assertEquals(Byte.valueOf("4"), context.getTypeConverter().convertTo(Byte.class, Float.valueOf("4")));
        assertEquals(Byte.valueOf("0"), context.getTypeConverter().convertTo(Byte.class, Float.NaN));
        assertEquals(Byte.valueOf("3"), context.getTypeConverter().convertTo(Byte.class, Float.valueOf("3")));
    }

}
