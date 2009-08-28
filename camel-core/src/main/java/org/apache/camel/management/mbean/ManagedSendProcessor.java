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
import org.apache.camel.Endpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.SendProcessor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @version $Revision$
 */
@ManagedResource(description = "Managed SendProcessor")
public class ManagedSendProcessor extends ManagedProcessor {

    private SendProcessor processor;

    public ManagedSendProcessor(CamelContext context, SendProcessor processor, ProcessorDefinition definition) {
        super(context, processor, definition);
        this.processor = processor;
    }

    public SendProcessor getProcessor() {
        return processor;
    }

    @ManagedAttribute(description = "Destination as Endpoint Uri")
    public String getDestination() {
        return processor.getDestination().getEndpointUri();
    }

    @ManagedAttribute(description = "Message Exchange Pattern")
    public String getMessageExchangePattern() {
        if (processor.getPattern() != null) {
            return processor.getPattern().name();
        } else {
            return null;
        }
    }

    @ManagedOperation(description = "Change Destination Endpoint Uri")
    public void changeDestination(String uri) throws Exception {
        Endpoint endpoint = getContext().getEndpoint(uri);
        processor.setDestination(endpoint);
    }

}
