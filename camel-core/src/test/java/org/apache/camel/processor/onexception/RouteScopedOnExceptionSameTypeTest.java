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
package org.apache.camel.processor.onexception;

import java.io.IOException;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision$
 */
public class RouteScopedOnExceptionSameTypeTest extends ContextTestSupport {

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public void testOnExceptionExactType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:foo")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionDifferentType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:foo")
                    .onException(IOException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionSameTypeRouteLast() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionDifferentTypeRouteLast() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo")
                    .onException(IOException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionExactTypeDLC() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("mock:dead"));

                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:foo")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);
        getMockEndpoint("mock:dlc").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testTwoOnExceptionExactType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .onException(IOException.class)
                        .handled(true)
                        .to("mock:io")
                    .end()
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));

                from("direct:foo")
                    .onException(IOException.class)
                        .handled(true)
                        .to("mock:io")
                    .end()
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:foo")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);
        getMockEndpoint("mock:io").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionRouteAndGlobalExactType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(IllegalArgumentException.class)
                    .handled(true)
                    .to("mock:foo");

                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionRouteAndGlobalDifferentType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(IOException.class)
                    .handled(true)
                    .to("mock:foo");

                from("direct:start")
                    .onException(IllegalArgumentException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        getMockEndpoint("mock:damn").expectedMessageCount(1);
        getMockEndpoint("mock:foo").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    public void testOnExceptionRouteAndOnlyGlobalExactType() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(IllegalArgumentException.class)
                    .handled(true)
                    .to("mock:foo");

                from("direct:start")
                    .onException(IOException.class)
                        .handled(true)
                        .to("mock:damn")
                    .end()
                    .throwException(new IllegalArgumentException("Damn"));
            }
        });
        context.start();

        // this time we pick global scoped as its an exact match, so foo should get the message
        getMockEndpoint("mock:damn").expectedMessageCount(0);
        getMockEndpoint("mock:foo").expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

}
