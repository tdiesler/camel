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
package org.apache.camel.processor.interceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.AggregateRouteNode;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultRouteNode;
import org.apache.camel.impl.DoCatchRouteNode;
import org.apache.camel.impl.DoFinallyRouteNode;
import org.apache.camel.impl.OnCompletionRouteNode;
import org.apache.camel.impl.OnExceptionRouteNode;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.CatchDefinition;
import org.apache.camel.model.FinallyDefinition;
import org.apache.camel.model.InterceptDefinition;
import org.apache.camel.model.OnCompletionDefinition;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.processor.Logger;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor for debugging and tracing routes
 *
 * @version $Revision$
 */
public class TraceInterceptor extends DelegateProcessor implements ExchangeFormatter {
    private static final transient Log LOG = LogFactory.getLog(TraceInterceptor.class);
    private static final String JPA_TRACE_EVENT_MESSAGE = "org.apache.camel.processor.interceptor.jpa.JpaTraceEventMessage";
    private Logger logger;
    private Producer traceEventProducer;
    private final ProcessorDefinition node;
    private final Tracer tracer;
    private TraceFormatter formatter;
    private Class<?> jpaTraceEventMessageClass;
    private RouteContext routeContext;

    public TraceInterceptor(ProcessorDefinition node, Processor target, TraceFormatter formatter, Tracer tracer) {
        super(target);
        this.tracer = tracer;
        this.node = node;
        this.formatter = formatter;
        this.logger = tracer.getLogger(this);
        if (tracer.getFormatter() != null) {
            this.formatter = tracer.getFormatter();
        }
    }

    @Override
    public String toString() {
        return "TraceInterceptor[" + node + "]";
    }

    public void setRouteContext(RouteContext routeContext) {
        this.routeContext = routeContext;
    }

    public void process(final Exchange exchange) throws Exception {
        // do not trace if tracing is disabled
        if (!tracer.isEnabled() || (routeContext != null && !routeContext.isTracing())) {
            super.proceed(exchange);
            return;
        }

        // interceptor will also trace routes supposed only for TraceEvents so we need to skip
        // logging TraceEvents to avoid infinite looping
        if (exchange.getProperty(Exchange.TRACE_EVENT, false, Boolean.class)) {
            // but we must still process to allow routing of TraceEvents to eg a JPA endpoint
            super.process(exchange);
            return;
        }

        boolean shouldLog = shouldLogNode(node) && shouldLogExchange(exchange);

        // whether we should trace it or not, some nodes should be skipped as they are abstract
        // intermediate steps for instance related to on completion
        boolean trace = true;

        // okay this is a regular exchange being routed we might need to log and trace
        try {
            // before
            if (shouldLog) {
                // traced holds the information about the current traced route path
                if (exchange.getUnitOfWork() != null) {
                    TracedRouteNodes traced = exchange.getUnitOfWork().getTracedRouteNodes();

                    if (node instanceof OnCompletionDefinition || node instanceof OnExceptionDefinition) {
                        // skip any of these as its just a marker definition
                        trace = false;
                    } else if (ProcessorDefinitionHelper.isFirstChildOfType(OnCompletionDefinition.class, node)) {
                        // special for on completion tracing
                        traceOnCompletion(traced, exchange);
                    } else if (ProcessorDefinitionHelper.isFirstChildOfType(OnExceptionDefinition.class, node)) {
                        // special for on exception
                        traceOnException(traced, exchange);
                    } else if (ProcessorDefinitionHelper.isFirstChildOfType(CatchDefinition.class, node)) {
                        // special for do catch
                        traceDoCatch(traced, exchange);
                    } else if (ProcessorDefinitionHelper.isFirstChildOfType(FinallyDefinition.class, node)) {
                        // special for do finally
                        traceDoFinally(traced, exchange);
                    } else if (ProcessorDefinitionHelper.isFirstChildOfType(AggregateDefinition.class, node)) {
                        // special for aggregate
                        traceAggregate(traced, exchange);
                    } else {
                        // regular so just add it
                        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
                    }
                } else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Cannot trace as this Exchange does not have an UnitOfWork: " + exchange);
                    }
                }
            }

