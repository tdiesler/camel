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

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.BeanComponent;
import org.apache.camel.component.direct.DirectComponent;
import org.apache.camel.component.log.LogComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.CamelContextHelper;

/**
 * @version $Revision$
 */
public class DefaultCamelContextTest extends TestCase {

    public void testAutoCreateComponentsOn() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        Component component = ctx.getComponent("bean");
        assertNotNull(component);
        assertEquals(component.getClass(), BeanComponent.class);
    }

    public void testAutoCreateComponentsOff() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.setAutoCreateComponents(false);
        Component component = ctx.getComponent("bean");
        assertNull(component);
    }

    public void testGetComponents() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        Component component = ctx.getComponent("bean");
        assertNotNull(component);

        List<String> list = ctx.getComponentNames();
        assertEquals(1, list.size());
        assertEquals("bean", list.get(0));
    }

    public void testGetEndpoint() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        Endpoint endpoint = ctx.getEndpoint("log:foo");
        assertNotNull(endpoint);

        try {
            ctx.getEndpoint(null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testGetEndpointNotFound() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        try {
            ctx.getEndpoint("xxx:foo");
            fail("Should have thrown a ResolveEndpointFailedException");
        } catch (ResolveEndpointFailedException e) {
            assertTrue(e.getMessage().contains("No component found with scheme: xxx"));
        }
    }

    public void testGetEndpointNoScheme() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        try {
            CamelContextHelper.getMandatoryEndpoint(ctx, "log.foo");
            fail("Should have thrown a NoSuchEndpointException");
        } catch (NoSuchEndpointException e) {
            // expected
        }
    }

    public void testRestartCamelContext() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:endpointA").to("mock:endpointB");
            }
        });
        ctx.start();
        assertEquals("Should have one RouteService", 1, ctx.getRouteServices().size());
        String routesString = ctx.getRoutes().toString();
        ctx.stop();
        assertEquals("The RouteService should NOT be removed even when we stop", 1, ctx.getRouteServices().size());
        ctx.start();
        assertEquals("Should have one RouteService", 1, ctx.getRouteServices().size());
        assertEquals("The Routes should be same", routesString, ctx.getRoutes().toString());
        ctx.stop();
        assertEquals("The RouteService should NOT be removed even when we stop", 1, ctx.getRouteServices().size());
    }

    public void testName() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        assertNotNull("Should have a default name", ctx.getName());
        ctx.setName("foo");
        assertEquals("foo", ctx.getName());

        assertNotNull(ctx.toString());
        assertTrue(ctx.isAutoStartup());
    }

    public void testVersion() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        assertNotNull("Should have a version", ctx.getVersion());
    }

    public void testHasComponent() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        assertNull(ctx.hasComponent("log"));

        ctx.addComponent("log", new LogComponent());
        assertNotNull(ctx.hasComponent("log"));
    }

    public void testGetComponent() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addComponent("log", new LogComponent());

        LogComponent log = ctx.getComponent("log", LogComponent.class);
        assertNotNull(log);
        try {
            ctx.addComponent("direct", new DirectComponent());
            ctx.getComponent("log", DirectComponent.class);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testGetEndpointMap() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addEndpoint("mock://foo", new MockEndpoint("mock://foo"));

        Map map = ctx.getEndpointMap();
        assertEquals(1, map.size());
    }

    public void testHasEndpoint() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addEndpoint("mock://foo", new MockEndpoint("mock://foo"));

        assertNotNull(ctx.hasEndpoint("mock://foo"));
        assertNull(ctx.hasEndpoint("mock://bar"));

        try {
            Endpoint endpoint = ctx.hasEndpoint(null);
            assertNull("Should not have endpoint", endpoint);
        } catch (ResolveEndpointFailedException e) {
            // expected
        }
    }

}
