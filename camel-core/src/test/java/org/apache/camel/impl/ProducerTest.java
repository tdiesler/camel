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
package org.apache.camel.impl;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.TestSupport;

/**
 * @version $Revision$
 */
public class ProducerTest extends TestSupport {
    protected CamelContext context = new DefaultCamelContext();
    protected ExchangePattern pattern = ExchangePattern.InOnly;

    public void testUsingADerivedExchange() throws Exception {
        DefaultEndpoint endpoint = new DefaultEndpoint("foo", new DefaultComponent() {
            @Override
            protected Endpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
                return null;
            }

        }) {
            public Consumer createConsumer(Processor processor) throws Exception {
                return null;
            }

            public Exchange createExchange(ExchangePattern pattern) {
                return new MyExchange(getCamelContext(), pattern);
            }

            public Producer createProducer() throws Exception {
                return null;
            }

            public boolean isSingleton() {
                return false;
            }
        };

        DefaultProducer producer = new DefaultProducer(endpoint) {
            public void process(Exchange exchange) throws Exception {
                log.debug("Received: " + exchange);
            }
        };

        // now lets try send in a normal exchange
        Exchange exchange = new DefaultExchange(context);
        producer.process(exchange);

        // TODO: fix me
        // Class<?> type = endpoint.getExchangeType();
        // assertEquals("exchange type", MyExchange.class, type);

        MyExchange actual = (MyExchange) endpoint.createExchange(exchange);
        assertNotNull(actual);
        assertTrue("Not same exchange", actual != exchange);

        MyExchange expected = new MyExchange(context, pattern);
        actual = (MyExchange) endpoint.createExchange(expected);

        // TODO: fix me
        // assertSame("Should not copy an exchange when of the correct type", expected, actual);
    }
}
