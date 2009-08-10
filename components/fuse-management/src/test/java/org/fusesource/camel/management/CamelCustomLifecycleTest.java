/*
 * © 2001-2009, Progress Software Corporation and/or its subsidiaries or affiliates.  All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fusesource.camel.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Unit test using the exiting lifecycle API to register our own custom callback
 * that is the hook we need to use to plugin a FUSE adapter.
 *
 * @version $Revision$
 */
public class CamelCustomLifecycleTest extends CamelTestSupport {

    private static Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        DefaultCamelContext context = new DefaultCamelContext(createRegistry());

        context.setLifecycleStrategy(new LifecycleStrategy() {
            public void onContextStart(CamelContext camelContext) {
            }

            public void onEndpointAdd(Endpoint endpoint) {
                endpoints.put(endpoint.getEndpointUri(), endpoint);
            }

            public void onServiceAdd(CamelContext camelContext, Service service) {
            }

            public void onRoutesAdd(Collection<Route> routes) {
            }

            public void onRouteContextCreate(RouteContext routeContext) {
            }
        });

        return context;
    }

    @Test
    public void testCustomLifecycle() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        assertEquals(3, endpoints.size());
        assertTrue(endpoints.containsKey("direct://start"));
        assertTrue(endpoints.containsKey("log://foo"));
        assertTrue(endpoints.containsKey("mock://result"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("log:foo").to("mock:result");
            }
        };
    }

}
