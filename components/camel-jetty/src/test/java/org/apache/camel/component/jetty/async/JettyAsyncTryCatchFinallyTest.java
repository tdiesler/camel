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
package org.apache.camel.component.jetty.async;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty.BaseJettyTest;
import org.junit.Test;

/**
 * @version 
 */
public class JettyAsyncTryCatchFinallyTest extends BaseJettyTest {

    @Test
    public void testJettyAsync() throws Exception {
        getMockEndpoint("mock:try").expectedBodiesReceived("Hello Camel");
        getMockEndpoint("mock:catch").expectedBodiesReceived("Hello Camel");
        getMockEndpoint("mock:finally").expectedBodiesReceived("Bye Camel");
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");

        String reply = template.requestBody("http://localhost:{{port}}/myservice", "Hello Camel", String.class);
        assertEquals("Bye World", reply);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.addComponent("async", new MyAsyncComponent());

                from("jetty:http://localhost:{{port}}/myservice")
                    .convertBodyTo(String.class)
                    .doTry()
                        .to("mock:try")
                        .throwException(new IllegalArgumentException("Damn"))
                    .doCatch(IllegalArgumentException.class)
                        .to("mock:catch")
                        .to("async:bye:camel")
                    .doFinally()
                        .to("mock:finally")
                        .to("async:bye:world")
                    .end()
                    .to("mock:result");
            }
        };
    }
}