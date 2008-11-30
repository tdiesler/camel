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
package org.apache.camel.itest.jms;

import javax.jms.ConnectionFactory;
import javax.naming.Context;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.util.jndi.JndiContext;

/**
 * Based on user forum.
 *
 * @version $Revision$
 */
public class JmsHttpJmsTest extends ContextTestSupport {

    public void testJmsHttpJms() throws Exception {
        template.sendBody("jms:in", "Hello World");

        Endpoint endpoint = context.getEndpoint("jms:out");
        endpoint.createConsumer(new Processor() {
            public void process(Exchange exchange) throws Exception {
                assertEquals("Bye World", exchange.getIn().getBody(String.class));
            }
        });
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("jms:in").to("http://localhost/myservice").to("jms:out");

                from("jetty:http://localhost/myservice").transform().constant("Bye World");
            }
        };
    }

    @Override
    protected Context createJndiContext() throws Exception {
        JndiContext answer = new JndiContext();

        // add ActiveMQ with embedded broker
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        answer.bind("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        return answer;
    }

}