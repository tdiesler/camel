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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version 
 */
public class ManagedCamelContextSuspendResumeTest extends ManagementTestSupport {

    public void testManagedCamelContext() throws Exception {
        MBeanServer mbeanServer = getMBeanServer();

        ObjectName on = ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=context,name=\"camel-1\"");

        assertTrue("Should be registered", mbeanServer.isRegistered(on));
        String name = (String) mbeanServer.getAttribute(on, "CamelId");
        assertEquals("camel-1", name);

        String uptime = (String) mbeanServer.getAttribute(on, "Uptime");
        assertNotNull(uptime);

        String status = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Started", status);

        // invoke operations
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");

        Object reply = mbeanServer.invoke(on, "requestBody", new Object[]{"direct:foo", "Hello World"}, new String[]{"java.lang.String", "java.lang.Object"});
        assertEquals("Bye World", reply);

        // suspend Camel
        mbeanServer.invoke(on, "suspend", null, null);

        status = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Suspended", status);

        // resume Camel
        mbeanServer.invoke(on, "resume", null, null);

        status = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Started", status);

        reply = mbeanServer.invoke(on, "requestBody", new Object[]{"direct:foo", "Hello Camel"}, new String[]{"java.lang.String", "java.lang.Object"});
        assertEquals("Bye World", reply);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo").transform(constant("Bye World"));
            }
        };
    }

}
