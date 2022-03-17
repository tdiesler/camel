/*
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
package org.apache.camel.component.netty4.http;

import io.netty.util.ResourceLeakDetector;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.junit.Test;

public class NettyEnricherLeakTest extends BaseNettyTest{

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Test
    public void leakNoTest() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

                from("netty4-http:http://localhost:" + getPort() + "/test")
                        .transform().simple("${body}");

                from("direct:outer")
                        .to("netty4-http:http://localhost:" + getPort() + "/test?disconnect=true");
            }
        });
        context.start();

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        for (int i = 0; i < 10; ++i) {
            template.requestBody("direct:outer", "input", String.class);
        }
    }

    @Test
    public void leakTest() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

                from("netty4-http:http://localhost:" + getPort() + "/test")
                        .transform().simple("${body}");

                from("direct:outer")
                        .enrich("netty4-http:http://localhost:" + getPort() + "/test?disconnect=true",
                                new StringAggregationStrategy(), false, false);
            }
        });
        context.start();

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        for (int i = 0; i < 10; ++i) {
            template.requestBody("direct:outer", "input", String.class);
        }
    }

    class StringAggregationStrategy implements AggregationStrategy {

        private String delimiter = "";
        private Expression pickExpression = ExpressionBuilder.bodyExpression();

        /**
         * Set delimiter used for joining aggregated String
         *
         * @param delimiter The delimiter to join with. Default empty String
         */
        public StringAggregationStrategy delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Set an expression to extract the element to be aggregated from the incoming {@link Exchange}.
         * <p/>
         * By default, it picks the full IN message body of the incoming exchange.
         *
         * @param  expression The picking expression.
         * @return            This instance.
         */
        public StringAggregationStrategy pick(Expression expression) {
            this.pickExpression = expression;
            return this;
        }

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            StringBuffer result; // Aggregate in StringBuffer instead of StringBuilder, to make it thread safe
            StringBuilder value = new StringBuilder();
            if (oldExchange == null) {
                result = getStringBuffer(newExchange); // do not prepend delimiter for first invocation
            } else {
                result = getStringBuffer(oldExchange);
                value.append(delimiter);
            }

            if (newExchange != null) {
                String pick = pickExpression.evaluate(newExchange, String.class);
                if (pick != null) {
                    value.append(pick);
                    result.append(value);
                }
            }

            return oldExchange != null ? oldExchange : newExchange;
        }

        public void onCompletion(Exchange exchange) {
            if (exchange != null) {
                StringBuffer stringBuffer = (StringBuffer) exchange.removeProperty(Exchange.GROUPED_EXCHANGE);
                if (stringBuffer != null) {
                    exchange.getIn().setBody(stringBuffer.toString());
                }
            }
        }

        private  StringBuffer getStringBuffer(Exchange exchange) {
            StringBuffer stringBuffer = exchange.getProperty(Exchange.GROUPED_EXCHANGE, StringBuffer.class);
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
                exchange.setProperty(Exchange.GROUPED_EXCHANGE, stringBuffer);
            }
            return stringBuffer;
        }
    }
}
