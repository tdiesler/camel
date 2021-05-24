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
package org.apache.camel.processor.idempotent.kafka;

import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.BaseEmbeddedKafkaTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * Test whether the KafkaIdempotentRepository successfully recreates its cache from pre-existing topics. This guarantees
 * that the de-duplication state survives application instance restarts.
 *
 * This test requires running in a certain order (which isn't great for unit testing), hence the ordering-related
 * annotations.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaIdempotentRepositoryPersistenceTest extends BaseEmbeddedKafkaTest {



    @EndpointInject(uri = "mock:out")
    private MockEndpoint mockOut;

    @EndpointInject(uri = "mock:before")
    private MockEndpoint mockBefore;

    // Every instance of the repository must use a different topic to guarantee isolation between tests
    private KafkaIdempotentRepository kafkaIdempotentRepository
            = new KafkaIdempotentRepository("TEST_PERSISTENCE", kafkaBroker.getBrokerList());


    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("kafkaIdempotentRepository", kafkaIdempotentRepository);
        return jndi;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:in").to("mock:before").idempotentConsumer(header("id"))
                        .messageIdRepositoryRef("kafkaIdempotentRepository").to("mock:out").end();
            }
        };
    }

    @Test
    public void testFirstPassFiltersAsExpected() throws InterruptedException {
        Object result;
        for (int i = 0; i < 10; i++) {
            result = template.sendBodyAndHeader("direct:in", ExchangePattern.InOut, "Test message", "id", i % 5);
        }

        // all records sent initially
        assertEquals(10, mockBefore.getReceivedCounter());

        // filters second attempt with same value
        assertEquals(5, kafkaIdempotentRepository.getDuplicateCount());

        // only first 1-4 records are received, the rest are filtered
        assertEquals(5, mockOut.getReceivedCounter());
    }

    @Test
    public void testSecondPassFiltersEverything() throws InterruptedException {

        Object result;
        for (int i = 0; i < 10; i++) {
            result = template.sendBodyAndHeader("direct:in", ExchangePattern.InOut, "Test message", "id", i % 5);
        }

        // all records sent initially
        assertEquals(10, mockBefore.getReceivedCounter());

        // the state from the previous test guarantees that all attempts now are blocked
        assertEquals(10, kafkaIdempotentRepository.getDuplicateCount());

        // nothing gets passed the idempotent consumer this time
        assertEquals(0, mockOut.getReceivedCounter());
    }
}