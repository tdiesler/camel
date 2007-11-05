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

import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * @version $Revision$
 */
public class ConsumeJmsMapMessageTest extends ContextTestSupport {
    protected JmsTemplate jmsTemplate;

    public void testConsumeMapMessage() throws Exception {
        MockEndpoint endpoint = getMockEndpoint("mock:result");
        endpoint.expectedMessageCount(1);

        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.send("test.map", new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("foo", "abc");
                mapMessage.setString("bar", "xyz");
                return mapMessage;
            }
        });

        endpoint.assertIsSatisfied();
        Exchange exchange = endpoint.getReceivedExchanges().get(0);
        Map map = exchange.getIn().getBody(Map.class);

        log.info("Received map: " + map);

        assertNotNull("Should have received a map message!", map);
        assertEquals("map.foo", "abc", map.get("foo"));
        assertEquals("map.bar", "xyz", map.get("bar"));
        assertEquals("map.size", 2, map.size());
    }


    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        jmsTemplate = new JmsTemplate(connectionFactory);
        camelContext.addComponent("activemq", jmsComponentClientAcknowledge(connectionFactory));

        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("activemq:test.map").to("mock:result");
            }
        };
    }
}