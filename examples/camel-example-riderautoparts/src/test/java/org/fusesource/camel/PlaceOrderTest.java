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
package org.fusesource.camel;

import java.io.File;

import org.apache.activemq.util.IOHelper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PlaceOrderTest extends CamelSpringTestSupport {

    @Test
    public void testPlacingOrders() throws Exception {
        // Try placing two file orders
        MockEndpoint result = getMockEndpoint("mock:orders");
        result.expectedMessageCount(1 + 3); // 1 xml and 3 csv
        
        // clear out any old orders
        deleteDirectory("target/placeorder");
        createDirectory("target/placeorder");

        // place 2 file based orders
        IOHelper.copyFile(new File("src/data/message1.xml"), new File("target/placeorder/message1.xml"));
        IOHelper.copyFile(new File("src/data/message2.csv"), new File("target/placeorder/message2.csv"));
        
        result.assertIsSatisfied();

        // Now try placing an order via HTTP       
        result.reset();
        result.expectedMessageCount(1);
        result.expectedBodiesReceived(new Order("gearbox", 5));
        
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<orders><order name=\"gearbox\" amount=\"5\"/></orders>";
        String response = template.requestBody("http://localhost:8888/placeorder", body, String.class);
        assertEquals("OK", response);
        
        // ensure that the order got through to the mock endpoint
        result.assertIsSatisfied();
    }       

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        // add additional routes
        return new OrderRouterExtension();
    }
    
    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
    }
}

