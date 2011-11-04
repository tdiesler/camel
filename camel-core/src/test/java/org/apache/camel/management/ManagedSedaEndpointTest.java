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

/**
 * @version 
 */
public class ManagedSedaEndpointTest extends ManagementTestSupport {

    public void testSedaEndpoint() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(2);

        template.sendBody("seda:start", "Hello World");
        template.sendBody("seda:start", "Bye World");

        assertMockEndpointsSatisfied();

        MBeanServer mbeanServer = getMBeanServer();

        ObjectName name = ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=endpoints,name=\"seda://start\"");
        String uri = (String) mbeanServer.getAttribute(name, "EndpointUri");
        assertEquals("seda://start", uri);

        Long timeout = (Long) mbeanServer.getAttribute(name, "Timeout");
        assertEquals(30000, timeout.intValue());

        Integer size = (Integer) mbeanServer.getAttribute(name, "CurrentQueueSize");
        assertEquals(0, size.intValue());

        // stop route
        context.stopRoute("foo");

        // send a message to queue
        template.sendBody("seda:start", "Hi World");

        size = (Integer) mbeanServer.getAttribute(name, "CurrentQueueSize");
        assertEquals(1, size.intValue());

        mbeanServer.invoke(name, "purgeQueue", null, null);

        size = (Integer) mbeanServer.getAttribute(name, "CurrentQueueSize");
        assertEquals(0, size.intValue());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:start").routeId("foo").to("log:foo").to("mock:result");
            }
        };
    }

}
