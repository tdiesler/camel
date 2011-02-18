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

import java.io.File;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;

/**
 * @version 
 */
public class FileIdempotentTrunkStoreTest extends ContextTestSupport {
    protected Endpoint startEndpoint;
    protected MockEndpoint resultEndpoint;
    private File store = new File("target/idempotentfilestore.dat");
    private IdempotentRepository<String> repo;

    public void testTrunkFileStore() throws Exception {
        resultEndpoint.expectedBodiesReceived("A", "B", "C", "D", "E");

        sendMessage("AAAAAAAAAA", "A");
        sendMessage("BBBBBBBBBB", "B");
        sendMessage("CCCCCCCCCC", "C");
        sendMessage("AAAAAAAAAA", "A");
        sendMessage("DDDDDDDDDD", "D");
        sendMessage("BBBBBBBBBB", "B");
        sendMessage("EEEEEEEEEE", "E");

        resultEndpoint.assertIsSatisfied();
        resultEndpoint.reset();
        resultEndpoint.expectedBodiesReceived("Z", "X");

        // should trunk the file store
        sendMessage("ZZZZZZZZZZ", "Z");

        // load in new store and verify we only have the last 5 elements
        IdempotentRepository<String> repo2 = FileIdempotentRepository.fileIdempotentRepository(store);
        repo2.start();
        assertFalse(repo2.contains("AAAAAAAAAA"));
        assertTrue(repo2.contains("BBBBBBBBBB"));
        assertTrue(repo2.contains("CCCCCCCCCC"));
        assertTrue(repo2.contains("DDDDDDDDDD"));
        assertTrue(repo2.contains("EEEEEEEEEE"));
        assertTrue(repo2.contains("ZZZZZZZZZZ"));

        // should trunk the file store
        sendMessage("XXXXXXXXXX", "X");

        resultEndpoint.assertIsSatisfied();
        assertFalse(repo.contains("BBBBBBBBBB"));
        assertTrue(repo.contains("XXXXXXXXXX"));
    }

    protected void sendMessage(final Object messageId, final Object body) {
        template.send(startEndpoint, new Processor() {
            public void process(Exchange exchange) {
                // now lets fire in a message
                Message in = exchange.getIn();
                in.setBody(body);
                in.setHeader("messageId", messageId);
            }
        });
    }

    @Override
    protected void setUp() throws Exception {
        // delete file store before testing
        if (store.exists()) {
            store.delete();
        }

        // 5 elements in cache, and 50 bytes as max size limit for when trunking should start
        repo = FileIdempotentRepository.fileIdempotentRepository(store, 5, 50);
        repo.start();

        super.setUp();
        startEndpoint = resolveMandatoryEndpoint("direct:start");
        resultEndpoint = getMockEndpoint("mock:result");
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                    .idempotentConsumer(header("messageId"), repo)
                    .to("mock:result");
            }
        };
    }
}