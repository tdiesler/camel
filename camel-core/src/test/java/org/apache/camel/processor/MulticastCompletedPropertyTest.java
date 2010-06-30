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
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision$
 */
public class MulticastCompletedPropertyTest extends ContextTestSupport {

    public void testMulticastCompleted() throws Exception {
        MockEndpoint a = getMockEndpoint("mock:a");
        a.expectedMessageCount(1);
        a.message(0).property(Exchange.MULTICAST_COMPLETE).isEqualTo(false);

        MockEndpoint b = getMockEndpoint("mock:b");
        b.expectedMessageCount(1);
        b.message(0).property(Exchange.MULTICAST_COMPLETE).isEqualTo(false);

        MockEndpoint c = getMockEndpoint("mock:c");
        c.expectedMessageCount(1);
        c.message(0).property(Exchange.MULTICAST_COMPLETE).isEqualTo(true);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .multicast().to("mock:a", "mock:b", "mock:c");
            }
        };
    }

}