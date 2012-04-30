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
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedOperation;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RouteDefinitionHelper;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.NodeIdFactory;
import org.apache.camel.support.ServiceSupport;

/**
 *
 */
@ManagedResource(description = "FabricTracer")
public class FabricTracer extends ServiceSupport implements InterceptStrategy {

    private final CamelContext camelContext;
    private boolean enabled;
    private final AtomicLong traceCounter = new AtomicLong(0);
    private Queue<FabricTracerEventMessage> queue =  new ArrayBlockingQueue<FabricTracerEventMessage>(1000);
    private int queueSize = 10;
    // remember the processors we are tracing, which we need later
    private final Set<ProcessorDefinition<?>> processors = new HashSet<ProcessorDefinition<?>>();

    public FabricTracer(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        // is this the first output from a route, as we want to know this so we can do special logic in first
        boolean first = false;
        RouteDefinition route = ProcessorDefinitionHelper.getRoute(definition);
        if (route != null && !route.getOutputs().isEmpty()) {
            first = route.getOutputs().get(0) == definition;
        }

        processors.add(definition);
        return new FabricTraceProcessor(queue, target, definition, route, first, this);
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
        // okay tracer is enabled then force auto assigning ids
        if (enabled) {
            forceAutoAssigningIds();
        }
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
        List<FabricTracerEventMessage> answer = new ArrayList<FabricTracerEventMessage>();
        if (nodeId != null) {
            for (FabricTracerEventMessage message : queue) {
                if (nodeId.equals(message.getToNode())) {
                    answer.add(message);
                }
            }
        }
        return answer;
    }

    @ManagedOperation(description = "Dumps the traced messages for the given node in xml format")
    public String dumpTracedMessagesAsXml(String nodeId) {
        List<FabricTracerEventMessage> events = dumpTracedMessages(nodeId);

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(FabricTracerEventMessage.ROOT_TAG).append("s>");
        for (FabricTracerEventMessage event : events) {
            sb.append("\n").append(event.toXml());
        }
        sb.append("\n</").append(FabricTracerEventMessage.ROOT_TAG).append("s>");
        return sb.toString();
    }

    @ManagedOperation(description = "Dumps the traced messages for all nodes")
    public List<FabricTracerEventMessage> dumpAllTracedMessages() {
        List<FabricTracerEventMessage> answer = new ArrayList<FabricTracerEventMessage>();
        answer.addAll(queue);
        queue.clear();
        return answer;
    }

    @ManagedOperation(description = "Dumps the traced messages for all nodes in xml format")
    public String dumpAllTracedMessagesAsXml() {
        List<FabricTracerEventMessage> events = dumpAllTracedMessages();

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(FabricTracerEventMessage.ROOT_TAG).append("s>");
        for (FabricTracerEventMessage event : events) {
            sb.append("\n").append(event.toXml());
        }
        sb.append("\n</").append(FabricTracerEventMessage.ROOT_TAG).append("s>");
        return sb.toString();
    }

    long incrementTraceCounter() {
        return traceCounter.incrementAndGet();
    }
    
    void stopProcessor(FabricTraceProcessor processor, ProcessorDefinition<?> processorDefinition) {
        this.processors.remove(processorDefinition);
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
        queue.clear();
    }

    @Override
    protected void doShutdown() throws Exception {
        queue.clear();
        processors.clear();
    }

    private void forceAutoAssigningIds() {
        NodeIdFactory factory = camelContext.getNodeIdFactory();
        if (factory != null) {
            for (ProcessorDefinition<?> child : processors) {
                // ensure also the children get ids assigned
                RouteDefinitionHelper.forceAssignIds(camelContext, child);
            }
        }
    }

}
