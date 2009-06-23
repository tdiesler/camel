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
package org.apache.camel.component.jms;

import java.util.concurrent.Executors;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;

/**
 * @version $Revision$
 */
public class JmsPollingConsumerTest extends ContextTestSupport {

    public void testJmsPollingConsumer() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello Claus");

        // use another thread for polling consumer to demonstrate that we can wait before
        // the message is sent to the quueue
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                try {
                    PollingConsumer consumer = context.getEndpoint("activemq:queue.start").createPollingConsumer();
                    consumer.start();
                    Exchange exchange = consumer.receive();
                    String body = exchange.getIn().getBody(String.class);
                    template.sendBody("activemq:queue.foo", body + " Claus");
                    consumer.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // wait a little to demonstrate we can start poll before we have a msg on the queue
        Thread.sleep(50);

        template.sendBody("direct:start", "Hello");

        assertMockEndpointsSatisfied();
    }


    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        camelContext.addComponent("activemq", jmsComponentClientAcknowledge(connectionFactory));

        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("activemq:queue.start");

                from("activemq:queue.foo").to("mock:result");
            }
        };
    }

}
