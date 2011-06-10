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

import java.util.Queue;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.NodeIdFactory;

/**
 *
 */
public class FabricTraceProcessor extends DelegateAsyncProcessor {

    private final Queue<FabricTracerEventMessage> queue;
    private final FabricTracer tracer;
    private final ProcessorDefinition<?> processorDefinition;
    private boolean createIds = true;

    public FabricTraceProcessor(Queue<FabricTracerEventMessage> queue, Processor processor, ProcessorDefinition<?> processorDefinition, FabricTracer tracer) {
        super(processor);
        this.queue = queue;
        this.processorDefinition = processorDefinition;
        this.tracer = tracer;
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            if (tracer.shouldTrace(processorDefinition)) {
                // ensure there is space on the queue
                int drain = queue.size() - tracer.getQueueSize();
                if (drain > 0) {
                for (int i = 0; i < drain; i++)
                    queue.poll();
                }

                if (createIds) {
                    NodeIdFactory factory = exchange.getContext().getNodeIdFactory();
                    if (factory != null) {
                        processorDefinition.idOrCreate(factory);
                    }
                }
                FabricTracerEventMessage event = new FabricTracerEventMessage(tracer.incrementTraceCounter(), exchange, processorDefinition);
                queue.add(event);
            }
        } catch (Exception e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        } finally {
            return super.process(exchange, callback);
        }
    }

    public void stop() throws Exception {
        super.stop();
        queue.clear();
    }

    @Override
    public String toString() {
        return processor.toString();
    }

    public boolean isCreateIds() {
        return createIds;
    }

    public void setCreateIds(boolean createIds) {
        this.createIds = createIds;
    }
}
