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
package org.apache.camel.component.quartz2;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @version 
 */
public class QuartzAddRoutesAfterCamelContextStartedTest extends CamelTestSupport {

    @Test
    public void testAddRoutes() throws Exception {
        // camel context should already be started
        assertTrue(context.getStatus().isStarted());

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);

        // add the quartz router after CamelContext has been started
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("quartz2://myGroup/myTimerName?trigger.repeatInterval=1000&trigger.repeatCount=1").to("mock:result");
            }
        });

        // it should also work
        assertMockEndpointsSatisfied();
    }

}
