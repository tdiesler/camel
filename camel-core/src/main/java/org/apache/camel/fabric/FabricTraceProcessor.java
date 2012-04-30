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

import java.util.Date;
import java.util.Queue;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.util.MessageHelper;

/**
 *
 */
public class FabricTraceProcessor extends DelegateAsyncProcessor {

    private final Queue<FabricTracerEventMessage> queue;
    private final FabricTracer tracer;
    private final ProcessorDefinition<?> processorDefinition;
    private final ProcessorDefinition<?> routeDefinition;
    private final boolean first;

    public FabricTraceProcessor(Queue<FabricTracerEventMessage> queue, Processor processor,
                                ProcessorDefinition<?> processorDefinition,
                                ProcessorDefinition<?> routeDefinition, boolean first,
                                FabricTracer tracer) {
        super(processor);
        this.queue = queue;
        this.processorDefinition = processorDefinition;
        this.routeDefinition = routeDefinition;
        this.first = first;
        this.tracer = tracer;
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            if (tracer.shouldTrace(processorDefinition)) {
                // ensure there is space on the queue
                int drain = queue.size() - tracer.getQueueSize();
                if (drain > 0) {
                    for (int i = 0; i < drain; i++) {
                        queue.poll();
                    }
                }

                Date timestamp = new Date();
                String toNode = processorDefinition.getId();
                String exchangeId = exchange.getExchangeId();
                String messageAsXml = MessageHelper.dumpAsXml(exchange.getIn());

                // if first we should add a pseudo trace message as well, so we have a starting message as well
                if (first) {
                    Date created = exchange.getProperty(Exchange.CREATED_TIMESTAMP, timestamp, Date.class);
                    String routeId = routeDefinition.getId();
                    FabricTracerEventMessage pseudo = new FabricTracerEventMessage(tracer.incrementTraceCounter(), created, routeId, exchangeId, messageAsXml);
                    queue.add(pseudo);
                }
                FabricTracerEventMessage event = new FabricTracerEventMessage(tracer.incrementTraceCounter(), timestamp, toNode, exchangeId, messageAsXml);
                queue.add(event);
            }

            // invoke processor
            return super.process(exchange, callback);

        } catch (Exception e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        }
    }

    public void stop() throws Exception {
        super.stop();
        queue.clear();
        // notify tracer we are stopping to not leak resources
        tracer.stopProcessor(this, processorDefinition);
    }

    @Override
    public String toString() {
        return processor.toString();
    }

}
