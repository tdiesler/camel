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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ThrottlingRoutePolicy;

/**
 * @version $Revision$
 */
public class ThrottelingRoutePolicyTest extends ContextTestSupport {

    private String url = "seda:foo?concurrentConsumers=20";
    private int size = 100;

    public void testThrottelingRoutePolicy() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(size);

        for (int i = 0; i < size; i++) {
            template.sendBody(url, "Message " + i);
        }

        // now start the route
        context.startRoute("myRoute");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                ThrottlingRoutePolicy policy = new ThrottlingRoutePolicy();
                policy.setMaxInflightExchanges(10);

                from(url)
                    .routePolicy(policy).noAutoStartup().id("myRoute")
                    .to("log:foo?groupSize=10").to("mock:result");
            }
        };
    }
}
