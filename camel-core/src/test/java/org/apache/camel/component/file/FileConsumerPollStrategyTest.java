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
package org.apache.camel.component.file;

import org.apache.camel.Consumer;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumerPollStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;

/**
 * Unit test for poll strategy
 */
public class FileConsumerPollStrategyTest extends ContextTestSupport {

    private static int counter;
    private static String event = "";

    private String fileUrl = "file://target/pollstrategy/?consumer.pollStrategy=#myPoll";

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("myPoll", new MyPollStrategy());
        return jndi;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteDirectory("target/pollstrategy");
        template.sendBodyAndHeader("file:target/pollstrategy/", "Hello World", Exchange.FILE_NAME, "hello.txt");
    }

    public void testFirstPollRollbackThenCommit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        // give poll strategy a bit time to signal commit
        Thread.sleep(50);

        assertEquals("rollbackcommit", event);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(fileUrl).to("mock:result");
            }
        };
    }

    private class MyPollStrategy implements PollingConsumerPollStrategy {

        public void begin(Consumer consumer, Endpoint endpoint) {
            if (counter++ == 0) {
                // simulate an error on first poll
                throw new IllegalArgumentException("Damn I cannot do this");
            }
        }

        public void commit(Consumer consumer, Endpoint endpoint) {
            event += "commit";
        }

        public void rollback(Consumer consumer, Endpoint endpoint, Exception cause) throws Exception {
            if (cause.getMessage().equals("Damn I cannot do this")) {
                event += "rollback";
            }
        }
    }

}