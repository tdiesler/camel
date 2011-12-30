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
package org.apache.camel.language.simple;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Test;

/**
 *  
 */
public class SimpleWithPropertiesTest {

    /**
     * A property from the property component in a expression 
     * should be kept as is to be processed later
     * See https://issues.apache.org/jira/browse/CAMEL-4843
     * 
     * The property could also be parsed correctly by the simple language but it should not throw an exception
     * 
     * @throws Exception
     */
    @Test
    public void testNullValue() throws Exception {
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        String result = SimpleBuilder.simple("{{test}}").evaluate(exchange, String.class);
        Assert.assertEquals("{{test}}", result);
    }

}