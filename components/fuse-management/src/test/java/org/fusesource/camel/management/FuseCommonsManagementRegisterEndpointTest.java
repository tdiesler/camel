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

import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.management.CommonManagementLifecycleStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * To test that we can register endpoints using fuse management from Camel
 *
 * @version $Revision$
 */
public class FuseCommonsManagementRegisterEndpointTest extends CamelTestSupport {

    private CommonManagementLifecycleStrategy lifecycle = new CommonManagementLifecycleStrategy();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        DefaultCamelContext context = new DefaultCamelContext(createRegistry());
        context.setLifecycleStrategy(lifecycle);
        return context;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryEndpoints() throws Exception {
        MBeanServer mbeanServer = lifecycle.getStrategy().getMbeanServer();

        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=endpoints,*"), null);
        assertEquals(3, set.size());

        Set<String> uris = new HashSet<String>();
        for (ObjectName name : set) {
            String uri = (String) mbeanServer.getAttribute(name, "Uri");
            uris.add(uri);
        }

        assertTrue(uris.contains("direct://start"));
        assertTrue(uris.contains("log://foo"));
        assertTrue(uris.contains("mock://result"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLookupEndpointsByName() throws Exception {
        MBeanServer mbeanServer = lifecycle.getStrategy().getMbeanServer();

        ObjectName name = ObjectName.getInstance("org.apache.camel:context=camel/camel-1,type=endpoints,name=\"direct://start\"");
        String uri = (String) mbeanServer.getAttribute(name, "Uri");
        assertEquals("direct://start", uri);

        name = ObjectName.getInstance("org.apache.camel:context=camel/camel-1,type=endpoints,name=\"log://foo\"");
        uri = (String) mbeanServer.getAttribute(name, "Uri");
        assertEquals("log://foo", uri);

        name = ObjectName.getInstance("org.apache.camel:context=camel/camel-1,type=endpoints,name=\"mock://result\"");
        uri = (String) mbeanServer.getAttribute(name, "Uri");
        assertEquals("mock://result", uri);
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