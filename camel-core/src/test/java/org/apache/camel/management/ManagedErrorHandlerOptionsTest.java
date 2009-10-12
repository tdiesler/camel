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
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision$
 */
public class ManagedErrorHandlerOptionsTest extends ContextTestSupport {

    private static int counter;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        DefaultManagementNamingStrategy naming = (DefaultManagementNamingStrategy) context.getManagementStrategy().getManagementNamingStrategy();
        naming.setHostName("localhost");
        naming.setDomainName("org.apache.camel");
        return context;
    }

    public void testManagedErrorHandlerOptions() throws Exception {
        counter = 0;

        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();

        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=errorhandlers,*"), null);
        assertEquals(1, set.size());
        ObjectName on = set.iterator().next();

        mbeanServer.setAttribute(on, new Attribute("MaximumRedeliveries", 3));
        Integer max = (Integer) mbeanServer.getAttribute(on, "MaximumRedeliveries");
        assertEquals(3, max.intValue());

        mbeanServer.setAttribute(on, new Attribute("MaximumRedeliveryDelay", Long.valueOf("20000")));
        Long delay = (Long) mbeanServer.getAttribute(on, "MaximumRedeliveryDelay");
        assertEquals(20000, delay.longValue());

        mbeanServer.setAttribute(on, new Attribute("RedeliveryDelay", Long.valueOf("250")));
        delay = (Long) mbeanServer.getAttribute(on, "RedeliveryDelay");
        assertEquals(250, delay.longValue());

        String camelId = (String) mbeanServer.getAttribute(on, "CamelId");
        assertEquals("camel-1", camelId);

        Double backoff = (Double) mbeanServer.getAttribute(on, "BackOffMultiplier");
        assertNotNull(backoff);

        Double cf = (Double) mbeanServer.getAttribute(on, "CollisionAvoidanceFactor");
        assertNotNull(cf);

        Double cp = (Double) mbeanServer.getAttribute(on, "CollisionAvoidancePercent");
        assertNotNull(cp);

        String dp = (String) mbeanServer.getAttribute(on, "DelayPattern");
        assertNull(dp);

        String ell = (String) mbeanServer.getAttribute(on, "RetriesExhaustedLogLevel");
        assertEquals(LoggingLevel.DEBUG.name(), ell);

        String rll = (String) mbeanServer.getAttribute(on, "RetryAttemptedLogLevel");
        assertEquals(LoggingLevel.DEBUG.name(), rll);

        Boolean lst = (Boolean) mbeanServer.getAttribute(on, "LogStackTrace");
        assertEquals(false, lst.booleanValue());

        Boolean uca = (Boolean) mbeanServer.getAttribute(on, "UseCollisionAvoidance");
        assertEquals(false, uca.booleanValue());

        Boolean uebf = (Boolean) mbeanServer.getAttribute(on, "UseExponentialBackOff");
        assertEquals(false, uebf.booleanValue());

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertEquals(3, counter);

        assertMockEndpointsSatisfied();

        // now change to 0 attempts and try again
        counter = 0;
        mock.reset();
        mock.expectedMessageCount(0);
        mbeanServer.setAttribute(on, new Attribute("MaximumRedeliveries", 0));

        try {
            template.sendBody("direct:start", "Bye World");
            fail("Should have thrown exception");
        } catch (CamelExecutionException e) {
            IllegalArgumentException cause = assertIsInstanceOf(IllegalArgumentException.class, e.getCause());
            assertEquals("Forced", cause.getMessage());

        }

        assertEquals(1, counter);

        // and should now be 0
        max = (Integer) mbeanServer.getAttribute(on, "MaximumRedeliveries");
        assertEquals(0, max.intValue());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(defaultErrorHandler().maximumRedeliveries(5));

                from("direct:start").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        counter++;
                        if (counter < 3) {
                            throw new  IllegalArgumentException("Forced");
                        }
                    }
                }).to("mock:result");
            }
        };
    }
}