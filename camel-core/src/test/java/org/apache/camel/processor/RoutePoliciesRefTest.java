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
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.RoutePolicySupport;

/**
 * @version 
 */
public class RoutePoliciesRefTest extends ContextTestSupport {

    private final MyCustomRoutePolicy policyA = new MyCustomRoutePolicy("A");
    private final MyCustomRoutePolicy policyB = new MyCustomRoutePolicy("B");

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("policy-a", policyA);
        jndi.bind("policy-b", policyB);
        return jndi;
    }

    private static final class MyCustomRoutePolicy extends RoutePolicySupport {

        private final String name;

        private MyCustomRoutePolicy(String name) {
            this.name = name;
        }

        @Override
        public void onExchangeBegin(Route route, Exchange exchange) {
            exchange.getIn().setHeader(name, name);
        }

    }

    public void testCustomPolicies() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("A", "A");
        mock.expectedHeaderReceived("B", "B");

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .routePolicyRef("policy-a, policy-b")
                    .to("mock:result");
            }
        };
    }
}

