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
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ManagementStatisticsLevel;

/**
 * @version $Revision$
 */
public class ManagedStatisticsLevelOffTest extends ContextTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        DefaultManagementNamingStrategy naming = (DefaultManagementNamingStrategy) context.getManagementStrategy().getManagementNamingStrategy();
        naming.setHostName("localhost");
        naming.setDomainName("org.apache.camel");

        // disable it by default
        context.getManagementStrategy().setSatisticsLevel(ManagementStatisticsLevel.Off);
        return context;
    }

    @SuppressWarnings("unchecked")
    public void testManageStatisticsLevelDisabled() throws Exception {
        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        // get the stats for the route
        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();

        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=routes,*"), null);
        assertEquals(1, set.size());

        ObjectName on = set.iterator().next();

        // use route to get the total time
        Long completed = (Long) mbeanServer.getAttribute(on, "ExchangesCompleted");
        assertEquals(0, completed.longValue());

        // disable statistics
        mbeanServer.setAttribute(on, new Attribute("StatisticsEnabled", true));

        // send in another message
        template.sendBody("direct:start", "Goodday World");

        // should be 1
        completed = (Long) mbeanServer.getAttribute(on, "ExchangesCompleted");
        assertEquals(1, completed.longValue());
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