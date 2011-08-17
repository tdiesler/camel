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
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.management.DefaultManagementNamingStrategy;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LogDefinition;
import org.apache.camel.model.OtherwiseDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.WhenDefinition;

/**
 *
 */
public class FabricTracerIdOnAllNodesTest extends ContextTestSupport {

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

        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = null;
        Set<ObjectName> names = mbeanServer.queryNames(ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=fabric,*"), null);
        for (ObjectName name : names) {
            if (name.toString().contains("FabricTracer")) {
                on = name;
                break;
            }
        }
        assertNotNull(on);
        mbeanServer.isRegistered(on);

        Boolean enabled = (Boolean) mbeanServer.getAttribute(on, "Enabled");
        assertEquals("Should not be enabled", Boolean.FALSE, enabled);

        Integer size = (Integer) mbeanServer.getAttribute(on, "QueueSize");
        assertEquals("Should be 10", 10, size.intValue());

        // enable it
        mbeanServer.setAttribute(on, new Attribute("Enabled", Boolean.TRUE));

        getMockEndpoint("mock:camel").expectedBodiesReceived("Hello Camel");
        getMockEndpoint("mock:other").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:end").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello Camel");
        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        List<Exchange> fooExchanges = getMockEndpoint("mock:foo").getReceivedExchanges();
        List<Exchange> camelExchanges = getMockEndpoint("mock:camel").getReceivedExchanges();

        RouteDefinition route = context.getRouteDefinitions().get(0);
        assertNotNull(route);

        ChoiceDefinition choice = (ChoiceDefinition) route.getOutputs().get(0);
        assertEquals("choice1", choice.getId());

        WhenDefinition when = (WhenDefinition) choice.getOutputs().get(0);
        assertEquals("when1", when.getId());

        LogDefinition log1 = (LogDefinition) when.getOutputs().get(0);
        assertEquals("log1", log1.getId());

        ToDefinition to1 = (ToDefinition) when.getOutputs().get(1);
        assertEquals("camel", to1.getId());

        OtherwiseDefinition other = (OtherwiseDefinition) choice.getOutputs().get(1);
        assertEquals("otherwise1", other.getId());

        LogDefinition log2 = (LogDefinition) other.getOutputs().get(0);
        assertEquals("log2", log2.getId());

        ToDefinition to2 = (ToDefinition) other.getOutputs().get(1);
        assertEquals("to1", to2.getId());

        ToDefinition to3 = (ToDefinition) other.getOutputs().get(2);
        assertEquals("foo", to3.getId());

        ToDefinition to4 = (ToDefinition) route.getOutputs().get(1);
        assertEquals("end", to4.getId());

        List<FabricTracerEventMessage> events = (List<FabricTracerEventMessage>) mbeanServer.invoke(on, "dumpTracedMessages",
                new Object[]{"to1"}, new String[]{"java.lang.String"});

        assertNotNull(events);
        assertEquals(1, events.size());

        FabricTracerEventMessage event1 = events.get(0);
        assertEquals("to1", event1.getToNode());
        assertEquals("<message exchangeId=\"" + fooExchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello World</body>\n"
                + "</message>", event1.getMessageAsXml());

        events = (List<FabricTracerEventMessage>) mbeanServer.invoke(on, "dumpTracedMessages",
                new Object[]{"camel"}, new String[]{"java.lang.String"});

        assertNotNull(events);
        assertEquals(1, events.size());

        event1 = events.get(0);
        assertEquals("camel", event1.getToNode());
        assertEquals("<message exchangeId=\"" + camelExchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello Camel</body>\n"
                + "</message>", event1.getMessageAsXml());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.setUseBreadcrumb(false);

                from("direct:start")
                    .choice()
                        .when(body().contains("Camel"))
                            .log("A Camel message")
                            .to("mock:camel").id("camel")
                        .otherwise()
                            .log("Some other kind of message")
                            .to("mock:other") // should auto generate id
                            .to("mock:foo").id("foo")
                        .end()
                    .to("mock:end").id("end");
            }
        };
    }
}
