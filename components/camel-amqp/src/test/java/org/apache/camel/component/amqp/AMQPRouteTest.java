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
package org.apache.camel.component.amqp;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.apache.camel.component.amqp.AMQPComponent.amqpComponent;

public class AMQPRouteTest extends CamelTestSupport {

    static BrokerService broker;

    @EndpointInject(uri = "mock:result")
    MockEndpoint resultEndpoint;

    String expectedBody = "Hello there!";

    @BeforeClass
    public static void beforeClass() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("amqp://0.0.0.0:5672");
        broker.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        broker.stop();
    }

    @Test
    public void testJmsQueue() throws Exception {
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.message(0).header("cheese").isEqualTo(123);
        template.sendBodyAndHeader("amqp1-0:queue:ping", expectedBody, "cheese", 123);
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testRequestReply() {
        String response = template.requestBody("amqp1-0:queue:inOut", expectedBody, String.class);
        assertEquals("response", response);
    }

    @Test
    public void testJmsTopic() throws Exception {
        resultEndpoint.expectedMessageCount(2);
        resultEndpoint.message(0).header("cheese").isEqualTo(123);
        template.sendBodyAndHeader("amqp1-0:topic:ping", expectedBody, "cheese", 123);
        resultEndpoint.assertIsSatisfied();
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        camelContext.addComponent("amqp1-0", amqpComponent("amqp://localhost:5672"));
        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("amqp1-0:queue:ping")
                    .to("log:routing")
                    .to("mock:result");

                from("amqp1-0:queue:inOut")
                        .setBody().constant("response");

                from("amqp1-0:topic:ping")
                        .to("log:routing")
                        .to("mock:result");

                from("amqp1-0:topic:ping")
                        .to("log:routing")
                        .to("mock:result");
            }
        };
    }

}
