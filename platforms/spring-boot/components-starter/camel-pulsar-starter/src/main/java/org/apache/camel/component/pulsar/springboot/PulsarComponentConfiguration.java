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
package org.apache.camel.component.pulsar.springboot;

import javax.annotation.Generated;
import org.apache.camel.component.pulsar.PulsarMessageReceiptFactory;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Camel Apache Pulsar Component
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.pulsar")
public class PulsarComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * The pulsar autoconfiguration
     */
    private AutoConfigurationNestedConfiguration autoConfiguration;
    /**
     * The pulsar client
     */
    @NestedConfigurationProperty
    private PulsarClient pulsarClient;
    /**
     * Whether to allow manual message acknowledgements. If this option is
     * enabled, then messages are not immediately acknowledged after being
     * consumed. Instead, an instance of PulsarMessageReceipt is stored as a
     * header on the org.apache.camel.Exchange. Messages can then be
     * acknowledged using PulsarMessageReceipt at any time before the ackTimeout
     * occurs.
     */
    private Boolean allowManualAcknowledgement = false;
    /**
     * Provide a factory to create an alternate implementation of
     * PulsarMessageReceipt.
     */
    @NestedConfigurationProperty
    private PulsarMessageReceiptFactory pulsarMessageReceiptFactory;
    /**
     * Whether the component should resolve property placeholders on itself when
     * starting. Only properties which are of String type can use property
     * placeholders.
     */
    private Boolean resolvePropertyPlaceholders = true;

    public AutoConfigurationNestedConfiguration getAutoConfiguration() {
        return autoConfiguration;
    }

    public void setAutoConfiguration(
            AutoConfigurationNestedConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

    public PulsarClient getPulsarClient() {
        return pulsarClient;
    }

    public void setPulsarClient(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    public Boolean getAllowManualAcknowledgement() {
        return allowManualAcknowledgement;
    }

    public void setAllowManualAcknowledgement(Boolean allowManualAcknowledgement) {
        this.allowManualAcknowledgement = allowManualAcknowledgement;
    }

    public PulsarMessageReceiptFactory getPulsarMessageReceiptFactory() {
        return pulsarMessageReceiptFactory;
    }

    public void setPulsarMessageReceiptFactory(
            PulsarMessageReceiptFactory pulsarMessageReceiptFactory) {
        this.pulsarMessageReceiptFactory = pulsarMessageReceiptFactory;
    }

    public Boolean getResolvePropertyPlaceholders() {
        return resolvePropertyPlaceholders;
    }

    public void setResolvePropertyPlaceholders(
            Boolean resolvePropertyPlaceholders) {
        this.resolvePropertyPlaceholders = resolvePropertyPlaceholders;
    }

    public static class AutoConfigurationNestedConfiguration {
        public static final Class CAMEL_NESTED_CLASS = org.apache.camel.component.pulsar.utils.AutoConfiguration.class;
    }
}