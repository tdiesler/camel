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
package org.apache.camel.processor;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;

/**
 * Testing http://camel.apache.org/dsl.html
 */
public class InterceptorSimplePredicateWithStopTest extends ContextTestSupport {

    public void testIntercept() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        template.sendBodyAndHeader("direct:start", "Hello World", "usertype", "test");
        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                // START SNIPPET: e1
                intercept()
                    .when(header("usertype").isEqualTo("test"))
                    // here we use stop() to tell Camel to NOT continue routing the message.
                    // this let us act as a filter, to drop certain messages.
                    .stop();

                // and here is our route
                from("direct:start").to("seda:bar").to("mock:result");
                // END SNIPPET: e1
            }
        };
    }

}