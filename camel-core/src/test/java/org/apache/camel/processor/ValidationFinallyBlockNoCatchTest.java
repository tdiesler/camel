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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * No catch blocks but handle all should work
 *
 * @author <a href="mailto:nsandhu@raleys.com">nsandhu</a>
 */
public class ValidationFinallyBlockNoCatchTest extends ContextTestSupport {
    protected Processor validator = new MyValidator();
    protected MockEndpoint validEndpoint;
    protected MockEndpoint allEndpoint;
    protected MockEndpoint deadEndpoint;

    public void testValidMessage() throws Exception {
        validEndpoint.expectedMessageCount(1);
        allEndpoint.expectedMessageCount(1);

        template.sendBodyAndHeader("direct:start", "<valid/>", "foo", "bar");

        assertMockEndpointsSatisfied();
    }

    public void testInvalidMessage() throws Exception {
        validEndpoint.expectedMessageCount(0);
        
        // allEndpoint should only receive 1 when the message is being moved to the dead letter queue
        allEndpoint.expectedMessageCount(1);

        // regular error handler is disbled for try .. catch .. finally
        deadEndpoint.expectedMessageCount(0);

        try {
            template.sendBodyAndHeader("direct:start", "<invalid/>", "foo", "notMatchedHeaderValue");
        } catch (Exception e) {
            // expected
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        validEndpoint = resolveMandatoryEndpoint("mock:valid", MockEndpoint.class);
        allEndpoint = resolveMandatoryEndpoint("mock:all", MockEndpoint.class);
        deadEndpoint = resolveMandatoryEndpoint("mock:dead", MockEndpoint.class);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // use dead letter channel that supports redeliveries
                errorHandler(deadLetterChannel("mock:dead").delay(0).maximumRedeliveries(3).logStackTrace(false));

                from("direct:start")
                    .doTry()
                        .process(validator)
                        .to("mock:valid")
                    .doFinally()
                        .to("mock:all");
            }
        };
    }
}
