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

import org.apache.camel.Exchange;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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

        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        List<Exchange> exchanges = getMockEndpoint("mock:foo").getReceivedExchanges();

        List<FabricTracerEventMessage> events = (List<FabricTracerEventMessage>) mbeanServer.invoke(on, "dumpTracedMessages",
                new Object[]{"foo"}, new String[]{"java.lang.String"});

        assertNotNull(events);
        assertEquals(2, events.size());

        FabricTracerEventMessage event1 = events.get(0);
        assertEquals("foo", event1.getToNode());
        assertEquals("<message exchangeId=\"" + exchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello World</body>\n"
                + "</message>", event1.getMessageAsXml());

        FabricTracerEventMessage event2 = events.get(1);
        assertEquals("foo", event2.getToNode());
        assertEquals("<message exchangeId=\"" + exchanges.get(1).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Bye World</body>\n"
                + "</message>", event2.getMessageAsXml());
    }

    @SuppressWarnings("unchecked")
    public void testFabricTracerEventMessageAsXml() throws Exception {

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

        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        String events = (String) mbeanServer.invoke(on, "dumpTracedMessagesAsXml",
                new Object[]{"foo"}, new String[]{"java.lang.String"});

        assertNotNull(events);
        log.info(events);

        // should be valid XML
        Document dom = context.getTypeConverter().convertTo(Document.class, events);
        assertNotNull(dom);

        NodeList list = dom.getElementsByTagName("fabricTracerEventMessage");
        assertEquals(2, list.getLength());
    }

    @SuppressWarnings("unchecked")
    public void testFabricTracerEventMessageDumpAll() throws Exception {

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

        // enable it
        mbeanServer.setAttribute(on, new Attribute("Enabled", Boolean.TRUE));

        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        List<Exchange> fooExchanges = getMockEndpoint("mock:foo").getReceivedExchanges();
        List<Exchange> barExchanges = getMockEndpoint("mock:bar").getReceivedExchanges();

        List<FabricTracerEventMessage> events = (List<FabricTracerEventMessage>) mbeanServer.invoke(on, "dumpAllTracedMessages", null, null);

        assertNotNull(events);
        assertEquals(6, events.size());

        FabricTracerEventMessage event0 = events.get(0);
        assertEquals("route1", event0.getToNode());
        assertEquals("<message exchangeId=\"" + fooExchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello World</body>\n"
                + "</message>", event0.getMessageAsXml());

        FabricTracerEventMessage event1 = events.get(1);
        assertEquals("foo", event1.getToNode());
        assertEquals("<message exchangeId=\"" + fooExchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello World</body>\n"
                + "</message>", event1.getMessageAsXml());

        FabricTracerEventMessage event2 = events.get(2);
        assertEquals("bar", event2.getToNode());
        assertEquals("<message exchangeId=\"" + barExchanges.get(0).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Hello World</body>\n"
                + "</message>", event2.getMessageAsXml());

        FabricTracerEventMessage event3 = events.get(3);
        assertEquals("route1", event3.getToNode());
        assertEquals("<message exchangeId=\"" + fooExchanges.get(1).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Bye World</body>\n"
                + "</message>", event3.getMessageAsXml());

        FabricTracerEventMessage event4 = events.get(4);
        assertEquals("foo", event4.getToNode());
        assertEquals("<message exchangeId=\"" + fooExchanges.get(1).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Bye World</body>\n"
                + "</message>", event3.getMessageAsXml());

        FabricTracerEventMessage event5 = events.get(5);
        assertEquals("bar", event5.getToNode());
        assertEquals("<message exchangeId=\"" + barExchanges.get(1).getExchangeId() + "\">\n"
                + "<body type=\"java.lang.String\">Bye World</body>\n"
                + "</message>", event4.getMessageAsXml());
    }

    @SuppressWarnings("unchecked")
    public void testFabricTracerEventMessageDumpAllAsXml() throws Exception {

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

        // enable it
        mbeanServer.setAttribute(on, new Attribute("Enabled", Boolean.TRUE));

        getMockEndpoint("mock:foo").expectedMessageCount(2);
        getMockEndpoint("mock:bar").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        String events = (String) mbeanServer.invoke(on, "dumpAllTracedMessagesAsXml", null, null);

        assertNotNull(events);
        log.info(events);

        // should be valid XML
        Document dom = context.getTypeConverter().convertTo(Document.class, events);
        assertNotNull(dom);

        NodeList list = dom.getElementsByTagName("fabricTracerEventMessage");
        assertEquals(6, list.getLength());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.setUseBreadcrumb(false);

                from("direct:start")
                    .to("mock:foo").id("foo")
                    .to("mock:bar").id("bar");

            }
        };
    }
}
