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
package org.apache.camel.component.hazelcast;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Test concurrent consumers.
 * 
 */
public class HazelcastSedaConcurrentConsumersTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    private MockEndpoint mock;

    @Test
    public void createConcurrentConsumers() throws Exception {
        int bodyCount = 5;

        mock.expectedBodiesReceived("test");
        mock.expectedMessageCount(bodyCount);

        for (int i = 0; i < bodyCount; i++) {
            template.sendBody("hazelcast:seda:foo?concurrentConsumers=4", "test");
        }

        assertMockEndpointsSatisfied();
        mock.reset();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("hazelcast:seda:foo?concurrentConsumers=4").threads(6).to("mock:result");
            }
        };
    }
}
