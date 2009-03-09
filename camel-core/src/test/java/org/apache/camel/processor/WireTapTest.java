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
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Wire tap unit test
 *
 * @version $Revision$
 */
public class WireTapTest extends ContextTestSupport {
    protected MockEndpoint tap;
    protected MockEndpoint result;

    public void testSend() throws Exception {
        // hello must come first, as we have delay on the tapped route
        result.expectedBodiesReceived("Hello World", "Tapped");
        tap.expectedBodiesReceived("Tapped");

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tap = getMockEndpoint("mock:tap");
        result = getMockEndpoint("mock:result");
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // START SNIPPET: e1
                from("direct:start")
                    .to("log:foo")
                    .wireTap("direct:tap")
                    .to("mock:result");
                // END SNIPPET: e1

                from("direct:tap")
                    .delay(100).setBody().constant("Tapped")
                    .to("mock:result", "mock:tap");
            }
        };
    }
}