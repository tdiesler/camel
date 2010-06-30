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
package org.apache.camel.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Navigate;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.impl.converter.AsyncProcessorTypeConverter;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.EventHelper;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.camel.util.StopWatch;
import org.apache.camel.util.concurrent.AtomicExchange;
import org.apache.camel.util.concurrent.SubmitOrderedCompletionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * Implements the Multicast pattern to send a message exchange to a number of
 * endpoints, each endpoint receiving a copy of the message exchange.
 *
 * @see Pipeline
 * @version $Revision$
 */
public class MulticastProcessor extends ServiceSupport implements AsyncProcessor, Navigate<Processor>, Traceable {

    private static final transient Log LOG = LogFactory.getLog(MulticastProcessor.class);

    /**
     * Class that represent each step in the multicast route to do
     */
    static final class DefaultProcessorExchangePair implements ProcessorExchangePair {
        private final int index;
        private final Processor processor;
        private final Processor prepared;
        private final Exchange exchange;

        private DefaultProcessorExchangePair(int index, Processor processor, Processor prepared, Exchange exchange) {
            this.index = index;
            this.processor = processor;
            this.prepared = prepared;
            this.exchange = exchange;
        }

        public int getIndex() {
            return index;
        }

        public Exchange getExchange() {
            return exchange;
        }

        public Producer getProducer() {
            if (processor instanceof Producer) {
                return (Producer) processor;
            }
            return null;
        }

        public Processor getProcessor() {
            return prepared;
        }

        public void begin() {
            // noop
            LOG.trace("ProcessorExchangePair #" + index + " begin: " + exchange);
        }

        public void done() {
            // noop
            LOG.trace("ProcessorExchangePair #" + index + " done: " + exchange);
        }

    }

    private final CamelContext camelContext;
    private Collection<Processor> processors;
    private final AggregationStrategy aggregationStrategy;
    private final boolean parallelProcessing;
    private final boolean streaming;
    private final boolean stopOnException;
    private final ExecutorService executorService;

    public MulticastProcessor(CamelContext camelContext, Collection<Processor> processors) {
        this(camelContext, processors, null);
    }

    public MulticastProcessor(CamelContext camelContext, Collection<Processor> processors, AggregationStrategy aggregationStrategy) {
        this(camelContext, processors, aggregationStrategy, false, null, false, false);
    }
    
    public MulticastProcessor(CamelContext camelContext, Collection<Processor> processors, AggregationStrategy aggregationStrategy,
                              boolean parallelProcessing, ExecutorService executorService, boolean streaming, boolean stopOnException) {
        notNull(camelContext, "camelContext");
        this.camelContext = camelContext;
        this.processors = processors;
        this.aggregationStrategy = aggregationStrategy;
        this.executorService = executorService;
        this.streaming = streaming;
        this.stopOnException = stopOnException;
        // must enable parallel if executor service is provided
        this.parallelProcessing = parallelProcessing || executorService != null;
    }

    @Override
    public String toString() {
        return "Multicast[" + getProcessors() + "]";
    }

    public String getTraceLabel() {
        return "multicast";
    }
    
    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        final AtomicExchange result = new AtomicExchange();
        final Iterable<ProcessorExchangePair> pairs;

        // multicast uses fine grained error handling on the output processors
        // so use try .. catch to cater for this
        try {
            boolean sync = true;

            pairs = createProcessorExchangePairs(exchange);
            if (isParallelProcessing()) {
                // ensure an executor is set when running in parallel
                ObjectHelper.notNull(executorService, "executorService", this);
                doProcessParallel(exchange, result, pairs, isStreaming(), callback);
            } else {
                sync = doProcessSequential(exchange, result, pairs, callback);
            }

            if (!sync) {
                // the remainder of the multicast will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }
        } catch (Throwable e) {
            exchange.setException(e);
            // and do the done work
            doDone(exchange, null, callback, true);
            return true;
        }

        // multicasting was processed successfully
        // and do the done work
        Exchange subExchange = result.get() != null ? result.get() : null;
        doDone(exchange, subExchange, callback, true);
        return true;
    }

