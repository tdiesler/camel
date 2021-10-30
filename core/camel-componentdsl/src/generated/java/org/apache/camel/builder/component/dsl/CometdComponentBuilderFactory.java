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
import org.apache.camel.component.cometd.CometdComponent;

/**
 * Offers publish/subscribe, peer-to-peer (via a server), and RPC style
 * messaging using the CometD/Bayeux protocol.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface CometdComponentBuilderFactory {

    /**
     * CometD (camel-cometd)
     * Offers publish/subscribe, peer-to-peer (via a server), and RPC style
     * messaging using the CometD/Bayeux protocol.
     * 
     * Category: websocket
     * Since: 2.0
     * Maven coordinates: org.apache.camel:camel-cometd
     * 
     * @return the dsl builder
     */
    static CometdComponentBuilder cometd() {
        return new CometdComponentBuilderImpl();
    }

    /**
     * Builder for the CometD component.
     */
    interface CometdComponentBuilder
            extends
                ComponentBuilder<CometdComponent> {
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
        default CometdComponentBuilder bridgeErrorHandler(
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
        default CometdComponentBuilder lazyStartProducer(
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
        default CometdComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * To use a list of custom BayeuxServer.Extension that allows modifying
         * incoming and outgoing requests.
         * 
         * The option is a:
         * &lt;code&gt;java.util.List&amp;lt;org.cometd.bayeux.server.BayeuxServer.Extension&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param extensions the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder extensions(
                java.util.List<org.cometd.bayeux.server.BayeuxServer.Extension> extensions) {
            doSetProperty("extensions", extensions);
            return this;
        }
        /**
         * To use a custom configured SecurityPolicy to control authorization.
         * 
         * The option is a:
         * &lt;code&gt;org.cometd.bayeux.server.SecurityPolicy&lt;/code&gt;
         * type.
         * 
         * Group: security
         * 
         * @param securityPolicy the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder securityPolicy(
                org.cometd.bayeux.server.SecurityPolicy securityPolicy) {
            doSetProperty("securityPolicy", securityPolicy);
            return this;
        }
        /**
         * To configure security using SSLContextParameters.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.support.jsse.SSLContextParameters&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslContextParameters the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder sslContextParameters(
                org.apache.camel.support.jsse.SSLContextParameters sslContextParameters) {
            doSetProperty("sslContextParameters", sslContextParameters);
            return this;
        }
        /**
         * The password for the keystore when using SSL.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslKeyPassword the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder sslKeyPassword(
                java.lang.String sslKeyPassword) {
            doSetProperty("sslKeyPassword", sslKeyPassword);
            return this;
        }
        /**
         * The path to the keystore.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslKeystore the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder sslKeystore(java.lang.String sslKeystore) {
            doSetProperty("sslKeystore", sslKeystore);
            return this;
        }
        /**
         * The password when using SSL.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslPassword the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder sslPassword(java.lang.String sslPassword) {
            doSetProperty("sslPassword", sslPassword);
            return this;
        }
        /**
         * Enable usage of global SSL context parameters.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: security
         * 
         * @param useGlobalSslContextParameters the value to set
         * @return the dsl builder
         */
        default CometdComponentBuilder useGlobalSslContextParameters(
                boolean useGlobalSslContextParameters) {
            doSetProperty("useGlobalSslContextParameters", useGlobalSslContextParameters);
            return this;
        }
    }

    class CometdComponentBuilderImpl
            extends
                AbstractComponentBuilder<CometdComponent>
            implements
                CometdComponentBuilder {
        @Override
        protected CometdComponent buildConcreteComponent() {
            return new CometdComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "bridgeErrorHandler": ((CometdComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "lazyStartProducer": ((CometdComponent) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((CometdComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "extensions": ((CometdComponent) component).setExtensions((java.util.List) value); return true;
            case "securityPolicy": ((CometdComponent) component).setSecurityPolicy((org.cometd.bayeux.server.SecurityPolicy) value); return true;
            case "sslContextParameters": ((CometdComponent) component).setSslContextParameters((org.apache.camel.support.jsse.SSLContextParameters) value); return true;
            case "sslKeyPassword": ((CometdComponent) component).setSslKeyPassword((java.lang.String) value); return true;
            case "sslKeystore": ((CometdComponent) component).setSslKeystore((java.lang.String) value); return true;
            case "sslPassword": ((CometdComponent) component).setSslPassword((java.lang.String) value); return true;
            case "useGlobalSslContextParameters": ((CometdComponent) component).setUseGlobalSslContextParameters((boolean) value); return true;
            default: return false;
            }
        }
    }
}