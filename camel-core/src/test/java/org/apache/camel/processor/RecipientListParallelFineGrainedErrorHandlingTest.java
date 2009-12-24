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
package org.apache.camel.processor;

import java.util.concurrent.ExecutionException;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;

/**
 * @version $Revision$
 */
public class RecipientListParallelFineGrainedErrorHandlingTest extends ContextTestSupport {

    private static int counter;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("fail", new MyFailBean());
        return jndi;
    }

    public void testRecipientListOk() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(Exception.class).maximumRedeliveries(2);

                from("direct:start")
                    .to("mock:a")
                    .recipientList(header("foo")).stopOnException().parallelProcessing();
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(1);
        getMockEndpoint("mock:bar").expectedMessageCount(1);
        getMockEndpoint("mock:baz").expectedMessageCount(1);

        template.sendBodyAndHeader("direct:start", "Hello World", "foo", "mock:foo,mock:bar,mock:baz");

        assertMockEndpointsSatisfied();
    }

    public void testRecipientListError() throws Exception {
        counter = 0;

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(Exception.class).maximumRedeliveries(2);

                from("direct:start")
                    .to("mock:a")
                    .recipientList(header("foo")).stopOnException().parallelProcessing();
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(1);
        getMockEndpoint("mock:bar").expectedMessageCount(1);
        getMockEndpoint("mock:baz").expectedMessageCount(1);

        try {
            template.sendBodyAndHeader("direct:start", "Hello World", "foo", "mock:foo,mock:bar,bean:fail,mock:baz");
            fail("Should throw exception");
        } catch (Exception e) {
            // expected
        }

        assertMockEndpointsSatisfied();

        assertEquals(3, counter);
    }

    public void testRecipientListAsBeanError() throws Exception {
        counter = 0;

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.setTracing(true);

                onException(Exception.class).maximumRedeliveries(2);

                from("direct:start")
                    .to("mock:a")
                    .bean(MyRecipientBean.class);
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(1);
        getMockEndpoint("mock:bar").expectedMessageCount(1);
        getMockEndpoint("mock:baz").expectedMessageCount(1);

        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should throw exception");
        } catch (CamelExecutionException e) {
            // expected
            assertIsInstanceOf(ExecutionException.class, e.getCause());
            assertIsInstanceOf(CamelExchangeException.class, e.getCause().getCause());
            assertIsInstanceOf(IllegalArgumentException.class, e.getCause().getCause().getCause());
            assertEquals("Damn", e.getCause().getCause().getCause().getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals(3, counter);
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public static class MyRecipientBean {

        @org.apache.camel.RecipientList(stopOnException = true, parallelProcessoing = true)
        public String sendSomewhere(Exchange exchange) {
            return "mock:foo,mock:bar,bean:fail,mock:baz";
        }
    }

    public static class MyFailBean {

        public String doSomething(Exchange exchange) throws Exception {
            counter++;
            assertEquals("bean://fail", exchange.getProperty(Exchange.TO_ENDPOINT, String.class));
            throw new IllegalArgumentException("Damn");
        }
    }
}