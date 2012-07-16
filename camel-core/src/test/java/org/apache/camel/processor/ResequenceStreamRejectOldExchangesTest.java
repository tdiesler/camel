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
import org.apache.camel.processor.resequencer.MessageRejectedException;

/**
 *
 */
public class ResequenceStreamRejectOldExchangesTest extends ContextTestSupport {

    public void testInSequenceAfterTimeout() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("A", "B", "C", "E");
        getMockEndpoint("mock:error").expectedMessageCount(0);

        template.sendBodyAndHeader("direct:start", "B", "seqno", 2);
        template.sendBodyAndHeader("direct:start", "C", "seqno", 3);
        template.sendBodyAndHeader("direct:start", "A", "seqno", 1);
        Thread.sleep(1100);
        template.sendBodyAndHeader("direct:start", "E", "seqno", 5);

        assertMockEndpointsSatisfied();
    }

    public void testDuplicateAfterTimeout() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("A", "B", "C");
        getMockEndpoint("mock:error").expectedMessageCount(0);

        template.sendBodyAndHeader("direct:start", "B", "seqno", 2);
        template.sendBodyAndHeader("direct:start", "C", "seqno", 3);
        template.sendBodyAndHeader("direct:start", "A", "seqno", 1);
        Thread.sleep(1100);
        template.sendBodyAndHeader("direct:start", "C", "seqno", 3);

        assertMockEndpointsSatisfied();
    }

    public void testOutOfSequenceAfterTimeout() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("A", "C", "D");
        getMockEndpoint("mock:error").expectedBodiesReceived("B");

        template.sendBodyAndHeader("direct:start", "D", "seqno", 4);
        template.sendBodyAndHeader("direct:start", "C", "seqno", 3);
        template.sendBodyAndHeader("direct:start", "A", "seqno", 1);
        Thread.sleep(1100);
        template.sendBodyAndHeader("direct:start", "B", "seqno", 2);

        assertMockEndpointsSatisfied();
    }

    public void testOutOfSequenceAfterTimeout2() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("B", "C", "D");
        getMockEndpoint("mock:error").expectedBodiesReceived("A");

        template.sendBodyAndHeader("direct:start", "D", "seqno", 4);
        template.sendBodyAndHeader("direct:start", "C", "seqno", 3);
        template.sendBodyAndHeader("direct:start", "B", "seqno", 2);
        Thread.sleep(1100);
        template.sendBodyAndHeader("direct:start", "A", "seqno", 1);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:start")
                        .onException(MessageRejectedException.class).handled(true).to("mock:error").end()
                        .resequence(header("seqno")).stream().timeout(1000).rejectOld()
                        .to("mock:result");
            }
        };
    }
}
