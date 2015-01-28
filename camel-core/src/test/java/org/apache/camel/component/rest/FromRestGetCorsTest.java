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
package org.apache.camel.component.rest;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spi.RestConfiguration;

public class FromRestGetCorsTest extends ContextTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("dummy-rest", new DummyRestConsumerFactory());
        return jndi;
    }

    public void testCors() throws Exception {
        // the rest becomes routes and the input is a seda endpoint created by the DummyRestConsumerFactory
        getMockEndpoint("mock:update").expectedMessageCount(1);

        Exchange out = template.request("seda:post-say-bye", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("I was here");
            }
        });
        assertNotNull(out);

        assertEquals(out.getOut().getHeader("Access-Control-Allow-Origin"), RestConfiguration.CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
        assertEquals(out.getOut().getHeader("Access-Control-Allow-Methods"), RestConfiguration.CORS_ACCESS_CONTROL_ALLOW_METHODS);
        assertEquals(out.getOut().getHeader("Access-Control-Allow-Headers"), RestConfiguration.CORS_ACCESS_CONTROL_ALLOW_HEADERS);
        assertEquals(out.getOut().getHeader("Access-Control-Max-Age"), RestConfiguration.CORS_ACCESS_CONTROL_MAX_AGE);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration().enableCORS(true);

                rest("/say/hello")
                    .get().to("direct:hello");

                rest("/say/bye")
                    .get().consumes("application/json").to("direct:bye")
                    .post().to("mock:update");

                from("direct:hello")
                    .transform().constant("Hello World");

                from("direct:bye")
                    .transform().constant("Bye World");
            }
        };
    }
}
