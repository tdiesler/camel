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
package org.apache.camel.management.mbean;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.management.ManagedAttribute;
import org.apache.camel.management.ManagedResource;

/**
 * @version 
 */
@ManagedResource(description = "Managed Consumer")
public class ManagedConsumer extends ManagedService {
    private final Consumer consumer;

    public ManagedConsumer(CamelContext context, Consumer consumer) {
        super(context, consumer);
        this.consumer = consumer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    @ManagedAttribute(description = "Endpoint Uri")
    public String getEndpointUri() {
        return consumer.getEndpoint().getEndpointUri();
    }

    @ManagedAttribute(description = "Current number of inflight Exchanges")
    public Integer getInflightExchanges() {
        return getContext().getInflightRepository().size(consumer.getEndpoint());
    }

}
