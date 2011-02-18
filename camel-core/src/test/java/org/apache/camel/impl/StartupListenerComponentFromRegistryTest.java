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
package org.apache.camel.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.StartupListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectComponent;

/**
 * @version 
 */
public class StartupListenerComponentFromRegistryTest extends ContextTestSupport {

    private MyComponent my;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        my = new MyComponent();

        JndiRegistry jndi = super.createRegistry();
        jndi.bind("my", my);
        return jndi;
    }

    public void testStartupListenerComponent() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBody("direct:foo", "Hello World");

        assertMockEndpointsSatisfied();

        assertEquals(1, my.getInvoked());
    }

    private class MyComponent extends DirectComponent implements StartupListener {

        private int invoked;

        public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
            invoked++;

            // the routes should have been started
            assertTrue(context.getRouteStatus("foo").isStarted());
            assertTrue(context.getRouteStatus("bar").isStarted());
        }

        public int getInvoked() {
            return invoked;
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo").routeId("foo").to("my:bar");
                from("my:bar").routeId("bar").to("mock:result");
            }
        };
    }
}