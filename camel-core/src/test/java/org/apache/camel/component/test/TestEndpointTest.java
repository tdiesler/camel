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
package org.apache.camel.component.test;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 */
public class TestEndpointTest extends ContextTestSupport {

    public void testMocksAreValid() throws Exception {
        // perform the test, and send in 2 messages we expect
        Thread.sleep(500);
        template.sendBody("seda:foo", "Hello World");
        template.sendBody("seda:foo", "Bye World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // send 2 bodies to the seda:foo which is the messages we expect
                template.sendBody("seda:foo", "Hello World");
                template.sendBody("seda:foo", "Bye World");

                from("seda:foo")
                    .to("test:seda:foo");
            }
        };
    }
}