            // log and trace the processor
            if (shouldLog && trace) {
                logExchange(exchange);
                traceExchange(exchange);
            }

            // special for interceptor where we need to keep booking how far we have routed in the intercepted processors
            if (node.getParent() instanceof InterceptDefinition && exchange.getUnitOfWork() != null) {
                TracedRouteNodes traced = exchange.getUnitOfWork().getTracedRouteNodes();
                traceIntercept((InterceptDefinition) node.getParent(), traced, exchange);
            }

            // process the exchange
            super.proceed(exchange);

            // after (trace out)
            if (shouldLog && tracer.isTraceOutExchanges()) {
                logExchange(exchange);
                traceExchange(exchange);
            }
        } catch (Exception e) {
            if (shouldLogException(exchange)) {
                logException(exchange, e);
            }
            throw e;
        }
    }

    private void traceOnCompletion(TracedRouteNodes traced, Exchange exchange) {
        traced.addTraced(new OnCompletionRouteNode());
        // do not log and trace as onCompletion should be a new event on its own
        // add the next step as well so we have onCompletion -> new step
        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
    }

    private void traceOnException(TracedRouteNodes traced, Exchange exchange) throws Exception {
        traced.addTraced(new DefaultRouteNode(traced.getLastNode().getProcessorDefinition(), traced.getLastNode().getProcessor()));
        traced.addTraced(new OnExceptionRouteNode());
        // log and trace so we have the from -> onException event as well
        logExchange(exchange);
        traceExchange(exchange);
        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
    }

    private void traceDoCatch(TracedRouteNodes traced, Exchange exchange) throws Exception {
        traced.addTraced(new DefaultRouteNode(traced.getLastNode().getProcessorDefinition(), traced.getLastNode().getProcessor()));
        traced.addTraced(new DoCatchRouteNode());
        // log and trace so we have the from -> doCatch event as well
        logExchange(exchange);
        traceExchange(exchange);
        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
    }

    private void traceDoFinally(TracedRouteNodes traced, Exchange exchange) throws Exception {
        traced.addTraced(new DefaultRouteNode(traced.getLastNode().getProcessorDefinition(), traced.getLastNode().getProcessor()));
        traced.addTraced(new DoFinallyRouteNode());
        // log and trace so we have the from -> doFinally event as well
        logExchange(exchange);
        traceExchange(exchange);
        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
    }

    private void traceAggregate(TracedRouteNodes traced, Exchange exchange) {
        traced.addTraced(new AggregateRouteNode((AggregateDefinition) node.getParent()));
        traced.addTraced(new DefaultRouteNode(node, super.getProcessor()));
    }

    protected void traceIntercept(InterceptDefinition intercept, TracedRouteNodes traced, Exchange exchange) throws Exception {
        // use the counter to get the index of the intercepted processor to be traced
        Processor last = intercept.getInterceptedProcessor(traced.getAndIncrementCounter(intercept));
        if (last != null) {
            traced.addTraced(new DefaultRouteNode(node, last));

            boolean shouldLog = shouldLogNode(node) && shouldLogExchange(exchange);
            if (shouldLog) {
                // log and trace the processor that was intercepted so we can see it
                logExchange(exchange);
                traceExchange(exchange);
            }
        }
    }

    public Object format(Exchange exchange) {
        return formatter.format(this, this.getNode(), exchange);
    }

    // Properties
    //-------------------------------------------------------------------------
    public ProcessorDefinition<?> getNode() {
        return node;
    }

    public Logger getLogger() {
        return logger;
    }

    public TraceFormatter getFormatter() {
        return formatter;
    }

    public Tracer getTracer() {
        return tracer;
    }

    protected void logExchange(Exchange exchange) {
        // process the exchange that formats and logs it
        logger.process(exchange);
    }

    @SuppressWarnings("unchecked")
    protected void traceExchange(Exchange exchange) throws Exception {
        // should we send a trace event to an optional destination?
        if (tracer.getDestination() != null || tracer.getDestinationUri() != null) {

            // create event exchange and add event information
            Date timestamp = new Date();
            Exchange event = new DefaultExchange(exchange);
            event.setProperty(Exchange.TRACE_EVENT_NODE_ID, node.getId());
            event.setProperty(Exchange.TRACE_EVENT_TIMESTAMP, timestamp);
            // keep a reference to the original exchange in case its needed
            event.setProperty(Exchange.TRACE_EVENT_EXCHANGE, exchange);

            // create event message to sent as in body containing event information such as
            // from node, to node, etc.
            TraceEventMessage msg = new DefaultTraceEventMessage(timestamp, node, exchange);

            // should we use ordinary or jpa objects
            if (tracer.isUseJpa()) {
                LOG.trace("Using class: " + JPA_TRACE_EVENT_MESSAGE + " for tracing event messages");

                // load the jpa event message class
                loadJpaTraceEventMessageClass(exchange);
                // create a new instance of the event message class
                Object jpa = ObjectHelper.newInstance(jpaTraceEventMessageClass);

                // copy options from event to jpa
                Map options = new HashMap();
                IntrospectionSupport.getProperties(msg, options, null);
                IntrospectionSupport.setProperties(jpa, options);
                // and set the timestamp as its not a String type
                IntrospectionSupport.setProperty(jpa, "timestamp", msg.getTimestamp());

                event.getIn().setBody(jpa);
            } else {
                event.getIn().setBody(msg);
            }

            // marker property to indicate its a tracing event being routed in case
            // new Exchange instances is created during trace routing so we can check
            // for this marker when interceptor also kick in during routing of trace events
            event.setProperty(Exchange.TRACE_EVENT, Boolean.TRUE);
            try {
                // process the trace route
                getTraceEventProducer(exchange).process(event);
            } catch (Exception e) {
                // log and ignore this as the original Exchange should be allowed to continue
                LOG.error("Error processing trace event (original Exchange will continue): " + event, e);
            }
        }
    }

    private synchronized void loadJpaTraceEventMessageClass(Exchange exchange) {
        if (jpaTraceEventMessageClass == null) {
            jpaTraceEventMessageClass = exchange.getContext().getClassResolver().resolveClass(JPA_TRACE_EVENT_MESSAGE);
            if (jpaTraceEventMessageClass == null) {
                throw new IllegalArgumentException("Cannot find class: " + JPA_TRACE_EVENT_MESSAGE
                        + ". Make sure camel-jpa.jar is in the classpath.");
            }
        }
    }

    protected void logException(Exchange exchange, Throwable throwable) {
        if (tracer.isTraceExceptions()) {
            if (tracer.isLogStackTrace()) {
                logger.process(exchange, throwable);
            } else {
                logger.process(exchange, ", Exception: " + throwable.toString());
            }
        }
    }

    /**
     * Returns true if the given exchange should be logged in the trace list
     */
    protected boolean shouldLogExchange(Exchange exchange) {
        return tracer.isEnabled() && (tracer.getTraceFilter() == null || tracer.getTraceFilter().matches(exchange));
    }

    /**
     * Returns true if the given exchange should be logged when an exception was thrown
     */
    protected boolean shouldLogException(Exchange exchange) {
        return tracer.isTraceExceptions();
    }

    /**
     * Returns whether exchanges coming out of processors should be traced
     */
    public boolean shouldTraceOutExchanges() {
        return tracer.isTraceOutExchanges();
    }

    /**
     * Returns true if the given node should be logged in the trace list
     */
    protected boolean shouldLogNode(ProcessorDefinition<?> node) {
        if (node == null) {
            return false;
        }
        if (!tracer.isTraceInterceptors() && (node instanceof InterceptStrategy)) {
            return false;
        }
        return true;
    }

    private synchronized Producer getTraceEventProducer(Exchange exchange) throws Exception {
        if (traceEventProducer == null) {
            // create producer when we have access the the camel context (we dont in doStart)
            Endpoint endpoint = tracer.getDestination() != null ? tracer.getDestination() : exchange.getContext().getEndpoint(tracer.getDestinationUri());
            traceEventProducer = endpoint.createProducer();
            ServiceHelper.startService(traceEventProducer);
        }
        return traceEventProducer;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        traceEventProducer = null;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (traceEventProducer != null) {
            ServiceHelper.stopService(traceEventProducer);
        }
    }

}
