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
package org.apache.camel.component.mina;

import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;

public class MinaConcurrencyTest extends ContextTestSupport {

    protected String uri = "mina:tcp://localhost:9123";
    protected CopyOnWriteArraySet<Long> threadSet = new CopyOnWriteArraySet<Long>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConcurrentInOnly() throws Exception {
        int count = 20;
        MockEndpoint endpoint = getMockEndpoint("mock:result");
        endpoint.expectedMessageCount(count);
        threadSet.clear();

        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = createSenderThread();
            threads[i].start();
        }
        for (int i = 0; i < count; i++) {
            threads[i].join();
        }

        assertTrue("Clients should not wait for processing", endpoint.getReceivedCounter() <= count);

        /*Thread.sleep(1000);
        for (int i = 0; i < count; i++) {
            threads[i] = createSenderThread();
            threads[i].start();
        }
        for (int i = 0; i < count; i++) {
            threads[i].join();
        }*/
        Thread.sleep(1000);
        assertMockEndpointsSatisfied();
        if (Runtime.getRuntime().availableProcessors() > 1) {
            // only expect this on a multiple core system
            assertTrue("Expected processing in more than one thread", threadSet.size() > 1);
        }
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(uri).process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        // some very slooow processing
                        Thread.sleep(1000);
                        threadSet.add(Thread.currentThread().getId());
                    }
                }).to("mock:result");
            }
        };
    }

    protected Thread createSenderThread() {
        return new Thread("Sender") {
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // do nothing
                }
                ProducerTemplate<Exchange> t = new DefaultProducerTemplate<Exchange>(context);
                t.sendBody(uri, ExchangePattern.InOnly, "Hello World!");
            }
        };
    }
}
