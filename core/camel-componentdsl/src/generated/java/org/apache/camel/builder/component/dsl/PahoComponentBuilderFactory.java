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
import org.apache.camel.component.paho.PahoComponent;

/**
 * Communicate with MQTT message brokers using Eclipse Paho MQTT Client.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface PahoComponentBuilderFactory {

    /**
     * Paho (camel-paho)
     * Communicate with MQTT message brokers using Eclipse Paho MQTT Client.
     * 
     * Category: messaging,iot
     * Since: 2.16
     * Maven coordinates: org.apache.camel:camel-paho
     * 
     * @return the dsl builder
     */
    static PahoComponentBuilder paho() {
        return new PahoComponentBuilderImpl();
    }

    /**
     * Builder for the Paho component.
     */
    interface PahoComponentBuilder extends ComponentBuilder<PahoComponent> {
        /**
         * Sets whether the client will automatically attempt to reconnect to
         * the server if the connection is lost. If set to false, the client
         * will not attempt to automatically reconnect to the server in the
         * event that the connection is lost. If set to true, in the event that
         * the connection is lost, the client will attempt to reconnect to the
         * server. It will initially wait 1 second before it attempts to
         * reconnect, for every failed reconnect attempt, the delay will double
         * until it is at 2 minutes at which point the delay will stay at 2
         * minutes.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: common
         * 
         * @param automaticReconnect the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder automaticReconnect(
                boolean automaticReconnect) {
            doSetProperty("automaticReconnect", automaticReconnect);
            return this;
        }
        /**
         * The URL of the MQTT broker.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: tcp://localhost:1883
         * Group: common
         * 
         * @param brokerUrl the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder brokerUrl(java.lang.String brokerUrl) {
            doSetProperty("brokerUrl", brokerUrl);
            return this;
        }
        /**
         * Sets whether the client and server should remember state across
         * restarts and reconnects. If set to false both the client and server
         * will maintain state across restarts of the client, the server and the
         * connection. As state is maintained: Message delivery will be reliable
         * meeting the specified QOS even if the client, server or connection
         * are restarted. The server will treat a subscription as durable. If
         * set to true the client and server will not maintain state across
         * restarts of the client, the server or the connection. This means
         * Message delivery to the specified QOS cannot be maintained if the
         * client, server or connection are restarted The server will treat a
         * subscription as non-durable.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: common
         * 
         * @param cleanSession the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder cleanSession(boolean cleanSession) {
            doSetProperty("cleanSession", cleanSession);
            return this;
        }
        /**
         * MQTT client identifier. The identifier must be unique.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param clientId the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder clientId(java.lang.String clientId) {
            doSetProperty("clientId", clientId);
            return this;
        }
        /**
         * To use the shared Paho configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.paho.PahoConfiguration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder configuration(
                org.apache.camel.component.paho.PahoConfiguration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
        /**
         * Sets the connection timeout value. This value, measured in seconds,
         * defines the maximum time interval the client will wait for the
         * network connection to the MQTT server to be established. The default
         * timeout is 30 seconds. A value of 0 disables timeout processing
         * meaning the client will wait until the network connection is made
         * successfully or fails.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 30
         * Group: common
         * 
         * @param connectionTimeout the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder connectionTimeout(int connectionTimeout) {
            doSetProperty("connectionTimeout", connectionTimeout);
            return this;
        }
        /**
         * Base directory used by file persistence. Will by default use user
         * directory.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param filePersistenceDirectory the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder filePersistenceDirectory(
                java.lang.String filePersistenceDirectory) {
            doSetProperty("filePersistenceDirectory", filePersistenceDirectory);
            return this;
        }
        /**
         * Sets the keep alive interval. This value, measured in seconds,
         * defines the maximum time interval between messages sent or received.
         * It enables the client to detect if the server is no longer available,
         * without having to wait for the TCP/IP timeout. The client will ensure
         * that at least one message travels across the network within each keep
         * alive period. In the absence of a data-related message during the
         * time period, the client sends a very small ping message, which the
         * server will acknowledge. A value of 0 disables keepalive processing
         * in the client. The default value is 60 seconds.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 60
         * Group: common
         * 
         * @param keepAliveInterval the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder keepAliveInterval(int keepAliveInterval) {
            doSetProperty("keepAliveInterval", keepAliveInterval);
            return this;
        }
        /**
         * Sets the max inflight. please increase this value in a high traffic
         * environment. The default value is 10.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 10
         * Group: common
         * 
         * @param maxInflight the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder maxInflight(int maxInflight) {
            doSetProperty("maxInflight", maxInflight);
            return this;
        }
        /**
         * Get the maximum time (in millis) to wait between reconnects.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 128000
         * Group: common
         * 
         * @param maxReconnectDelay the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder maxReconnectDelay(int maxReconnectDelay) {
            doSetProperty("maxReconnectDelay", maxReconnectDelay);
            return this;
        }
        /**
         * Sets the MQTT version. The default action is to connect with version
         * 3.1.1, and to fall back to 3.1 if that fails. Version 3.1.1 or 3.1
         * can be selected specifically, with no fall back, by using the
         * MQTT_VERSION_3_1_1 or MQTT_VERSION_3_1 options respectively.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param mqttVersion the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder mqttVersion(int mqttVersion) {
            doSetProperty("mqttVersion", mqttVersion);
            return this;
        }
        /**
         * Client persistence to be used - memory or file.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.paho.PahoPersistence&lt;/code&gt; type.
         * 
         * Default: MEMORY
         * Group: common
         * 
         * @param persistence the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder persistence(
                org.apache.camel.component.paho.PahoPersistence persistence) {
            doSetProperty("persistence", persistence);
            return this;
        }
        /**
         * Client quality of service level (0-2).
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 2
         * Group: common
         * 
         * @param qos the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder qos(int qos) {
            doSetProperty("qos", qos);
            return this;
        }
        /**
         * Retain option.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param retained the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder retained(boolean retained) {
            doSetProperty("retained", retained);
            return this;
        }
        /**
         * Set a list of one or more serverURIs the client may connect to.
         * Multiple servers can be separated by comma. Each serverURI specifies
         * the address of a server that the client may connect to. Two types of
         * connection are supported tcp:// for a TCP connection and ssl:// for a
         * TCP connection secured by SSL/TLS. For example: tcp://localhost:1883
         * ssl://localhost:8883 If the port is not specified, it will default to
         * 1883 for tcp:// URIs, and 8883 for ssl:// URIs. If serverURIs is set
         * then it overrides the serverURI parameter passed in on the
         * constructor of the MQTT client. When an attempt to connect is
         * initiated the client will start with the first serverURI in the list
         * and work through the list until a connection is established with a
         * server. If a connection cannot be made to any of the servers then the
         * connect attempt fails. Specifying a list of servers that a client may
         * connect to has several uses: High Availability and reliable message
         * delivery Some MQTT servers support a high availability feature where
         * two or more equal MQTT servers share state. An MQTT client can
         * connect to any of the equal servers and be assured that messages are
         * reliably delivered and durable subscriptions are maintained no matter
         * which server the client connects to. The cleansession flag must be
         * set to false if durable subscriptions and/or reliable message
         * delivery is required. Hunt List A set of servers may be specified
         * that are not equal (as in the high availability option). As no state
         * is shared across the servers reliable message delivery and durable
         * subscriptions are not valid. The cleansession flag must be set to
         * true if the hunt list mode is used.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param serverURIs the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder serverURIs(java.lang.String serverURIs) {
            doSetProperty("serverURIs", serverURIs);
            return this;
        }
        /**
         * Sets the Last Will and Testament (LWT) for the connection. In the
         * event that this client unexpectedly loses its connection to the
         * server, the server will publish a message to itself using the
         * supplied details. The topic to publish to The byte payload for the
         * message. The quality of service to publish the message at (0, 1 or
         * 2). Whether or not the message should be retained.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param willPayload the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder willPayload(java.lang.String willPayload) {
            doSetProperty("willPayload", willPayload);
            return this;
        }
        /**
         * Sets the Last Will and Testament (LWT) for the connection. In the
         * event that this client unexpectedly loses its connection to the
         * server, the server will publish a message to itself using the
         * supplied details. The topic to publish to The byte payload for the
         * message. The quality of service to publish the message at (0, 1 or
         * 2). Whether or not the message should be retained.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param willQos the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder willQos(int willQos) {
            doSetProperty("willQos", willQos);
            return this;
        }
        /**
         * Sets the Last Will and Testament (LWT) for the connection. In the
         * event that this client unexpectedly loses its connection to the
         * server, the server will publish a message to itself using the
         * supplied details. The topic to publish to The byte payload for the
         * message. The quality of service to publish the message at (0, 1 or
         * 2). Whether or not the message should be retained.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param willRetained the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder willRetained(boolean willRetained) {
            doSetProperty("willRetained", willRetained);
            return this;
        }
        /**
         * Sets the Last Will and Testament (LWT) for the connection. In the
         * event that this client unexpectedly loses its connection to the
         * server, the server will publish a message to itself using the
         * supplied details. The topic to publish to The byte payload for the
         * message. The quality of service to publish the message at (0, 1 or
         * 2). Whether or not the message should be retained.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param willTopic the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder willTopic(java.lang.String willTopic) {
            doSetProperty("willTopic", willTopic);
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
        default PahoComponentBuilder bridgeErrorHandler(
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
        default PahoComponentBuilder lazyStartProducer(boolean lazyStartProducer) {
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
        default PahoComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * To use a shared Paho client.
         * 
         * The option is a:
         * &lt;code&gt;org.eclipse.paho.client.mqttv3.MqttClient&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param client the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder client(
                org.eclipse.paho.client.mqttv3.MqttClient client) {
            doSetProperty("client", client);
            return this;
        }
        /**
         * Sets the Custom WebSocket Headers for the WebSocket Connection.
         * 
         * The option is a: &lt;code&gt;java.util.Properties&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param customWebSocketHeaders the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder customWebSocketHeaders(
                java.util.Properties customWebSocketHeaders) {
            doSetProperty("customWebSocketHeaders", customWebSocketHeaders);
            return this;
        }
        /**
         * Set the time in seconds that the executor service should wait when
         * terminating before forcefully terminating. It is not recommended to
         * change this value unless you are absolutely sure that you need to.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1
         * Group: advanced
         * 
         * @param executorServiceTimeout the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder executorServiceTimeout(
                int executorServiceTimeout) {
            doSetProperty("executorServiceTimeout", executorServiceTimeout);
            return this;
        }
        /**
         * Whether SSL HostnameVerifier is enabled or not. The default value is
         * true.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: security
         * 
         * @param httpsHostnameVerificationEnabled the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder httpsHostnameVerificationEnabled(
                boolean httpsHostnameVerificationEnabled) {
            doSetProperty("httpsHostnameVerificationEnabled", httpsHostnameVerificationEnabled);
            return this;
        }
        /**
         * Password to be used for authentication against the MQTT broker.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param password the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder password(java.lang.String password) {
            doSetProperty("password", password);
            return this;
        }
        /**
         * Sets the SocketFactory to use. This allows an application to apply
         * its own policies around the creation of network sockets. If using an
         * SSL connection, an SSLSocketFactory can be used to supply
         * application-specific security settings.
         * 
         * The option is a: &lt;code&gt;javax.net.SocketFactory&lt;/code&gt;
         * type.
         * 
         * Group: security
         * 
         * @param socketFactory the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder socketFactory(
                javax.net.SocketFactory socketFactory) {
            doSetProperty("socketFactory", socketFactory);
            return this;
        }
        /**
         * Sets the SSL properties for the connection. Note that these
         * properties are only valid if an implementation of the Java Secure
         * Socket Extensions (JSSE) is available. These properties are not used
         * if a custom SocketFactory has been set. The following properties can
         * be used: com.ibm.ssl.protocol One of: SSL, SSLv3, TLS, TLSv1,
         * SSL_TLS. com.ibm.ssl.contextProvider Underlying JSSE provider. For
         * example IBMJSSE2 or SunJSSE com.ibm.ssl.keyStore The name of the file
         * that contains the KeyStore object that you want the KeyManager to
         * use. For example /mydir/etc/key.p12 com.ibm.ssl.keyStorePassword The
         * password for the KeyStore object that you want the KeyManager to use.
         * The password can either be in plain-text, or may be obfuscated using
         * the static method: com.ibm.micro.security.Password.obfuscate(char
         * password). This obfuscates the password using a simple and insecure
         * XOR and Base64 encoding mechanism. Note that this is only a simple
         * scrambler to obfuscate clear-text passwords. com.ibm.ssl.keyStoreType
         * Type of key store, for example PKCS12, JKS, or JCEKS.
         * com.ibm.ssl.keyStoreProvider Key store provider, for example IBMJCE
         * or IBMJCEFIPS. com.ibm.ssl.trustStore The name of the file that
         * contains the KeyStore object that you want the TrustManager to use.
         * com.ibm.ssl.trustStorePassword The password for the TrustStore object
         * that you want the TrustManager to use. The password can either be in
         * plain-text, or may be obfuscated using the static method:
         * com.ibm.micro.security.Password.obfuscate(char password). This
         * obfuscates the password using a simple and insecure XOR and Base64
         * encoding mechanism. Note that this is only a simple scrambler to
         * obfuscate clear-text passwords. com.ibm.ssl.trustStoreType The type
         * of KeyStore object that you want the default TrustManager to use.
         * Same possible values as keyStoreType. com.ibm.ssl.trustStoreProvider
         * Trust store provider, for example IBMJCE or IBMJCEFIPS.
         * com.ibm.ssl.enabledCipherSuites A list of which ciphers are enabled.
         * Values are dependent on the provider, for example:
         * SSL_RSA_WITH_AES_128_CBC_SHA;SSL_RSA_WITH_3DES_EDE_CBC_SHA.
         * com.ibm.ssl.keyManager Sets the algorithm that will be used to
         * instantiate a KeyManagerFactory object instead of using the default
         * algorithm available in the platform. Example values: IbmX509 or
         * IBMJ9X509. com.ibm.ssl.trustManager Sets the algorithm that will be
         * used to instantiate a TrustManagerFactory object instead of using the
         * default algorithm available in the platform. Example values: PKIX or
         * IBMJ9X509.
         * 
         * The option is a: &lt;code&gt;java.util.Properties&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslClientProps the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder sslClientProps(
                java.util.Properties sslClientProps) {
            doSetProperty("sslClientProps", sslClientProps);
            return this;
        }
        /**
         * Sets the HostnameVerifier for the SSL connection. Note that it will
         * be used after handshake on a connection and you should do actions by
         * yourself when hostname is verified error. There is no default
         * HostnameVerifier.
         * 
         * The option is a:
         * &lt;code&gt;javax.net.ssl.HostnameVerifier&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslHostnameVerifier the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder sslHostnameVerifier(
                javax.net.ssl.HostnameVerifier sslHostnameVerifier) {
            doSetProperty("sslHostnameVerifier", sslHostnameVerifier);
            return this;
        }
        /**
         * Username to be used for authentication against the MQTT broker.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param userName the value to set
         * @return the dsl builder
         */
        default PahoComponentBuilder userName(java.lang.String userName) {
            doSetProperty("userName", userName);
            return this;
        }
    }

    class PahoComponentBuilderImpl
            extends
                AbstractComponentBuilder<PahoComponent>
            implements
                PahoComponentBuilder {
        @Override
        protected PahoComponent buildConcreteComponent() {
            return new PahoComponent();
        }
        private org.apache.camel.component.paho.PahoConfiguration getOrCreateConfiguration(
                org.apache.camel.component.paho.PahoComponent component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.paho.PahoConfiguration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "automaticReconnect": getOrCreateConfiguration((PahoComponent) component).setAutomaticReconnect((boolean) value); return true;
            case "brokerUrl": getOrCreateConfiguration((PahoComponent) component).setBrokerUrl((java.lang.String) value); return true;
            case "cleanSession": getOrCreateConfiguration((PahoComponent) component).setCleanSession((boolean) value); return true;
            case "clientId": getOrCreateConfiguration((PahoComponent) component).setClientId((java.lang.String) value); return true;
            case "configuration": ((PahoComponent) component).setConfiguration((org.apache.camel.component.paho.PahoConfiguration) value); return true;
            case "connectionTimeout": getOrCreateConfiguration((PahoComponent) component).setConnectionTimeout((int) value); return true;
            case "filePersistenceDirectory": getOrCreateConfiguration((PahoComponent) component).setFilePersistenceDirectory((java.lang.String) value); return true;
            case "keepAliveInterval": getOrCreateConfiguration((PahoComponent) component).setKeepAliveInterval((int) value); return true;
            case "maxInflight": getOrCreateConfiguration((PahoComponent) component).setMaxInflight((int) value); return true;
            case "maxReconnectDelay": getOrCreateConfiguration((PahoComponent) component).setMaxReconnectDelay((int) value); return true;
            case "mqttVersion": getOrCreateConfiguration((PahoComponent) component).setMqttVersion((int) value); return true;
            case "persistence": getOrCreateConfiguration((PahoComponent) component).setPersistence((org.apache.camel.component.paho.PahoPersistence) value); return true;
            case "qos": getOrCreateConfiguration((PahoComponent) component).setQos((int) value); return true;
            case "retained": getOrCreateConfiguration((PahoComponent) component).setRetained((boolean) value); return true;
            case "serverURIs": getOrCreateConfiguration((PahoComponent) component).setServerURIs((java.lang.String) value); return true;
            case "willPayload": getOrCreateConfiguration((PahoComponent) component).setWillPayload((java.lang.String) value); return true;
            case "willQos": getOrCreateConfiguration((PahoComponent) component).setWillQos((int) value); return true;
            case "willRetained": getOrCreateConfiguration((PahoComponent) component).setWillRetained((boolean) value); return true;
            case "willTopic": getOrCreateConfiguration((PahoComponent) component).setWillTopic((java.lang.String) value); return true;
            case "bridgeErrorHandler": ((PahoComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "lazyStartProducer": ((PahoComponent) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((PahoComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "client": ((PahoComponent) component).setClient((org.eclipse.paho.client.mqttv3.MqttClient) value); return true;
            case "customWebSocketHeaders": getOrCreateConfiguration((PahoComponent) component).setCustomWebSocketHeaders((java.util.Properties) value); return true;
            case "executorServiceTimeout": getOrCreateConfiguration((PahoComponent) component).setExecutorServiceTimeout((int) value); return true;
            case "httpsHostnameVerificationEnabled": getOrCreateConfiguration((PahoComponent) component).setHttpsHostnameVerificationEnabled((boolean) value); return true;
            case "password": getOrCreateConfiguration((PahoComponent) component).setPassword((java.lang.String) value); return true;
            case "socketFactory": getOrCreateConfiguration((PahoComponent) component).setSocketFactory((javax.net.SocketFactory) value); return true;
            case "sslClientProps": getOrCreateConfiguration((PahoComponent) component).setSslClientProps((java.util.Properties) value); return true;
            case "sslHostnameVerifier": getOrCreateConfiguration((PahoComponent) component).setSslHostnameVerifier((javax.net.ssl.HostnameVerifier) value); return true;
            case "userName": getOrCreateConfiguration((PahoComponent) component).setUserName((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}