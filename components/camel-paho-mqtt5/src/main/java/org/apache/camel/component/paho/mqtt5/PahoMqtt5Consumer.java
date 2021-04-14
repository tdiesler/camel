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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PahoMqtt5Consumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMqtt5Consumer.class);

    public PahoMqtt5Consumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        String topic = getEndpoint().getTopic();
        getEndpoint().getClient().subscribe(topic, getEndpoint().getQos());
        getEndpoint().getClient().setCallback(new MqttCallback() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    try {
                        getEndpoint().getClient().subscribe(topic, getEndpoint().getQos());
                    } catch (MqttException e) {
                        LOG.error("MQTT resubscribe failed {}", e.getMessage(), e);
                    }
                }
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
                LOG.debug("Auth packet arrived {} {}", reasonCode, properties);
            }

            @Override
            public void disconnected(MqttDisconnectResponse response) {
                LOG.debug("MQTT broker disconnected due {}", response.getReasonString(), response.getException());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                LOG.debug("Error occurred {}", exception.getMessage(), exception);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LOG.debug("Message arrived on topic: {} -> {}", topic, message);
                Exchange exchange = getEndpoint().createExchange(message, topic);

                getAsyncProcessor().process(exchange, doneSync -> {
                    // noop
                });
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                LOG.debug("Delivery complete. Token: {}", token);
            }
        });
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        MqttClient client = getEndpoint().getClient();
        if (client != null && client.isConnected()) {
            if (getEndpoint().getConnectionOptions().isCleanStart()) {
                String topic = getEndpoint().getTopic();
                client.unsubscribe(topic);
            }
        }
    }

    @Override
    public PahoMqtt5Endpoint getEndpoint() {
        return (PahoMqtt5Endpoint) super.getEndpoint();
    }

}
