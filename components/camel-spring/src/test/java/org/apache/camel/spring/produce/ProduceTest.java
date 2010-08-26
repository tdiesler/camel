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
package org.apache.camel.spring.produce;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Produce;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * @version $Revision$
 */
@ContextConfiguration
public class ProduceTest extends AbstractJUnit38SpringContextTests {
    
    @Produce(uri = "direct:start")
    protected MyListener producer;

    public void testInvokeService() throws Exception {
        // lets send a message
        String actual = producer.sayHello("James");
        assertEquals("response", "Hello James", actual);
    }
    
    public void testInvokeServiceWithMessageHeader() throws Exception {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("greeter", "Nihao ");
        String response = producer.greet(headers, "Willem");
        assertEquals("response is wrong", "Nihao Willem", response);
    }
}