    protected void doProcessParallel(final Exchange original, final AtomicExchange result, Iterable<ProcessorExchangePair> pairs,
                                     boolean streaming, final AsyncCallback callback) throws InterruptedException, ExecutionException {
        final CompletionService<Exchange> completion;
        final AtomicBoolean running = new AtomicBoolean(true);

        if (streaming) {
            // execute tasks in parallel+streaming and aggregate in the order they are finished (out of order sequence)
            completion = new ExecutorCompletionService<Exchange>(executorService);
        } else {
            // execute tasks in parallel and aggregate in the order the tasks are submitted (in order sequence)
            completion = new SubmitOrderedCompletionService<Exchange>(executorService);
        }

        final AtomicInteger total = new AtomicInteger(0);

        final Iterator<ProcessorExchangePair> it = pairs.iterator();
        while (it.hasNext()) {
            final ProcessorExchangePair pair = it.next();
            final Exchange subExchange = pair.getExchange();
            updateNewExchange(subExchange, total.intValue(), it);

            completion.submit(new Callable<Exchange>() {
                public Exchange call() throws Exception {
                    if (!running.get()) {
                        // do not start processing the task if we are not running
                        return subExchange;
                    }

                    doProcess(original, result, it, pair, callback, total);

                    // should we stop in case of an exception occurred during processing?
                    if (stopOnException && subExchange.getException() != null) {
                        // signal to stop running
                        running.set(false);
                        throw new CamelExchangeException("Parallel processing failed for number " + total.intValue(), subExchange, subExchange.getException());
                    }

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Parallel processing complete for exchange: " + subExchange);
                    }
                    return subExchange;
                }
            });

            total.incrementAndGet();
        }

