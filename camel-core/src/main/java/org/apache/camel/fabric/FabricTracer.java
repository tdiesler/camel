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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.NodeIdFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 */
@ManagedResource("FabricTracer")
public class FabricTracer extends ServiceSupport implements InterceptStrategy {

    private final Map<ProcessorDefinition, Queue<FabricTracerEventMessage>> traces =
            new ConcurrentHashMap<ProcessorDefinition, Queue<FabricTracerEventMessage>>();
    private boolean enabled;
    private final AtomicLong traceCounter = new AtomicLong(0);
    private int queueSize = 10;
    private CamelContext context;

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        this.context = context;
        Queue<FabricTracerEventMessage> queue = traces.get(definition);
        if (queue == null) {
            queue = new ArrayBlockingQueue<FabricTracerEventMessage>(queueSize);
            traces.put(definition, queue);
        }

        return new FabricTraceProcessor(queue, target, definition, this);
    }

    /**
     * Whether or not to trace the given processor definition.
     *
     * @param definition the processor definition
     * @return <tt>true</tt> to trace, <tt>false</tt> to skip tracing
     */
    public boolean shouldTrace(ProcessorDefinition<?> definition) {
        return enabled;
    }

    @ManagedAttribute(description = "Is tracing enabled")
    public boolean isEnabled() {
        return enabled;
    }

    @ManagedAttribute(description = "Is tracing enabled")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ManagedAttribute(description = "Number of traced messages to keep in FIFO queue")
    public int getQueueSize() {
        return queueSize;
    }

    @ManagedAttribute(description = "Number of traced messages to keep in FIFO queue")
    public void setQueueSize(int queueSize) {
        if (queueSize <= 0) {
            throw new IllegalArgumentException("The queue size must be a positive number, was: " + queueSize);
        }
        this.queueSize = queueSize;
    }

    @ManagedAttribute(description = "Number of total traced messages")
    public long getTraceCounter() {
        return traceCounter.get();
    }

    @ManagedOperation(description = "Resets the trace counter")
    public void resetTraceCounter() {
        traceCounter.set(0);
    }

    @ManagedOperation(description = "Dumps the traced messages for the given node")
    public List<FabricTracerEventMessage> dumpTracedMessages(String nodeId) {
        ProcessorDefinition def = getTracedProcessorDefinition(nodeId);
        return dumpTracedMessages(def);
    }

    protected List<FabricTracerEventMessage> dumpTracedMessages(ProcessorDefinition def) {
        List<FabricTracerEventMessage> answer = new ArrayList<FabricTracerEventMessage>();

        if (def != null) {
            // TODO: maybe a BlockedQueue so we can drain it?
            Queue<FabricTracerEventMessage> queue = traces.get(def);
            if (queue != null) {
                answer.addAll(queue);
                queue.clear();
            }
        }

        return answer;
    }

    @ManagedOperation(description = "Dumps the traced messages for all nodes")
    public Map<String, List<FabricTracerEventMessage>> dumpAllTracedMessages() {
        Map<String, List<FabricTracerEventMessage>> answer = new HashMap<String, List<FabricTracerEventMessage>>();
        for (ProcessorDefinition def : traces.keySet()) {
            String id = def.getId();
            if (id == null) {
                NodeIdFactory idFactory = context.getNodeIdFactory();
                if (idFactory != null) {
                    id = def.idOrCreate(idFactory);
                }
            }
            if (id == null) {
                id = context.getName();
            }
            if (id != null) {
                List<FabricTracerEventMessage> queue = dumpTracedMessages(def);
                if (queue != null) {
                    answer.put(id, queue);
                }
            }
        }
        return answer;
    }

    long incrementTraceCounter() {
        return traceCounter.incrementAndGet();
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
        for (Queue<FabricTracerEventMessage> queue : traces.values()) {
            queue.clear();
        }
        traces.clear();
    }

    private ProcessorDefinition getTracedProcessorDefinition(String nodeId) {
        for (ProcessorDefinition def : traces.keySet()) {
            if (def.getId().equals(nodeId)) {
                return def;
            }
        }
        return null;
    }

}
