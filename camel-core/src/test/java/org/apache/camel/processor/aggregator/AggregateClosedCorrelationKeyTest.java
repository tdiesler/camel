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
package org.apache.camel.processor.aggregator;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.BodyInAggregatingStrategy;
import org.apache.camel.processor.aggregate.ClosedCorrelationKeyException;

/**
 * @version $Revision$
 */
public class AggregateClosedCorrelationKeyTest extends ContextTestSupport {

    public void testAggregateClosedCorrelationKey() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("A+B");

        template.sendBodyAndHeader("direct:start", "A", "id", 1);
        template.sendBodyAndHeader("direct:start", "B", "id", 1);

        // should be closed
        try {
            template.sendBodyAndHeader("direct:start", "C", "id", 1);
            fail("Should throw an exception");
        } catch (CamelExecutionException e) {
            ClosedCorrelationKeyException cause = assertIsInstanceOf(ClosedCorrelationKeyException.class, e.getCause());
            assertEquals(1, cause.getCorrelationKey());
            assertEquals("The correlation key [1] has been closed. Exchange[Message: C]", cause.getMessage());
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .aggregate(header("id"), new BodyInAggregatingStrategy())
                        .completionSize(2).closeCorrelationKeyOnCompletion()
                        .to("mock:result");

            }
        };
    }
}
