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
import org.apache.camel.component.master.MasterComponent;

/**
 * Have only a single consumer in a cluster consuming from a given endpoint;
 * with automatic failover if the JVM dies.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface MasterComponentBuilderFactory {

    /**
     * Master (camel-master)
     * Have only a single consumer in a cluster consuming from a given endpoint;
     * with automatic failover if the JVM dies.
     * 
     * Category: clustering
     * Since: 2.20
     * Maven coordinates: org.apache.camel:camel-master
     * 
     * @return the dsl builder
     */
    static MasterComponentBuilder master() {
        return new MasterComponentBuilderImpl();
    }

    /**
     * Builder for the Master component.
     */
    interface MasterComponentBuilder
            extends
                ComponentBuilder<MasterComponent> {
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
        default MasterComponentBuilder bridgeErrorHandler(
                boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
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
        default MasterComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * Inject the service to use.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.cluster.CamelClusterService&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param service the value to set
         * @return the dsl builder
         */
        default MasterComponentBuilder service(
                org.apache.camel.cluster.CamelClusterService service) {
            doSetProperty("service", service);
            return this;
        }
        /**
         * Inject the service selector used to lookup the CamelClusterService to
         * use.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.cluster.CamelClusterService.Selector&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param serviceSelector the value to set
         * @return the dsl builder
         */
        default MasterComponentBuilder serviceSelector(
                org.apache.camel.cluster.CamelClusterService.Selector serviceSelector) {
            doSetProperty("serviceSelector", serviceSelector);
            return this;
        }
    }

    class MasterComponentBuilderImpl
            extends
                AbstractComponentBuilder<MasterComponent>
            implements
                MasterComponentBuilder {
        @Override
        protected MasterComponent buildConcreteComponent() {
            return new MasterComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "bridgeErrorHandler": ((MasterComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "autowiredEnabled": ((MasterComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "service": ((MasterComponent) component).setService((org.apache.camel.cluster.CamelClusterService) value); return true;
            case "serviceSelector": ((MasterComponent) component).setServiceSelector((org.apache.camel.cluster.CamelClusterService.Selector) value); return true;
            default: return false;
            }
        }
    }
}