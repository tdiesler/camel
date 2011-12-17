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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ThreadPoolProfileSupport;
import org.apache.camel.management.ManagementTestSupport;
import org.apache.camel.spi.ThreadPoolProfile;

/**
 *
 */
public class ThrottlerAsyncDelayedCallerRunsTest extends ManagementTestSupport {

    public void testThrottler() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(6);

        template.sendBody("seda:start", "A");
        template.sendBody("seda:start", "B");
        template.sendBody("seda:start", "C");
        template.sendBody("seda:start", "D");
        template.sendBody("seda:start", "E");
        template.sendBody("seda:start", "F");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // create a profile for the throttler
                ThreadPoolProfile profile = new ThreadPoolProfileSupport("myThrottler");
                profile.setMaxPoolSize(5);
                profile.setMaxQueueSize(2);
                context.getExecutorServiceStrategy().registerThreadPoolProfile(profile);

                from("seda:start")
                        .throttle(1).timePeriodMillis(100)
                        .asyncDelayed().executorServiceRef("myThrottler").callerRunsWhenRejected(true)
                        .to("mock:result");
            }
        };
    }
}
