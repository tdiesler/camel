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
import org.apache.camel.component.etcd.EtcdKeysComponent;

/**
 * Get, set or delete keys in etcd key-value store.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface EtcdKeysComponentBuilderFactory {

    /**
     * Etcd Keys (camel-etcd)
     * Get, set or delete keys in etcd key-value store.
     * 
     * Category: clustering,database
     * Since: 2.18
     * Maven coordinates: org.apache.camel:camel-etcd
     * 
     * @return the dsl builder
     */
    static EtcdKeysComponentBuilder etcdKeys() {
        return new EtcdKeysComponentBuilderImpl();
    }

    /**
     * Builder for the Etcd Keys component.
     */
    interface EtcdKeysComponentBuilder
            extends
                ComponentBuilder<EtcdKeysComponent> {
        /**
         * Component configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.etcd.EtcdConfiguration&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder configuration(
                org.apache.camel.component.etcd.EtcdConfiguration configuration) {
            doSetProperty("configuration", configuration);
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
        default EtcdKeysComponentBuilder lazyStartProducer(
                boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * To apply an action recursively.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param recursive the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder recursive(boolean recursive) {
            doSetProperty("recursive", recursive);
            return this;
        }
        /**
         * The path to look for for service discovery.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: /services/
         * Group: producer
         * 
         * @param servicePath the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder servicePath(
                java.lang.String servicePath) {
            doSetProperty("servicePath", servicePath);
            return this;
        }
        /**
         * To set the maximum time an action could take to complete.
         * 
         * The option is a: &lt;code&gt;java.lang.Long&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param timeout the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder timeout(java.lang.Long timeout) {
            doSetProperty("timeout", timeout);
            return this;
        }
        /**
         * To set the URIs the client connects.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: http://localhost:2379,http://localhost:4001
         * Group: common
         * 
         * @param uris the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder uris(java.lang.String uris) {
            doSetProperty("uris", uris);
            return this;
        }
        /**
         * To set the lifespan of a key in milliseconds.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param timeToLive the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder timeToLive(java.lang.Integer timeToLive) {
            doSetProperty("timeToLive", timeToLive);
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
        default EtcdKeysComponentBuilder autowiredEnabled(
                boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * The password to use for basic authentication.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param password the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder password(java.lang.String password) {
            doSetProperty("password", password);
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
        default EtcdKeysComponentBuilder sslContextParameters(
                org.apache.camel.support.jsse.SSLContextParameters sslContextParameters) {
            doSetProperty("sslContextParameters", sslContextParameters);
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
        default EtcdKeysComponentBuilder useGlobalSslContextParameters(
                boolean useGlobalSslContextParameters) {
            doSetProperty("useGlobalSslContextParameters", useGlobalSslContextParameters);
            return this;
        }
        /**
         * The user name to use for basic authentication.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param userName the value to set
         * @return the dsl builder
         */
        default EtcdKeysComponentBuilder userName(java.lang.String userName) {
            doSetProperty("userName", userName);
            return this;
        }
    }

    class EtcdKeysComponentBuilderImpl
            extends
                AbstractComponentBuilder<EtcdKeysComponent>
            implements
                EtcdKeysComponentBuilder {
        @Override
        protected EtcdKeysComponent buildConcreteComponent() {
            return new EtcdKeysComponent();
        }
        private org.apache.camel.component.etcd.EtcdConfiguration getOrCreateConfiguration(
                org.apache.camel.component.etcd.EtcdKeysComponent component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.etcd.EtcdConfiguration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "configuration": ((EtcdKeysComponent) component).setConfiguration((org.apache.camel.component.etcd.EtcdConfiguration) value); return true;
            case "lazyStartProducer": ((EtcdKeysComponent) component).setLazyStartProducer((boolean) value); return true;
            case "recursive": getOrCreateConfiguration((EtcdKeysComponent) component).setRecursive((boolean) value); return true;
            case "servicePath": getOrCreateConfiguration((EtcdKeysComponent) component).setServicePath((java.lang.String) value); return true;
            case "timeout": getOrCreateConfiguration((EtcdKeysComponent) component).setTimeout((java.lang.Long) value); return true;
            case "uris": getOrCreateConfiguration((EtcdKeysComponent) component).setUris((java.lang.String) value); return true;
            case "timeToLive": getOrCreateConfiguration((EtcdKeysComponent) component).setTimeToLive((java.lang.Integer) value); return true;
            case "autowiredEnabled": ((EtcdKeysComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "password": getOrCreateConfiguration((EtcdKeysComponent) component).setPassword((java.lang.String) value); return true;
            case "sslContextParameters": getOrCreateConfiguration((EtcdKeysComponent) component).setSslContextParameters((org.apache.camel.support.jsse.SSLContextParameters) value); return true;
            case "useGlobalSslContextParameters": ((EtcdKeysComponent) component).setUseGlobalSslContextParameters((boolean) value); return true;
            case "userName": getOrCreateConfiguration((EtcdKeysComponent) component).setUserName((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}