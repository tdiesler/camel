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
package org.apache.camel.issues;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Testing http://camel.apache.org/dsl.html
 */
public class InterceptFromLogTest extends ContextTestSupport {

    public void testInterceptor() throws Exception {
        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello World");

        template.sendBody("seda:foo", "Hello World");

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                interceptFrom().to("log:foo").to("mock:foo");
                interceptFrom().to("log:bar").to("mock:bar");

                from("seda:foo").to("seda:bar");
                from("seda:bar").to("mock:result");
            }
        };
    }

}
