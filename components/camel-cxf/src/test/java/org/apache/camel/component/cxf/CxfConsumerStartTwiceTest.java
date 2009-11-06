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
package org.apache.camel.component.cxf;

import javax.xml.ws.Endpoint;

import junit.framework.TestCase;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.hello_world_soap_http.GreeterImpl;


public class CxfConsumerStartTwiceTest extends TestCase {
    
   
    public void testStartServiceTwice() throws Exception {
        CamelContext context = new DefaultCamelContext();
        
        //add the same route twice...
        context.addRoutes(new RouteBuilder() {
            public void configure() {

                from("cxf:http://localhost:7070/test?serviceClass=org.apache.camel.component.cxf.HelloService")
                    .to("log:POJO");
            }
        });

        context.addRoutes(new RouteBuilder() {
            public void configure() {

                from("cxf:http://localhost:7070/test?serviceClass=org.apache.camel.component.cxf.HelloService")
                    .to("log:POJO");
            }
        });

        try {
            context.start();
            fail("Expect an exception here");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("Expect the exception message has a Soap errror", ex.getMessage().equals("Soap 1.1 endpoint already registered on address http://localhost:7070/test"));
            context.stop();
        }
        
        
    }

}
