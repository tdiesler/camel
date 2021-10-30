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
import org.apache.camel.component.atomix.client.messaging.AtomixMessagingComponent;

/**
 * Access Atomix's group messaging.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface AtomixMessagingComponentBuilderFactory {

    /**
     * Atomix Messaging (camel-atomix)
     * Access Atomix's group messaging.
     * 
     * Category: clustering
     * Since: 2.20
     * Maven coordinates: org.apache.camel:camel-atomix
     * 
     * @return the dsl builder
     */
    static AtomixMessagingComponentBuilder atomixMessaging() {
        return new AtomixMessagingComponentBuilderImpl();
    }

    /**
     * Builder for the Atomix Messaging component.
     */
    interface AtomixMessagingComponentBuilder
            extends
                ComponentBuilder<AtomixMessagingComponent> {
        /**
         * The Atomix instance to use.
         * 
         * The option is a: &lt;code&gt;io.atomix.Atomix&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param atomix the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder atomix(io.atomix.Atomix atomix) {
            doSetProperty("atomix", atomix);
            return this;
        }
        /**
         * The broadcast type.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.atomix.client.messaging.AtomixMessaging.BroadcastType&lt;/code&gt; type.
         * 
         * Default: ALL
         * Group: common
         * 
         * @param broadcastType the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder broadcastType(
                org.apache.camel.component.atomix.client.messaging.AtomixMessaging.BroadcastType broadcastType) {
            doSetProperty("broadcastType", broadcastType);
            return this;
        }
        /**
         * The messaging channel name.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param channelName the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder channelName(
                java.lang.String channelName) {
            doSetProperty("channelName", channelName);
            return this;
        }
        /**
         * The shared component configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.atomix.client.messaging.AtomixMessagingConfiguration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder configuration(
                org.apache.camel.component.atomix.client.messaging.AtomixMessagingConfiguration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
        /**
         * The path to the AtomixClient configuration.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configurationUri the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder configurationUri(
                java.lang.String configurationUri) {
            doSetProperty("configurationUri", configurationUri);
            return this;
        }
        /**
         * The default action.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.atomix.client.messaging.AtomixMessaging.Action&lt;/code&gt; type.
         * 
         * Default: DIRECT
         * Group: common
         * 
         * @param defaultAction the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder defaultAction(
                org.apache.camel.component.atomix.client.messaging.AtomixMessaging.Action defaultAction) {
            doSetProperty("defaultAction", defaultAction);
            return this;
        }
        /**
         * The Atomix Group member name.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param memberName the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder memberName(
                java.lang.String memberName) {
            doSetProperty("memberName", memberName);
            return this;
        }
        /**
         * The nodes the AtomixClient should connect to.
         * 
         * The option is a:
         * &lt;code&gt;java.util.List&amp;lt;io.atomix.catalyst.transport.Address&amp;gt;&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param nodes the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder nodes(
                java.util.List<io.atomix.catalyst.transport.Address> nodes) {
            doSetProperty("nodes", nodes);
            return this;
        }
        /**
         * The header that wil carry the result.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param resultHeader the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder resultHeader(
                java.lang.String resultHeader) {
            doSetProperty("resultHeader", resultHeader);
            return this;
        }
        /**
         * The class name (fqn) of the Atomix transport.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: io.atomix.catalyst.transport.netty.NettyTransport
         * Group: common
         * 
         * @param transportClassName the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder transportClassName(
                java.lang.String transportClassName) {
            doSetProperty("transportClassName", transportClassName);
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
        default AtomixMessagingComponentBuilder bridgeErrorHandler(
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
        default AtomixMessagingComponentBuilder lazyStartProducer(
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
        default AtomixMessagingComponentBuilder autowiredEnabled(
                boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * The cluster wide default resource configuration.
         * 
         * The option is a: &lt;code&gt;java.util.Properties&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param defaultResourceConfig the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder defaultResourceConfig(
                java.util.Properties defaultResourceConfig) {
            doSetProperty("defaultResourceConfig", defaultResourceConfig);
            return this;
        }
        /**
         * The local default resource options.
         * 
         * The option is a: &lt;code&gt;java.util.Properties&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param defaultResourceOptions the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder defaultResourceOptions(
                java.util.Properties defaultResourceOptions) {
            doSetProperty("defaultResourceOptions", defaultResourceOptions);
            return this;
        }
        /**
         * Sets if the local member should join groups as PersistentMember or
         * not. If set to ephemeral the local member will receive an auto
         * generated ID thus the local one is ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: advanced
         * 
         * @param ephemeral the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder ephemeral(boolean ephemeral) {
            doSetProperty("ephemeral", ephemeral);
            return this;
        }
        /**
         * The read consistency level.
         * 
         * The option is a:
         * &lt;code&gt;io.atomix.resource.ReadConsistency&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param readConsistency the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder readConsistency(
                io.atomix.resource.ReadConsistency readConsistency) {
            doSetProperty("readConsistency", readConsistency);
            return this;
        }
        /**
         * Cluster wide resources configuration.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.util.Properties&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param resourceConfigs the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder resourceConfigs(
                java.util.Map<java.lang.String, java.util.Properties> resourceConfigs) {
            doSetProperty("resourceConfigs", resourceConfigs);
            return this;
        }
        /**
         * Local resources configurations.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.util.Properties&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param resourceOptions the value to set
         * @return the dsl builder
         */
        default AtomixMessagingComponentBuilder resourceOptions(
                java.util.Map<java.lang.String, java.util.Properties> resourceOptions) {
            doSetProperty("resourceOptions", resourceOptions);
            return this;
        }
    }

    class AtomixMessagingComponentBuilderImpl
            extends
                AbstractComponentBuilder<AtomixMessagingComponent>
            implements
                AtomixMessagingComponentBuilder {
        @Override
        protected AtomixMessagingComponent buildConcreteComponent() {
            return new AtomixMessagingComponent();
        }
        private org.apache.camel.component.atomix.client.messaging.AtomixMessagingConfiguration getOrCreateConfiguration(
                org.apache.camel.component.atomix.client.messaging.AtomixMessagingComponent component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.atomix.client.messaging.AtomixMessagingConfiguration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "atomix": getOrCreateConfiguration((AtomixMessagingComponent) component).setAtomix((io.atomix.Atomix) value); return true;
            case "broadcastType": getOrCreateConfiguration((AtomixMessagingComponent) component).setBroadcastType((org.apache.camel.component.atomix.client.messaging.AtomixMessaging.BroadcastType) value); return true;
            case "channelName": getOrCreateConfiguration((AtomixMessagingComponent) component).setChannelName((java.lang.String) value); return true;
            case "configuration": ((AtomixMessagingComponent) component).setConfiguration((org.apache.camel.component.atomix.client.messaging.AtomixMessagingConfiguration) value); return true;
            case "configurationUri": ((AtomixMessagingComponent) component).setConfigurationUri((java.lang.String) value); return true;
            case "defaultAction": getOrCreateConfiguration((AtomixMessagingComponent) component).setDefaultAction((org.apache.camel.component.atomix.client.messaging.AtomixMessaging.Action) value); return true;
            case "memberName": getOrCreateConfiguration((AtomixMessagingComponent) component).setMemberName((java.lang.String) value); return true;
            case "nodes": ((AtomixMessagingComponent) component).setNodes((java.util.List) value); return true;
            case "resultHeader": getOrCreateConfiguration((AtomixMessagingComponent) component).setResultHeader((java.lang.String) value); return true;
            case "transportClassName": getOrCreateConfiguration((AtomixMessagingComponent) component).setTransportClassName((java.lang.String) value); return true;
            case "bridgeErrorHandler": ((AtomixMessagingComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "lazyStartProducer": ((AtomixMessagingComponent) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((AtomixMessagingComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "defaultResourceConfig": getOrCreateConfiguration((AtomixMessagingComponent) component).setDefaultResourceConfig((java.util.Properties) value); return true;
            case "defaultResourceOptions": getOrCreateConfiguration((AtomixMessagingComponent) component).setDefaultResourceOptions((java.util.Properties) value); return true;
            case "ephemeral": getOrCreateConfiguration((AtomixMessagingComponent) component).setEphemeral((boolean) value); return true;
            case "readConsistency": getOrCreateConfiguration((AtomixMessagingComponent) component).setReadConsistency((io.atomix.resource.ReadConsistency) value); return true;
            case "resourceConfigs": getOrCreateConfiguration((AtomixMessagingComponent) component).setResourceConfigs((java.util.Map) value); return true;
            case "resourceOptions": getOrCreateConfiguration((AtomixMessagingComponent) component).setResourceOptions((java.util.Map) value); return true;
            default: return false;
            }
        }
    }
}