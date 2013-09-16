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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class ManagedRouteRemoveWireTapExplicitThreadPoolTest extends ManagementTestSupport {

    private ExecutorService myThreadPool;

    public void testRemove() throws Exception {
        // JMX tests dont work well on AIX CI servers (hangs them)
        if (isPlatform("aix")) {
            return;
        }

        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = ObjectName.getInstance("org.apache.camel:context=localhost/camel-1,type=routes,name=\"foo\"");

        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:tap").expectedMessageCount(1);

        template.sendBody("seda:foo", "Hello World");

        assertMockEndpointsSatisfied();

        // should be started
        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Should be started", ServiceStatus.Started.name(), state);

        // and no wire tap thread pool as we use an existing external pool
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=threadpools,*"), null);
        boolean wireTap = false;
        for (ObjectName name : set) {
            if (name.toString().contains("wireTap")) {
                wireTap = true;
                break;
            }
        }
        assertFalse("Should not have a wire tap thread pool", wireTap);

        // stop
        mbeanServer.invoke(on, "stop", null, null);

        state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals("Should be stopped", ServiceStatus.Stopped.name(), state);

        // remove
        mbeanServer.invoke(on, "remove", null, null);

        // should not be registered anymore
        boolean registered = mbeanServer.isRegistered(on);
        assertFalse("Route mbean should have been unregistered", registered);

        // and no wire tap thread pool as we use an existing external pool
        set = mbeanServer.queryNames(new ObjectName("*:type=threadpools,*"), null);
        wireTap = false;
        for (ObjectName name : set) {
            if (name.toString().contains("wireTap")) {
                wireTap = true;
                break;
            }
        }
        assertFalse("Should not have a wire tap thread pool", wireTap);

        // should not be shutdown
        assertFalse("Thread pool should not be shutdown", myThreadPool.isShutdown());

        myThreadPool.shutdownNow();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // create a new thread pool to use for wire tap
                myThreadPool = Executors.newFixedThreadPool(1);

                from("seda:foo").routeId("foo").wireTap("direct:tap").executorService(myThreadPool).to("mock:result");
                from("direct:tap").routeId("tap").to("mock:tap");
            }
        };
    }

}
