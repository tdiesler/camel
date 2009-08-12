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
package org.fusesource.camel.management;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.management.CommonManagementLifecycleStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class FuseCommonsManagementRegisterCamelContextTest extends CamelTestSupport {

    private CommonManagementLifecycleStrategy lifecycle = new CommonManagementLifecycleStrategy();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        DefaultCamelContext context = new DefaultCamelContext(createRegistry());
        context.setLifecycleStrategy(lifecycle);
        return context;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCamelContext() throws Exception {
        MBeanServer mbeanServer = lifecycle.getStrategy().getMbeanServer();

        ObjectName on = ObjectName.getInstance("org.apache.camel:context=camel/camel-1,type=context,name=\"camel-1\"");
        String name = (String) mbeanServer.getAttribute(on, "Name");
        assertEquals("camel-1", name);

        Boolean started = (Boolean) mbeanServer.getAttribute(on, "Started");
        assertEquals(true, started);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("log:foo").to("mock:result");
            }
        };
    }

}
