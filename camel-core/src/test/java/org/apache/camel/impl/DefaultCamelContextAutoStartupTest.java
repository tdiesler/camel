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
package org.apache.camel.impl;

import junit.framework.TestCase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision$
 */
public class DefaultCamelContextAutoStartupTest extends TestCase {

    public void testAutoStartupFalse() throws Exception {
        DefaultCamelContext camel = new DefaultCamelContext(new SimpleRegistry());
        camel.setAutoStartup(false);

        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:result");
            }
        });
        camel.start();

        assertEquals(false, camel.isStarted());
        assertEquals(0, camel.getRoutes().size());

        // now start it again as auto startup prevented it from starting first time
        camel.start();

        assertEquals(true, camel.isStarted());
        assertEquals(1, camel.getRoutes().size());

        // and now its started we can test that it works by sending in a message to the route
        MockEndpoint mock = camel.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);

        camel.createProducerTemplate().sendBody("direct:start", "Hello World");

        mock.assertIsSatisfied();
    }

    public void testAutoStartupTrue() throws Exception {
        DefaultCamelContext camel = new DefaultCamelContext(new SimpleRegistry());
        camel.setAutoStartup(true);

        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:result");
            }
        });
        camel.start();

        assertEquals(true, camel.isStarted());
        assertEquals(1, camel.getRoutes().size());

        MockEndpoint mock = camel.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMessageCount(1);

        camel.createProducerTemplate().sendBody("direct:start", "Hello World");

        mock.assertIsSatisfied();
    }

}
