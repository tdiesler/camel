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
package org.apache.camel.component.vertx;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A Camel Endpoint for working with <a href="http://vertx.io/">vert.x</a> event bus endpoints
 */
@UriEndpoint(scheme = "vertx", consumerClass = VertxConsumer.class)
public class VertxEndpoint extends DefaultEndpoint {

    @UriParam
    private String address;

    public VertxEndpoint(String uri, VertxComponent component, String address) {
        super(uri, component);
        this.address = address;
    }

    @Override
    public VertxComponent getComponent() {
        return (VertxComponent) super.getComponent();
    }

    public Producer createProducer() throws Exception {
        return new VertxProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        VertxConsumer consumer = new VertxConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    public boolean isSingleton() {
        return true;
    }

    public EventBus getEventBus() {
        return getVertx().eventBus();
    }

    public Vertx getVertx() {
        return getComponent().getVertx();
    }

    public String getAddress() {
        return address;
    }

    /**
     * Sets the event bus address used to communicate
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
