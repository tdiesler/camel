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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision$
 */
public class ErrorOccuredInOnExceptionRoute extends ContextTestSupport {

    public void testErrorInOnException() throws Exception {
        getMockEndpoint("mock:onFunc").expectedMessageCount(1);
        getMockEndpoint("mock:doneFunc").expectedMessageCount(0);
        getMockEndpoint("mock:tech").expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // TODO: Should also work with DLC
                // errorHandler(deadLetterChannel("mock:dead").disableRedelivery());

                onException(MyTechnicalException.class)
                    .handled(true)
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            // System.out.println("tech");
                        }
                    })
                    .to("mock:tech");

                onException(MyFunctionalException.class)
                    .handled(true)
                    .to("mock:onFunc")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                // System.out.println("func");
                                throw new MyTechnicalException("Tech error");
                            }
                        })
                    .to("mock:doneFunc");

                // in this regular route the processing failed
                from("direct:start")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            throw new MyFunctionalException("Func error");
                        }
                    });
            }
        };

    }
}
