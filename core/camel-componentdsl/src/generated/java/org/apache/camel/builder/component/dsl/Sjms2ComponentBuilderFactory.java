/*
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
package org.apache.camel.builder.component.dsl;

import javax.annotation.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.sjms2.Sjms2Component;

/**
 * Send and receive messages to/from a JMS Queue or Topic using plain JMS 2.x
 * API.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface Sjms2ComponentBuilderFactory {

    /**
     * Simple JMS2 (camel-sjms2)
     * Send and receive messages to/from a JMS Queue or Topic using plain JMS
     * 2.x API.
     * 
     * Category: messaging
     * Since: 2.19
     * Maven coordinates: org.apache.camel:camel-sjms2
     * 
     * @return the dsl builder
     */
    static Sjms2ComponentBuilder sjms2() {
        return new Sjms2ComponentBuilderImpl();
    }

    /**
     * Builder for the Simple JMS2 component.
     */
    interface Sjms2ComponentBuilder extends ComponentBuilder<Sjms2Component> {
        /**
         * The connection factory to be use. A connection factory must be
         * configured either on the component or endpoint.
         * 
         * The option is a: &lt;code&gt;javax.jms.ConnectionFactory&lt;/code&gt;
         * type.
         * 
         * Group: common
         * 
         * @param connectionFactory the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder connectionFactory(
                javax.jms.ConnectionFactory connectionFactory) {
            doSetProperty("connectionFactory", connectionFactory);
            return this;
        }
        /**
         * Allows for bridging the consumer to the Camel routing Error Handler,
         * which mean any exceptions occurred while the consumer is trying to
         * pickup incoming messages, or the likes, will now be processed as a
         * message and handled by the routing Error Handler. By default the
         * consumer will use the org.apache.camel.spi.ExceptionHandler to deal
         * with exceptions, that will be logged at WARN or ERROR level and
         * ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param bridgeErrorHandler the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder bridgeErrorHandler(
                boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder lazyStartProducer(
                boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * To use a custom DestinationCreationStrategy.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.sjms.jms.DestinationCreationStrategy&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param destinationCreationStrategy the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder destinationCreationStrategy(
                org.apache.camel.component.sjms.jms.DestinationCreationStrategy destinationCreationStrategy) {
            doSetProperty("destinationCreationStrategy", destinationCreationStrategy);
            return this;
        }
        /**
         * Pluggable strategy for encoding and decoding JMS keys so they can be
         * compliant with the JMS specification. Camel provides one
         * implementation out of the box: default. The default strategy will
         * safely marshal dots and hyphens (. and -). Can be used for JMS
         * brokers which do not care whether JMS header keys contain illegal
         * characters. You can provide your own implementation of the
         * org.apache.camel.component.jms.JmsKeyFormatStrategy and refer to it
         * using the # notation.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.sjms.jms.JmsKeyFormatStrategy&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param jmsKeyFormatStrategy the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder jmsKeyFormatStrategy(
                org.apache.camel.component.sjms.jms.JmsKeyFormatStrategy jmsKeyFormatStrategy) {
            doSetProperty("jmsKeyFormatStrategy", jmsKeyFormatStrategy);
            return this;
        }
        /**
         * To use the given MessageCreatedStrategy which are invoked when Camel
         * creates new instances of javax.jms.Message objects when Camel is
         * sending a JMS message.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.sjms.jms.MessageCreatedStrategy&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param messageCreatedStrategy the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder messageCreatedStrategy(
                org.apache.camel.component.sjms.jms.MessageCreatedStrategy messageCreatedStrategy) {
            doSetProperty("messageCreatedStrategy", messageCreatedStrategy);
            return this;
        }
        /**
         * Specifies the interval between recovery attempts, i.e. when a
         * connection is being refreshed, in milliseconds. The default is 5000
         * ms, that is, 5 seconds.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 5000
         * Group: advanced
         * 
         * @param recoveryInterval the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder recoveryInterval(long recoveryInterval) {
            doSetProperty("recoveryInterval", recoveryInterval);
            return this;
        }
        /**
         * Specifies the maximum number of concurrent consumers for continue
         * routing when timeout occurred when using request/reply over JMS.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1
         * Group: advanced
         * 
         * @param replyToOnTimeoutMaxConcurrentConsumers the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder replyToOnTimeoutMaxConcurrentConsumers(
                int replyToOnTimeoutMaxConcurrentConsumers) {
            doSetProperty("replyToOnTimeoutMaxConcurrentConsumers", replyToOnTimeoutMaxConcurrentConsumers);
            return this;
        }
        /**
         * Configures how often Camel should check for timed out Exchanges when
         * doing request/reply over JMS. By default Camel checks once per
         * second. But if you must react faster when a timeout occurs, then you
         * can lower this interval, to check more frequently. The timeout is
         * determined by the option requestTimeout.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 1000
         * Group: advanced
         * 
         * @param requestTimeoutCheckerInterval the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder requestTimeoutCheckerInterval(
                long requestTimeoutCheckerInterval) {
            doSetProperty("requestTimeoutCheckerInterval", requestTimeoutCheckerInterval);
            return this;
        }
        /**
         * To use a custom org.apache.camel.spi.HeaderFilterStrategy to filter
         * header to and from Camel message.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.spi.HeaderFilterStrategy&lt;/code&gt;
         * type.
         * 
         * Group: filter
         * 
         * @param headerFilterStrategy the value to set
         * @return the dsl builder
         */
        default Sjms2ComponentBuilder headerFilterStrategy(
                org.apache.camel.spi.HeaderFilterStrategy headerFilterStrategy) {
            doSetProperty("headerFilterStrategy", headerFilterStrategy);
            return this;
        }
    }

    class Sjms2ComponentBuilderImpl
            extends
                AbstractComponentBuilder<Sjms2Component>
            implements
                Sjms2ComponentBuilder {
        @Override
        protected Sjms2Component buildConcreteComponent() {
            return new Sjms2Component();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "connectionFactory": ((Sjms2Component) component).setConnectionFactory((javax.jms.ConnectionFactory) value); return true;
            case "bridgeErrorHandler": ((Sjms2Component) component).setBridgeErrorHandler((boolean) value); return true;
            case "lazyStartProducer": ((Sjms2Component) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((Sjms2Component) component).setAutowiredEnabled((boolean) value); return true;
            case "destinationCreationStrategy": ((Sjms2Component) component).setDestinationCreationStrategy((org.apache.camel.component.sjms.jms.DestinationCreationStrategy) value); return true;
            case "jmsKeyFormatStrategy": ((Sjms2Component) component).setJmsKeyFormatStrategy((org.apache.camel.component.sjms.jms.JmsKeyFormatStrategy) value); return true;
            case "messageCreatedStrategy": ((Sjms2Component) component).setMessageCreatedStrategy((org.apache.camel.component.sjms.jms.MessageCreatedStrategy) value); return true;
            case "recoveryInterval": ((Sjms2Component) component).setRecoveryInterval((long) value); return true;
            case "replyToOnTimeoutMaxConcurrentConsumers": ((Sjms2Component) component).setReplyToOnTimeoutMaxConcurrentConsumers((int) value); return true;
            case "requestTimeoutCheckerInterval": ((Sjms2Component) component).setRequestTimeoutCheckerInterval((long) value); return true;
            case "headerFilterStrategy": ((Sjms2Component) component).setHeaderFilterStrategy((org.apache.camel.spi.HeaderFilterStrategy) value); return true;
            default: return false;
            }
        }
    }
}