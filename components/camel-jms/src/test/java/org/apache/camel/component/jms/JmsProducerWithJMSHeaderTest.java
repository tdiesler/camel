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

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;

/**
 * @version $Revision$
 */
public class JmsProducerWithJMSHeaderTest extends ContextTestSupport {

    @Test
    public void testInOnlyJMSPrioritory() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSPriority").isEqualTo(2);

        template.sendBodyAndHeader("activemq:queue:foo?preserveMessageQos=true", "Hello World", "JMSPriority", "2");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSPrioritoryTheDeliveryModeIsDefault() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSPriority").isEqualTo(2);
        // not provided as header but should use endpoint default then
        mock.message(0).header("JMSDeliveryMode").isEqualTo(2);

        template.sendBodyAndHeader("activemq:queue:foo?preserveMessageQos=true", "Hello World", "JMSPriority", "2");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSDeliveryMode() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSDeliveryMode").isEqualTo(1);

        template.sendBodyAndHeader("activemq:queue:foo?preserveMessageQos=true", "Hello World", "JMSDeliveryMode", "1");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSDeliveryModeThenPriorityIsDefault() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSDeliveryMode").isEqualTo(1);
        // not provided as header but should use endpoint default then
        mock.message(0).header("JMSPriority").isEqualTo(4);

        template.sendBodyAndHeader("activemq:queue:foo?preserveMessageQos=true", "Hello World", "JMSDeliveryMode", "1");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSExpiration() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        long ttl = System.currentTimeMillis() + 2000;
        template.sendBodyAndHeader("activemq:queue:bar?preserveMessageQos=true", "Hello World", "JMSExpiration", ttl);

        // sleep just a little
        Thread.sleep(500);

        Exchange bar = consumer.receiveNoWait("activemq:queue:bar");
        assertNotNull("Should be a message on queue", bar);

        template.send("activemq:queue:foo", bar);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSExpirationNoMessage() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        long ttl = System.currentTimeMillis() + 2000;
        template.sendBodyAndHeader("activemq:queue:bar?preserveMessageQos=true", "Hello World", "JMSExpiration", ttl);

        // sleep more so the message is expired
        Thread.sleep(3000);

        Exchange bar = consumer.receiveNoWait("activemq:queue:bar");
        assertNull("Should NOT be a message on queue", bar);

        template.sendBody("activemq:queue:foo", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyMultipleJMSHeaders() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSPriority").isEqualTo(3);
        mock.message(0).header("JMSDeliveryMode").isEqualTo(2);

        Map headers = new HashMap();
        headers.put("JMSPriority", 3);
        headers.put("JMSDeliveryMode", 2);
        template.sendBodyAndHeaders("activemq:queue:foo?preserveMessageQos=true", "Hello World", headers);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyMultipleJMSHeadersAndExpiration() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSPriority").isEqualTo(3);
        mock.message(0).header("JMSDeliveryMode").isEqualTo(2);

        long ttl = System.currentTimeMillis() + 2000;
        Map headers = new HashMap();
        headers.put("JMSPriority", 3);
        headers.put("JMSDeliveryMode", 2);
        headers.put("JMSExpiration", ttl);
        template.sendBodyAndHeaders("activemq:queue:bar?preserveMessageQos=true", "Hello World", headers);

        // sleep just a little
        Thread.sleep(500);

        Exchange bar = consumer.receive("activemq:queue:bar");
        assertNotNull("Should be a message on queue", bar);

        template.send("activemq:queue:foo?preserveMessageQos=true", bar);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyMultipleJMSHeadersAndExpirationNoMessage() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        long ttl = System.currentTimeMillis() + 2000;
        Map headers = new HashMap();
        headers.put("JMSPriority", 3);
        headers.put("JMSDeliveryMode", 2);
        headers.put("JMSExpiration", ttl);
        template.sendBodyAndHeaders("activemq:queue:bar?preserveMessageQos=true", "Hello World", headers);

        // sleep more so the message is expired
        Thread.sleep(3000);

        Exchange bar = consumer.receiveNoWait("activemq:queue:bar");
        assertNull("Should NOT be a message on queue", bar);

        template.sendBody("activemq:queue:foo", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSXGroupID() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSXGroupID").isEqualTo("atom");

        template.sendBodyAndHeader("activemq:queue:foo", "Hello World", "JMSXGroupID", "atom");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInOnlyJMSDestination() throws Exception {
        Destination queue = new ActiveMQQueue("foo");
        
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).header("JMSDestination").isNotNull();

        // must use a property for JMSDestination to send it somewhere else
        template.sendBodyAndProperty("activemq:queue:bar", "Hello World", "JMSDestination", queue);

        assertMockEndpointsSatisfied();

        assertEquals("queue://foo", mock.getReceivedExchanges().get(0).getIn().getHeader("JMSDestination", Destination.class).toString());
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        camelContext.addComponent("activemq", jmsComponentClientAcknowledge(connectionFactory));

        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:foo").to("mock:result");
            }
        };
    }
}
