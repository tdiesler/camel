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
package org.apache.camel.fabric;

import java.io.Serializable;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.MessageHelper;

/**
 *
 */
public class FabricTracerEventMessage implements Serializable {

    private final Date timestamp;
    private final String toNode;
    private final String exchangeId;
    private final String messageAsXml;

    public FabricTracerEventMessage(Exchange exchange, ProcessorDefinition<?> toNode) {
        this.timestamp = new Date();
        this.toNode = toNode.getId();
        this.exchangeId = exchange.getExchangeId();

        // TODO: stream payloads
        this.messageAsXml = MessageHelper.dumpAsXml(exchange.getIn());
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getToNode() {
        return toNode;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public String getMessageAsXml() {
        return messageAsXml;
    }

    @Override
    public String toString() {
        return "FabricTraceEvent[" + exchangeId + " at " + toNode + "]";
    }
}
