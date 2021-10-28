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
import org.apache.camel.component.iec60870.server.ServerComponent;

/**
 * IEC 60870 supervisory control and data acquisition (SCADA) server using
 * NeoSCADA implementation.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface Iec60870ServerComponentBuilderFactory {

    /**
     * IEC 60870 Server (camel-iec60870)
     * IEC 60870 supervisory control and data acquisition (SCADA) server using
     * NeoSCADA implementation.
     * 
     * Category: iot
     * Since: 2.20
     * Maven coordinates: org.apache.camel:camel-iec60870
     * 
     * @return the dsl builder
     */
    static Iec60870ServerComponentBuilder iec60870Server() {
        return new Iec60870ServerComponentBuilderImpl();
    }

    /**
     * Builder for the IEC 60870 Server component.
     */
    interface Iec60870ServerComponentBuilder
            extends
                ComponentBuilder<ServerComponent> {
        /**
         * Default connection options.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.iec60870.server.ServerOptions&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param defaultConnectionOptions the value to set
         * @return the dsl builder
         */
        default Iec60870ServerComponentBuilder defaultConnectionOptions(
                org.apache.camel.component.iec60870.server.ServerOptions defaultConnectionOptions) {
            doSetProperty("defaultConnectionOptions", defaultConnectionOptions);
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
        default Iec60870ServerComponentBuilder bridgeErrorHandler(
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
        default Iec60870ServerComponentBuilder lazyStartProducer(
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
        default Iec60870ServerComponentBuilder autowiredEnabled(
                boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
    }

    class Iec60870ServerComponentBuilderImpl
            extends
                AbstractComponentBuilder<ServerComponent>
            implements
                Iec60870ServerComponentBuilder {
        @Override
        protected ServerComponent buildConcreteComponent() {
            return new ServerComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "defaultConnectionOptions": ((ServerComponent) component).setDefaultConnectionOptions((org.apache.camel.component.iec60870.server.ServerOptions) value); return true;
            case "bridgeErrorHandler": ((ServerComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "lazyStartProducer": ((ServerComponent) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((ServerComponent) component).setAutowiredEnabled((boolean) value); return true;
            default: return false;
            }
        }
    }
}