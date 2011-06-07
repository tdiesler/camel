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
package org.apache.camel.issues;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class ExceptionThrownFromOnExceptionTest extends ContextTestSupport {

    private static AtomicInteger retry = new AtomicInteger();
    private static AtomicInteger onExceptionRetry = new AtomicInteger();

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public void testExceptionThrownFromOnException() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            throw new IOException("Some other IOException");
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                    .to("direct:intermediate")
                    .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);

        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IOException cause = assertIsInstanceOf(IOException.class, e.getCause());
            assertEquals("Some other IOException", cause.getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

    public void testExceptionThrownFromOnExceptionAndHandled() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    // this time we handle the exception
                    .handled(true)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            throw new IOException("Some other IOException");
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                    .to("direct:intermediate")
                    .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);

        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IOException cause = assertIsInstanceOf(IOException.class, e.getCause());
            assertEquals("Some other IOException", cause.getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

    public void testExceptionThrownFromOnExceptionWithDeadLetterChannel() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // DLC
                deadLetterChannel("mock:error").redeliveryDelay(0).maximumRedeliveries(3);

                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            throw new IOException("Some other IOException");
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                    .to("direct:intermediate")
                    .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        // the error will not be handled by DLC since we had an onException, and that failed,
        // so the exchange will throw an exception
        getMockEndpoint("mock:error").expectedMessageCount(0);

        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IOException cause = assertIsInstanceOf(IOException.class, e.getCause());
            assertEquals("Some other IOException", cause.getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

    public void testExceptionThrownFromOnExceptionAndHandledWithDeadLetterChannel() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // DLC
                deadLetterChannel("mock:error").redeliveryDelay(0).maximumRedeliveries(3);

                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    // this time we handle the exception
                    .handled(true)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            throw new IOException("Some other IOException");
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                    .to("direct:intermediate")
                    .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        // the error will not be handled by DLC since we had an onException, and that failed,
        // so the exchange will throw an exception
        getMockEndpoint("mock:error").expectedMessageCount(0);

        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IOException cause = assertIsInstanceOf(IOException.class, e.getCause());
            assertEquals("Some other IOException", cause.getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

    public void testNoExceptionThrownFromOnExceptionWithDeadLetterChannel() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // DLC
                deadLetterChannel("mock:error").redeliveryDelay(0).maximumRedeliveries(3);

                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            // no exception is thrown this time
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                     .to("direct:intermediate")
                     .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        // the exception is handled by the onException and thus not the DLC
        getMockEndpoint("mock:error").expectedMessageCount(0);

        // and this time there was no exception thrown from onException,
        // but the caller still fails since handled is false on onException
        try {
            template.sendBody("direct:start", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            // this time its the first exception thrown from the route
            IOException cause = assertIsInstanceOf(IOException.class, e.getCause());
            assertEquals("IO error", cause.getMessage());
        }

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

    public void testNoExceptionThrownFromOnExceptionAndHandledWithDeadLetterChannel() throws Exception {
        retry.set(0);
        onExceptionRetry.set(0);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // DLC
                deadLetterChannel("mock:error").redeliveryDelay(0).maximumRedeliveries(3);

                // on exception to catch all IO exceptions and handle them specially
                onException(IOException.class)
                    .maximumRedeliveries(3)
                    // we now handle the exception
                    .handled(true)
                    .to("mock:b")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            onExceptionRetry.incrementAndGet();
                            // no exception is thrown this time
                        }
                    })
                    .to("mock:c");

                from("direct:start")
                     .to("direct:intermediate")
                     .to("mock:result");

                from("direct:intermediate")
                    .to("mock:a")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            retry.incrementAndGet();
                            throw new IOException("IO error");
                        }
                    })
                    .to("mock:end");
            }
        });
        context.start();

        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);
        getMockEndpoint("mock:c").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        // the exception is handled by onException so it goes not in DLC
        getMockEndpoint("mock:error").expectedMessageCount(0);

        // and this time there was no exception thrown from onException,
        // and the exception is handled so the caller should not fail
        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        assertEquals("Should try 4 times (1 first, 3 retry)", 4, retry.get());
        assertEquals("Should only invoke onException once", 1, onExceptionRetry.get());
    }

}

