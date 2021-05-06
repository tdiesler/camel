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
package org.apache.camel.component.paho.mqtt5;

import java.nio.charset.StandardCharsets;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.Test;

public class PahoMqtt5ComponentMqtt5Test extends PahoMqtt5TestSupport {

    MqttConnectionOptions connectionOptions = new MqttConnectionOptions();

    @EndpointInject(uri = "mock:test")
    MockEndpoint mock;

    @EndpointInject(uri = "mock:testCustomizedPaho")
    MockEndpoint testCustomizedPahoMock;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                PahoMqtt5Component customizedPaho = new PahoMqtt5Component();
                context.addComponent("customizedPaho", customizedPaho);

                from("direct:test").to("paho-mqtt5:queue?brokerUrl=tcp://localhost:" + mqttPort);
                from("paho-mqtt5:queue?brokerUrl=tcp://localhost:" + mqttPort).to("mock:test");

                from("direct:test2").to("paho-mqtt5:queue?brokerUrl=tcp://localhost:" + mqttPort);

                from("paho-mqtt5:persistenceTest?persistence=FILE&brokerUrl=tcp://localhost:" + mqttPort)
                        .to("mock:persistenceTest");

                from("direct:connectionOptions").to("paho-mqtt5:registryConnectionOptions?connectionOptions=#connectionOptions&brokerUrl=tcp://localhost:" + mqttPort);

                from("direct:testCustomizedPaho").to("customizedPaho:testCustomizedPaho?brokerUrl=tcp://localhost:" + mqttPort);
                from("paho-mqtt5:testCustomizedPaho?brokerUrl=tcp://localhost:" + mqttPort).to("mock:testCustomizedPaho");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("connectionOptions", connectionOptions);
        return registry;
    }

    // Tests

    @Test
    public void checkOptions() {
        String uri = "paho-mqtt5:/test/topic" + "?clientId=sampleClient" + "&brokerUrl=tcp://localhost:" + mqttPort + "&qos=2"
                     + "&persistence=file";

        PahoMqtt5Endpoint endpoint = getMandatoryEndpoint(uri, PahoMqtt5Endpoint.class);

        // Then
        assertEquals("/test/topic", endpoint.getTopic());
        assertEquals("sampleClient", endpoint.getClientId());
        assertEquals("tcp://localhost:" + mqttPort, endpoint.getBrokerUrl());
        assertEquals(2, endpoint.getQos());
        assertEquals(PahoMqtt5Persistence.FILE, endpoint.getPersistence());
    }

    @Test
    public void shouldReadMessageFromMqtt() throws InterruptedException {
        // Given
        String msg = "msg";
        mock.expectedBodiesReceived(msg);

        // When
        template.sendBody("direct:test", msg);

        // Then
        mock.assertIsSatisfied();
    }

    @Test
    public void shouldNotReadMessageFromUnregisteredTopic() throws InterruptedException {
        // Given
        mock.expectedMessageCount(0);

        // When
        template.sendBody("paho-mqtt5:someRandomQueue?brokerUrl=tcp://localhost:" + mqttPort, "msg");

        // Then
        mock.assertIsSatisfied();
    }

    @Test
    public void shouldUseConnectionOptionsFromRegistry() {
        // Given
        PahoMqtt5Endpoint pahoWithConnectionOptionsFromRegistry = getMandatoryEndpoint(
            "paho-mqtt5:registryConnectionOptions?connectionOptions=#connectionOptions&brokerUrl=tcp://localhost:" + mqttPort,
            PahoMqtt5Endpoint.class);

        // Then
        assertSame(connectionOptions, pahoWithConnectionOptionsFromRegistry.resolveMqttConnectOptions());
    }

    @Test
    public void shouldAutomaticallyUseConnectionOptionsFromRegistry() {
        // Given
        PahoMqtt5Endpoint pahoWithConnectionOptionsFromRegistry = getMandatoryEndpoint(
            "paho-mqtt5:registryConnectionOptions?brokerUrl=tcp://localhost:" + mqttPort,
            PahoMqtt5Endpoint.class);

        // Then
        assertSame(connectionOptions, pahoWithConnectionOptionsFromRegistry.resolveMqttConnectOptions());
    }

    @Test
    public void shouldKeepDefaultMessageInHeader() throws InterruptedException {
        // Given
        final String msg = "msg";
        mock.expectedBodiesReceived(msg);

        // When
        template.sendBody("direct:test", msg);

        // Then
        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        String payload = new String((byte[]) exchange.getIn().getBody(), StandardCharsets.UTF_8);

        assertEquals("queue", exchange.getIn().getHeader(PahoMqtt5Constants.MQTT_TOPIC));
        assertEquals(msg, payload);
    }

    @Test
    public void shouldKeepOriginalMessageInHeader() throws InterruptedException {
        // Given
        final String msg = "msg";
        mock.expectedBodiesReceived(msg);

        // When
        template.sendBody("direct:test2", msg);

        // Then
        mock.assertIsSatisfied();
        Exchange exchange = mock.getExchanges().get(0);

        MqttMessage message = exchange.getIn(PahoMqtt5Message.class).getMqttMessage();
        assertNotNull(message);
        assertEquals(msg, new String(message.getPayload()));
    }

    @Test
    public void shouldReadMessageFromCustomizedComponent() throws InterruptedException {
        // Given
        String msg = "msg";
        testCustomizedPahoMock.expectedBodiesReceived(msg);

        // When
        template.sendBody("direct:testCustomizedPaho", msg);

        // Then
        testCustomizedPahoMock.assertIsSatisfied();
    }

    @Test
    public void shouldNotSendMessageAuthIsNotValid() throws InterruptedException {
        // Given
        mock.expectedMessageCount(0);

        // When
        template.sendBody("paho-mqtt5:someRandomQueue?brokerUrl=tcp://localhost:" + mqttPort + "&userName=test&password=test",
                "msg");

        // Then
        mock.assertIsSatisfied();
    }

}
