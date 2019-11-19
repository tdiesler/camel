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
package org.apache.camel.component.pulsar;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.pulsar.configuration.PulsarConfiguration;
import org.apache.camel.component.pulsar.utils.AutoConfiguration;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.spi.Metadata;
import org.apache.pulsar.client.api.PulsarClient;

public class PulsarComponent extends DefaultComponent {

    @Metadata
    private AutoConfiguration autoConfiguration;
    @Metadata
    private PulsarClient pulsarClient;
    @Metadata(label = "consumer", defaultValue = "false")
    private boolean allowManualAcknowledgement;
    @Metadata(label = "consumer,advanced")
    private PulsarMessageReceiptFactory pulsarMessageReceiptFactory = new DefaultPulsarMessageReceiptFactory();

    public PulsarComponent() {
        this(null);
    }

    public PulsarComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String path, final Map<String, Object> parameters) throws Exception {
        final PulsarConfiguration configuration = new PulsarConfiguration();

        configuration.setAllowManualAcknowledgement(isAllowManualAcknowledgement());

        setProperties(configuration, parameters);
        if (autoConfiguration != null) {
            setProperties(autoConfiguration, parameters);

            if (autoConfiguration.isAutoConfigurable()) {
                autoConfiguration.ensureNameSpaceAndTenant(path);
            }
        }

        return PulsarEndpoint.create(uri, path, configuration, this, pulsarClient);
    }

    public AutoConfiguration getAutoConfiguration() {
        return autoConfiguration;
    }

    /**
     * The pulsar autoconfiguration
     */
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

    public PulsarClient getPulsarClient() {
        return pulsarClient;
    }

    /**
     * The pulsar client
     */
    public void setPulsarClient(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    public boolean isAllowManualAcknowledgement() {
        return allowManualAcknowledgement;
    }

    /**
     * Whether to allow manual message acknowledgements.
     * <p/>
     * If this option is enabled, then messages are not immediately acknowledged after being consumed.
     * Instead, an instance of {@link PulsarMessageReceipt} is stored as a header on the {@link org.apache.camel.Exchange}.
     * Messages can then be acknowledged using {@link PulsarMessageReceipt} at any time before the ackTimeout occurs.
     */
    public void setAllowManualAcknowledgement(boolean allowManualAcknowledgement) {
        this.allowManualAcknowledgement = allowManualAcknowledgement;
    }

    public PulsarMessageReceiptFactory getPulsarMessageReceiptFactory() {
        return pulsarMessageReceiptFactory;
    }

    /**
     * Provide a factory to create an alternate implementation of {@link PulsarMessageReceipt}.
     */
    public void setPulsarMessageReceiptFactory(PulsarMessageReceiptFactory pulsarMessageReceiptFactory) {
        this.pulsarMessageReceiptFactory = pulsarMessageReceiptFactory;
    }
}
