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

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;

public class JmsSelectorOptionTest extends CamelTestSupport {
    
    protected String componentName = "activemq";

    @Test
    public void testJmsMessageWithSelector() throws Exception {
        MockEndpoint endpointA = getMockEndpoint("mock:a");
        MockEndpoint endpointB = getMockEndpoint("mock:b");
        MockEndpoint endpointC = getMockEndpoint("mock:c");
        
        endpointA.expectedBodiesReceived("A blue car!", "A blue car, again!");
        endpointA.expectedHeaderReceived("color", "blue");
        endpointB.expectedHeaderReceived("color", "red");
        endpointB.expectedBodiesReceived("A red car!");
        
        endpointC.expectedBodiesReceived("Message1", "Message2");
        endpointC.expectedMessageCount(2);

        template.sendBodyAndHeader("activemq:queue:hello", "A blue car!", "color", "blue");
        template.sendBodyAndHeader("activemq:queue:hello", "A red car!", "color", "red");
        template.sendBodyAndHeader("activemq:queue:hello", "A blue car, again!", "color", "blue");
        template.sendBodyAndHeader("activemq:queue:hello", "Message1", "SIZE_NUMBER", 1505);
        template.sendBodyAndHeader("activemq:queue:hello", "Message3", "SIZE_NUMBER", 1300);
        template.sendBodyAndHeader("activemq:queue:hello", "Message2", "SIZE_NUMBER", 1600);
        assertMockEndpointsSatisfied();
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        camelContext.addComponent(componentName, jmsComponentClientAcknowledge(connectionFactory));

        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("activemq:queue:hello?selector=color='blue'").to("mock:a");
                from("activemq:queue:hello?selector=color='red'").to("mock:b");
                from("activemq:queue:hello?selector=SIZE_NUMBER>1500").to("mock:c");
            }
        };
    }

}
