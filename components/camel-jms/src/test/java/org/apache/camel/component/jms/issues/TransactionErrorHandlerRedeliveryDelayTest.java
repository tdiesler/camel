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
package org.apache.camel.component.jms.issues;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * Unit test for issue CAMEL-706
 */
@ContextConfiguration
public class TransactionErrorHandlerRedeliveryDelayTest extends AbstractJUnit38SpringContextTests {

    private static int counter;

    @Autowired
    protected CamelContext context;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    public void testTransactedRedeliveryDelay() throws Exception {
        result.expectedMessageCount(1);
        result.expectedBodiesReceived("Bye World");

        long start = System.currentTimeMillis();
        context.createProducerTemplate().sendBody("activemq:queue:in", "Hello World");

        result.assertIsSatisfied();
        long delta = System.currentTimeMillis() - start;
        assertTrue("There should have been redelivery delay: delta=" + delta, delta > 6000L);
    }

    public static class MyFailureProcessor implements Processor {

        public MyFailureProcessor() {
        }

        public void process(Exchange exchange) throws Exception {
            if (counter++ < 3) {
                throw new IllegalArgumentException("Forced exception as counter is " + counter);
            }
            assertTrue("Should be transacted", exchange.isTransacted());
            exchange.getIn().setBody("Bye World");
        }
    }
}
