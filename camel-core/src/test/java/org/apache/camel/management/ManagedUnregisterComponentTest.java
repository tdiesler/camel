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

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * @version $Revision$
 */
public class ManagedUnregisterComponentTest extends ContextTestSupport {

    @Override
    protected boolean useJmx() {
        return true;
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = new DefaultCamelContext();
        DefaultManagementNamingStrategy naming = (DefaultManagementNamingStrategy) context.getManagementStrategy().getManagementNamingStrategy();
        naming.setHostName("localhost");
        naming.setDomainName("org.apache.camel");
        return context;
    }

    @SuppressWarnings("unchecked")
    public void testUnregisterComponent() throws Exception {
        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();

        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=components,*"), null);
        assertEquals(2, set.size());

        ObjectName on = set.iterator().next();
        assertTrue("Should be registered", mbeanServer.isRegistered(on));

        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals(ServiceStatus.Started.name(), state);

        String id = (String) mbeanServer.getAttribute(on, "CamelId");
        assertEquals("camel-1", id);

        context.stop();

        assertFalse("Should no longer be registered", mbeanServer.isRegistered(on));
        set = mbeanServer.queryNames(new ObjectName("*:type=components,*"), null);
        assertEquals("Should no longer be registered", 0, set.size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:result");
            }
        };
    }

}
