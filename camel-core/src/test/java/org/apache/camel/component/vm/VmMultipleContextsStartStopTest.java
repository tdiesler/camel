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
package org.apache.camel.component.vm;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * @version 
 */
public class VmMultipleContextsStartStopTest extends ContextTestSupport {

    public void testStartStop() throws Exception {
        DefaultCamelContext c1 = new DefaultCamelContext();
        c1.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test")
                    .to("vm:foo");
            }
        });
        c1.start();
        ProducerTemplate template = c1.createProducerTemplate();

        DefaultCamelContext c2 = new DefaultCamelContext();
        c2.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("vm:foo")
                    .to("mock:result");
            }
        });
        c2.start();
        
        /* Check that contexts are communicated */
        MockEndpoint mock = c2.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);
        template.requestBody("direct:test", "Hello world!");
        mock.assertIsSatisfied();
        mock.reset();
        
        /* Restart the consumer Camel Context */
        c2.stop();
        c2.start();
        
        /* Send a message again and assert that it's received */
        template.requestBody("direct:test", "Hello world!");
        mock.assertIsSatisfied();
        
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

            }
        };
    }
}