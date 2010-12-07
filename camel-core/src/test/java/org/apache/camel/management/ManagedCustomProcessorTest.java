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
package org.apache.camel.management;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @version $Revision: 950375 $
 */
public class ManagedCustomProcessorTest extends ContextTestSupport {

    @Override
    protected boolean useJmx() {
        return true;
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        DefaultManagementNamingStrategy naming = (DefaultManagementNamingStrategy) context.getManagementStrategy().getManagementNamingStrategy();
        naming.setHostName("localhost");
        naming.setDomainName("org.apache.camel");
        return context;
    }

    public void testManageCustomProcessor() throws Exception {
        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();
        ObjectName on = ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=processors,name=\"custom\"");

        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedHeaderReceived("foo", "hey");
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();

        String foo = (String) mbeanServer.getAttribute(on, "Foo");
        assertEquals("hey", foo);

        // change foo
        mbeanServer.setAttribute(on, new Attribute("Foo", "changed"));

        resetMocks();

        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedHeaderReceived("foo", "changed");
        template.sendBody("direct:start", "Bye World");
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").routeId("foo")
                    .process(new MyCustomProcessor()).id("custom")
                    .to("mock:result");
            }
        };
    }

    // START SNIPPET: e1
    @ManagedResource("My Managed Component")
    public static class MyCustomProcessor implements Processor {
        private String foo = "hey";

        @ManagedAttribute
        public String getFoo() {
            return foo;
        }

        @ManagedAttribute
        public void setFoo(String foo) {
            this.foo = foo;
        }

        public void process(Exchange exchange) throws Exception {
            exchange.getIn().setHeader("foo", getFoo());
        }
    }
    // END SNIPPET: e1

}
