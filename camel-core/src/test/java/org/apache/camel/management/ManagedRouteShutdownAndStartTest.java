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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision$
 */
public class ManagedRouteShutdownAndStartTest extends ContextTestSupport {

    @Override
    protected boolean useJmx() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        deleteDirectory("target/managed");
        super.setUp();
    }

    public void testShutdownAndStartRoute() throws Exception {
        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();
        ObjectName on = getRouteObjectName(mbeanServer);

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");

        template.sendBodyAndHeader("file://target/managed", "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied();

        // should be started
        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Should be started", ServiceStatus.Started.name(), state);

        // calling the shutdown 
        mbeanServer.invoke(on, "shutdown", null, null);

        // the managed route object should be removed
        assertFalse("The managed route should be removed", mbeanServer.isRegistered(on));
        
        mock.reset();
        mock.expectedBodiesReceived("Bye World");
        // wait 3 seconds while route is stopped to verify that file was not consumed
        mock.setResultWaitTime(3000);

        template.sendBodyAndHeader("file://target/managed", "Bye World", Exchange.FILE_NAME, "bye.txt");

        // route is stopped so we do not get the file
        mock.assertIsNotSatisfied();
        
    }

    @SuppressWarnings("unchecked")
    static ObjectName getRouteObjectName(MBeanServer mbeanServer) throws Exception {
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=routes,*"), null);
        assertEquals(1, set.size());

        return set.iterator().next();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/managed").to("mock:result");
            }
        };
    }

}