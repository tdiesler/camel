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
package org.apache.camel.fabric;

import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.management.DefaultManagementNamingStrategy;

/**
 *
 */
public class FabricTracerTest extends ContextTestSupport {

    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = new DefaultCamelContext();
        DefaultManagementNamingStrategy naming = (DefaultManagementNamingStrategy) context.getManagementStrategy().getManagementNamingStrategy();
        naming.setHostName("localhost");
        naming.setDomainName("org.apache.camel");
        return context;
    }

    protected MBeanServer getMBeanServer() {
        return context.getManagementStrategy().getManagementAgent().getMBeanServer();
    }

    @Override
    protected boolean useJmx() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public void testFabricTracerEventMessage() throws Exception {
        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = null;
        Set<ObjectName> names = mbeanServer.queryNames(ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=services,*"), null);
        for (ObjectName name : names) {
            if (name.toString().contains("FabricTracer")) {
                on = name;
                break;
            }
        }
        assertNotNull(on);
        mbeanServer.isRegistered(on);

        Boolean enabled = (Boolean) mbeanServer.getAttribute(on, "Enabled");
        assertEquals("Should be enabled", Boolean.TRUE, enabled);

        List<FabricTracerEventMessage> events = (List<FabricTracerEventMessage>) mbeanServer.invoke(on, "dumpTracedMessages",
                new Object[]{"foo"}, new String[]{"java.lang.String"});

        assertNotNull(events);
        assertEquals(2, events.size());

        FabricTracerEventMessage event1 = events.get(0);
        assertEquals("foo", event1.getToNode());
        assertEquals("<message>\n" +
                "<body type=\"java.lang.String\">Hello World</body>\n" +
                "</message>", event1.getMessageAsXml());

        FabricTracerEventMessage event2 = events.get(1);
        assertEquals("foo", event2.getToNode());
        assertEquals("<message>\n" +
                "<body type=\"java.lang.String\">Bye World</body>\n" +
                "</message>", event2.getMessageAsXml());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
				// no breadcrumb in 2.7
                // context.setUseBreadcrumb(false);

                from("direct:start")
                    .to("mock:foo").id("foo")
                    .to("mock:bar").id("bar");

            }
        };
    }
}
