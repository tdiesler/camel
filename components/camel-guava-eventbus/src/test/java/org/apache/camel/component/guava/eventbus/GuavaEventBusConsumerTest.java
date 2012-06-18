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
package org.apache.camel.component.guava.eventbus;

import com.google.common.eventbus.EventBus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class GuavaEventBusConsumerTest extends CamelTestSupport {

    EventBus eventBus = new EventBus();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("guava-eventbus:eventBus").to("mock:allEvents");

                from("guava-eventbus:eventBus?eventClass=org.apache.camel.component.guava.eventbus.MessageWrapper").
                        to("mock:wrapperEvents");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("eventBus", eventBus);
        return registry;
    }

    @Test
    public void shouldForwardMessageToCamel() throws InterruptedException {
        // Given
        String message = "message";

        // When
        eventBus.post(message);

        // Then
        getMockEndpoint("mock:allEvents").setExpectedMessageCount(1);
        assertMockEndpointsSatisfied();
        assertEquals(message, getMockEndpoint("mock:allEvents").getExchanges().get(0).getIn().getBody());
    }

    @Test
    public void shouldFilterForwardedMessages() throws InterruptedException {
        // Given
        MessageWrapper wrappedMessage = new MessageWrapper("message");

        // When
        eventBus.post(wrappedMessage);
        eventBus.post("String message.");

        // Then
        getMockEndpoint("mock:wrapperEvents").setExpectedMessageCount(1);
        assertMockEndpointsSatisfied();
        assertEquals(wrappedMessage, getMockEndpoint("mock:wrapperEvents").getExchanges().get(0).getIn().getBody());
    }

}
