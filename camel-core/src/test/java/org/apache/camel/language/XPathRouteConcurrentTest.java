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
package org.apache.camel.language;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision$
 */
public class XPathRouteConcurrentTest extends ContextTestSupport {

    public void testXPathNotConcurrent() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:other").expectedMessageCount(0);

        template.sendBody("seda:foo", "<person><name>Claus</name></person>");

        assertMockEndpointsSatisfied();
    }

    public void testXPathTwoMessages() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:other").expectedMessageCount(1);

        template.sendBody("seda:foo", "<person><name>Claus</name></person>");
        template.sendBody("seda:foo", "<person><name>James</name></person>");

        assertMockEndpointsSatisfied();
    }

    public void testXPathTwoMessagesNotSameTime() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:other").expectedMessageCount(1);

        template.sendBody("seda:foo", "<person><name>Claus</name></person>");

        Thread.sleep(1000);

        template.sendBody("seda:foo", "<person><name>James</name></person>");

        assertMockEndpointsSatisfied();
    }

    public void testNoConcurrent() throws Exception {
        doSendMessages(1, 1);
    }

    public void testConcurrent() throws Exception {
        doSendMessages(10, 5);
    }

    private void doSendMessages(int files, int poolSize) throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(files);
        getMockEndpoint("mock:result").assertNoDuplicates(body());
        getMockEndpoint("mock:other").expectedMessageCount(0);

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        for (int i = 0; i < files; i++) {
            final int index = i;
            executor.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    template.sendBody("seda:foo", "<person><id>" + index + "</id><name>Claus</name></person>");
                    return null;
                }
            });
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:foo?concurrentConsumers=10")
                    .choice()
                        .when().xpath("/person/name = 'Claus'")
                            .to("mock:result")
                        .otherwise()
                            .to("mock:other")
                    .end();

            }
        };
    }
}