        for (int i = 0; i < total.intValue(); i++) {
            Future<Exchange> future = completion.take();
            Exchange subExchange = future.get();
            doAggregate(getAggregationStrategy(subExchange), result, subExchange);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done parallel processing " + total + " exchanges");
        }
    }

    protected boolean doProcessSequential(Exchange original, AtomicExchange result, Iterable<ProcessorExchangePair> pairs, AsyncCallback callback) throws Exception {
        AtomicInteger total = new AtomicInteger();
        Iterator<ProcessorExchangePair> it = pairs.iterator();

        while (it.hasNext()) {
            ProcessorExchangePair pair = it.next();
            Exchange subExchange = pair.getExchange();
            updateNewExchange(subExchange, total.get(), it);

            boolean sync = doProcess(original, result, it, pair, callback, total);
            if (!sync) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Processing exchangeId: " + pair.getExchange().getExchangeId() + " is continued being processed asynchronously");
                }
                // the remainder of the multicast will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Processing exchangeId: " + pair.getExchange().getExchangeId() + " is continued being processed synchronously");
            }

            // should we stop in case of an exception occurred during processing?
            if (stopOnException && subExchange.getException() != null) {
                throw new CamelExchangeException("Sequential processing failed for number " + total.get(), subExchange, subExchange.getException());
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Sequential processing complete for number " + total + " exchange: " + subExchange);
            }

            doAggregate(getAggregationStrategy(subExchange), result, subExchange);
            total.incrementAndGet();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done sequential processing " + total + " exchanges");
        }

        return true;
    }

    private boolean doProcess(final Exchange original, final AtomicExchange result, final Iterator<ProcessorExchangePair> it,
                              final ProcessorExchangePair pair, final AsyncCallback callback, final AtomicInteger total) {
        boolean sync = true;

        final Exchange exchange = pair.getExchange();
        Processor processor = pair.getProcessor();
        Producer producer = pair.getProducer();

        TracedRouteNodes traced = exchange.getUnitOfWork() != null ? exchange.getUnitOfWork().getTracedRouteNodes() : null;

        // compute time taken if sending to another endpoint
        StopWatch watch = null;
        if (producer != null) {
            watch = new StopWatch();
        }

        try {
            // prepare tracing starting from a new block
            if (traced != null) {
                traced.pushBlock();
            }

            // let the prepared process it, remember to begin the exchange pair
            AsyncProcessor async = AsyncProcessorTypeConverter.convert(processor);
            pair.begin();
            sync = async.process(exchange, new AsyncCallback() {
                public void done(boolean doneSync) {
                    // we are done with the exchange pair
                    pair.done();

                    // we only have to handle async completion of the routing slip
                    if (doneSync) {
                        return;
                    }

                    // continue processing the multicast asynchronously
                    Exchange subExchange = exchange;

                    // remember to test for stop on exception and aggregate before copying back results
                    if (stopOnException && subExchange.getException() != null) {
                        // wrap in exception to explain where it failed
                        subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, subExchange.getException()));
                        // and do the done work
                        doDone(original, subExchange, callback, false);
                        return;
                    }

                    try {
                        doAggregate(getAggregationStrategy(subExchange), result, subExchange);
                    } catch (Throwable e) {
                        // wrap in exception to explain where it failed
                        subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, e));
                        // and do the done work
                        doDone(original, subExchange, callback, false);
                        return;
                    }

                    total.incrementAndGet();

                    // maybe there are more processors to multicast
                    while (it.hasNext()) {

                        // prepare and run the next
                        ProcessorExchangePair pair = it.next();
                        subExchange = pair.getExchange();
                        updateNewExchange(subExchange, total.get(), it);
                        boolean sync = doProcess(original, result, it, pair, callback, total);

                        if (!sync) {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Processing exchangeId: " + original.getExchangeId() + " is continued being processed asynchronously");
                            }
                            return;
                        }

                        if (stopOnException && subExchange.getException() != null) {
                            // wrap in exception to explain where it failed
                            subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, subExchange.getException()));
                            // and do the done work
                            doDone(original, subExchange, callback, false);
                            return;
                        }

                        try {
                            doAggregate(getAggregationStrategy(subExchange), result, subExchange);
                        } catch (Throwable e) {
                            // wrap in exception to explain where it failed
                            subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, e));
                            // and do the done work
                            doDone(original, subExchange, callback, false);
                            return;
                        }

                        total.incrementAndGet();
                    }

                    // do the done work
                    subExchange = result.get() != null ? result.get() : null;
                    doDone(original, subExchange, callback, false);
                }
            });
        } finally {
            // pop the block so by next round we have the same staring point and thus the tracing looks accurate
            if (traced != null) {
                traced.popBlock();
            }
            if (producer != null) {
                long timeTaken = watch.stop();
                Endpoint endpoint = producer.getEndpoint();
                // emit event that the exchange was sent to the endpoint
                EventHelper.notifyExchangeSent(exchange.getContext(), exchange, endpoint, timeTaken);
            }
        }

        return sync;
    }

    /**
     * Common work which must be done when we are done multicasting.
     * <p/>
     * This logic applies for both running synchronous and asynchronous as there are multiple exist points
     * when using the asynchronous routing engine. And therefore we want the logic in one method instead
     * of being scattered.
     *
     * @param original      the original exchange
     * @param subExchange   the current sub exchange, can be <tt>null</tt> for the synchronous part
     * @param callback      the callback
     * @param doneSync      the <tt>doneSync</tt> parameter to call on callback
     */
    protected void doDone(Exchange original, Exchange subExchange, AsyncCallback callback, boolean doneSync) {
        // cleanup any per exchange aggregation strategy
        original.removeProperty(Exchange.AGGREGATION_STRATEGY);
        if (original.getException() != null) {
            // multicast uses error handling on its output processors and they have tried to redeliver
            // so we shall signal back to the other error handlers that we are exhausted and they should not
            // also try to redeliver as we will then do that twice
            original.setProperty(Exchange.REDELIVERY_EXHAUSTED, Boolean.TRUE);
        }
        if (subExchange != null) {
            // and copy the current result to original so it will contain this exception
            ExchangeHelper.copyResults(original, subExchange);
        }
        callback.done(doneSync);
    }

    /**
     * Aggregate the {@link Exchange} with the current result
     *
     * @param strategy the aggregation strategy to use
     * @param result the current result
     * @param exchange the exchange to be added to the result
     */
    protected synchronized void doAggregate(AggregationStrategy strategy, AtomicExchange result, Exchange exchange) {
        if (strategy != null) {
            // prepare the exchanges for aggregation
            Exchange oldExchange = result.get();
            ExchangeHelper.prepareAggregation(oldExchange, exchange);
            result.set(strategy.aggregate(oldExchange, exchange));
        }
    }

    protected void updateNewExchange(Exchange exchange, int index, Iterator<ProcessorExchangePair> allPairs) {
        exchange.setProperty(Exchange.MULTICAST_INDEX, index);
        if (allPairs.hasNext()) {
            exchange.setProperty(Exchange.MULTICAST_COMPLETE, Boolean.FALSE);
        } else {
            exchange.setProperty(Exchange.MULTICAST_COMPLETE, Boolean.TRUE);
        }
    }

    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) throws Exception {
        List<ProcessorExchangePair> result = new ArrayList<ProcessorExchangePair>(processors.size());

        int index = 0;
        for (Processor processor : processors) {
            Exchange copy = exchange.copy();
            result.add(createProcessorExchangePair(index++, processor, copy));
        }

        return result;
    }

    /**
     * Creates the {@link ProcessorExchangePair} which holds the processor and exchange to be send out.
     * <p/>
     * You <b>must</b> use this method to create the instances of {@link ProcessorExchangePair} as they
     * need to be specially prepared before use.
     *
     * @param processor  the processor
     * @param exchange   the exchange
     * @return prepared for use
     */
    protected ProcessorExchangePair createProcessorExchangePair(int index, Processor processor, Exchange exchange) {
        Processor prepared = processor;

        // set property which endpoint we send to
        setToEndpoint(exchange, prepared);

        // rework error handling to support fine grained error handling
        if (exchange.getUnitOfWork() != null && exchange.getUnitOfWork().getRouteContext() != null) {
            // wrap the producer in error handler so we have fine grained error handling on
            // the output side instead of the input side
            // this is needed to support redelivery on that output alone and not doing redelivery
            // for the entire multicast block again which will start from scratch again
            RouteContext routeContext = exchange.getUnitOfWork().getRouteContext();
            ErrorHandlerBuilder builder = routeContext.getRoute().getErrorHandlerBuilder();
            // create error handler (create error handler directly to keep it light weight,
            // instead of using ProcessorDefinition.wrapInErrorHandler)
            try {
                prepared = builder.createErrorHandler(routeContext, prepared);
            } catch (Exception e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        return new DefaultProcessorExchangePair(index, processor, prepared, exchange);
    }

    protected void doStart() throws Exception {
        if (isParallelProcessing() && executorService == null) {
            throw new IllegalArgumentException("ParallelProcessing is enabled but ExecutorService has not been set");
        }
        ServiceHelper.startServices(processors);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopServices(processors);
    }

    protected static void setToEndpoint(Exchange exchange, Processor processor) {
        if (processor instanceof Producer) {
            Producer producer = (Producer) processor;
            exchange.setProperty(Exchange.TO_ENDPOINT, producer.getEndpoint().getEndpointUri());
        }
    }

    protected AggregationStrategy getAggregationStrategy(Exchange exhange) {
        // prefer to use per Exchange aggregation strategy over a global strategy
        AggregationStrategy answer = exhange.getProperty(Exchange.AGGREGATION_STRATEGY, AggregationStrategy.class);
        if (answer == null) {
            // fallback to global strategy
            answer = getAggregationStrategy();
        }
        return answer;
    }

    /**
     * Is the multicast processor working in streaming mode?
     * 
     * In streaming mode:
     * <ul>
     * <li>we use {@link Iterable} to ensure we can send messages as soon as the data becomes available</li>
     * <li>for parallel processing, we start aggregating responses as they get send back to the processor;
     * this means the {@link org.apache.camel.processor.aggregate.AggregationStrategy} has to take care of handling out-of-order arrival of exchanges</li>
     * </ul>
     */
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Should the multicast processor stop processing further exchanges in case of an exception occurred?
     */
    public boolean isStopOnException() {
        return stopOnException;
    }

    /**
     * Returns the producers to multicast to
     */
    public Collection<Processor> getProcessors() {
        return processors;
    }

    /**
     * Use {@link #getAggregationStrategy(org.apache.camel.Exchange)} instead.
     */
    public AggregationStrategy getAggregationStrategy() {
        return aggregationStrategy;
    }

    public boolean isParallelProcessing() {
        return parallelProcessing;
    }

    public List<Processor> next() {
        if (!hasNext()) {
            return null;
        }
        return new ArrayList<Processor>(processors);
    }

    public boolean hasNext() {
        return processors != null && !processors.isEmpty();
    }
}
