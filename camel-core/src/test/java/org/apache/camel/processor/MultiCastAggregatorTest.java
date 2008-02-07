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
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class MultiCastAggregatorTest extends ContextTestSupport {
    protected Endpoint<Exchange> startEndpoint;
    protected MockEndpoint result;


    public void testSendingAMessageUsingMulticastReceivesItsOwnExchange() throws Exception {
        result.expectedBodiesReceived("inputx+inputy+inputz");

        Exchange result = template.send("direct:a", new Processor() {
            public void process(Exchange exchange) {
                Message in = exchange.getIn();
                in.setBody("input");
                in.setHeader("foo", "bar");
            }
        });
        assertNotNull("We should get result here", result);
        assertEquals("Can't get the right result", "inputx+inputy+inputz", result.getOut().getBody(String.class));

        assertMockEndpointsSatisifed();
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        result = getMockEndpoint("mock:result");


    }

    class AppendingProcessor implements Processor {
        String appendingString;

        AppendingProcessor(String string) {
            appendingString = string;
        }


       public void process(Exchange exchange) {
                // lets transform the IN message
                Message in = exchange.getIn();
                String body = in.getBody(String.class);
                in.setBody(body + appendingString);
       }


    }

    class BodyOutAggregatingStrategy implements AggregationStrategy {
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            Message newOut = newExchange.getOut();
            String oldBody = oldExchange.getOut().getBody(String.class);
            String newBody = newOut.getBody(String.class);
            newOut.setBody(oldBody + "+" + newBody);
            return newExchange;
        }
    }


    class BodyInAggregatingStrategy implements AggregationStrategy {

        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            Message newIn = newExchange.getIn();
            String oldBody = oldExchange.getIn().getBody(String.class);
            String newBody = newIn.getBody(String.class);
            newIn.setBody(oldBody + "+" + newBody);
            Integer old = (Integer) oldExchange.getProperty("aggregated");
            if (old == null) {
                old = 1;
            }
            newExchange.setProperty("aggregated", old + 1);
            return newExchange;
        }

        /**
         * An expression used to determine if the aggreagation is complete
         */
        public boolean isCompleted(@Header(name="aggregated") Integer aggregated) {
            if (aggregated == null) {
                return false;
            }
            return aggregated == 3;
        }

    }

    protected RouteBuilder createRouteBuilder() {

        return new RouteBuilder() {
            public void configure() {
                from("direct:a").multicast(new BodyOutAggregatingStrategy()).to("direct:x", "direct:y", "direct:z");

                from("direct:x").process(new AppendingProcessor("x")).to("direct:aggregater");
                from("direct:y").process(new AppendingProcessor("y")).to("direct:aggregater");
                from("direct:z").process(new AppendingProcessor("z")).to("direct:aggregater");

                from("direct:aggregater").aggregator(header("cheese"), new BodyInAggregatingStrategy()).
                completedPredicate(header("aggregated").isEqualTo(3)).to("mock:result");
            }
        };

    }


}
