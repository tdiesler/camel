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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 */
public class AdviceWithContextScopedErrorHandlerIssueTest extends ContextTestSupport {

    public void testAdviceWith() throws Exception {
        context.getRouteDefinition("route-a").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint("direct:bar")
                    .skipSendToOriginalEndpoint()
                    .throwException(new IllegalArgumentException("Forced"));
            }
        });

        getMockEndpoint("mock:error").expectedMessageCount(1);
        getMockEndpoint("mock:bar").expectedMessageCount(0);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("mock:error"));

                from("direct:start").routeId("route-a").to("direct:bar");
                from("direct:bar").routeId("route-b").to("mock:bar");
            }
        };
    }
}
