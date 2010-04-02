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
package org.apache.camel.processor.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Navigate;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.impl.LoggingExceptionHandler;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.processor.Traceable;
import org.apache.camel.spi.AggregationRepository;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.spi.RecoverableAggregationRepository;
import org.apache.camel.util.DefaultTimeoutMap;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.LRUCache;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.camel.util.TimeoutMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the <a
 * href="http://camel.apache.org/aggregator2.html">Aggregator</a>
 * pattern where a batch of messages are processed (up to a maximum amount or
 * until some timeout is reached) and messages for the same correlation key are
 * combined together using some kind of {@link AggregationStrategy}
 * (by default the latest message is used) to compress many message exchanges
 * into a smaller number of exchanges.
 * <p/>
 * A good example of this is stock market data; you may be receiving 30,000
 * messages/second and you may want to throttle it right down so that multiple
 * messages for the same stock are combined (or just the latest message is used
 * and older prices are discarded). Another idea is to combine line item messages
 * together into a single invoice message.
 *
 * @version $Revision$
 */
public class AggregateProcessor extends ServiceSupport implements Processor, Navigate<Processor>, Traceable {

    private static final Log LOG = LogFactory.getLog(AggregateProcessor.class);

    private final CamelContext camelContext;
    private final Processor processor;
    private final AggregationStrategy aggregationStrategy;
    private final Expression correlationExpression;
    private final ExecutorService executorService;
    private ScheduledExecutorService recoverService;
    private TimeoutMap<Object, Exchange> timeoutMap;
    private ExceptionHandler exceptionHandler;
    private AggregationRepository<Object> aggregationRepository = new MemoryAggregationRepository();
    private Map<Object, Object> closedCorrelationKeys;
    private final Set<String> inProgressCompleteExchanges = new HashSet<String>();

    // options
    private boolean ignoreBadCorrelationKeys;
    private Integer closeCorrelationKeyOnCompletion;
    private boolean parallelProcessing;

    // different ways to have completion triggered
    private boolean eagerCheckCompletion;
    private Predicate completionPredicate;
    private long completionTimeout;
    private Expression completionTimeoutExpression;
    private int completionSize;
    private Expression completionSizeExpression;
    private boolean completionFromBatchConsumer;
    private AtomicInteger batchConsumerCounter = new AtomicInteger();

    public AggregateProcessor(CamelContext camelContext, Processor processor,
                              Expression correlationExpression, AggregationStrategy aggregationStrategy,
                              ExecutorService executorService) {
        ObjectHelper.notNull(camelContext, "camelContext");
        ObjectHelper.notNull(processor, "processor");
        ObjectHelper.notNull(correlationExpression, "correlationExpression");
        ObjectHelper.notNull(aggregationStrategy, "aggregationStrategy");
        ObjectHelper.notNull(executorService, "executorService");
        this.camelContext = camelContext;
        this.processor = processor;
        this.correlationExpression = correlationExpression;
        this.aggregationStrategy = aggregationStrategy;
        this.executorService = executorService;
    }

    @Override
    public String toString() {
        return "AggregateProcessor[to: " + processor + "]";
    }

    public String getTraceLabel() {
        return "aggregate[" + correlationExpression + "]";
    }

    public List<Processor> next() {
        if (!hasNext()) {
            return null;
        }
        List<Processor> answer = new ArrayList<Processor>(1);
        answer.add(processor);
        return answer;
    }

    public boolean hasNext() {
        return processor != null;
    }

    public void process(Exchange exchange) throws Exception {
        // compute correlation expression
        Object key = correlationExpression.evaluate(exchange, Object.class);
        if (ObjectHelper.isEmpty(key)) {
            // we have a bad correlation key
            if (isIgnoreBadCorrelationKeys()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Correlation key could not be evaluated to a value. Exchange will be ignored: " + exchange);
                }
                return;
            } else {
                throw new CamelExchangeException("Correlation key could not be evaluated to a value", exchange);
            }
        }

        // is the correlation key closed?
        if (closedCorrelationKeys != null && closedCorrelationKeys.containsKey(key)) {
            throw new ClosedCorrelationKeyException(key, exchange);
        }

        doAggregation(key, exchange);
    }

    /**
     * Aggregates the exchange with the given correlation key
     * <p/>
     * This method <b>must</b> be run synchronized as we cannot aggregate the same correlation key
     * in parallel.
     *
     * @param key      the correlation key
     * @param exchange the exchange
     * @return the aggregated exchange
     */
    private synchronized Exchange doAggregation(Object key, Exchange exchange) {
        // when memory based then its fast using synchronized, but if the aggregation repository is IO
        // bound such as JPA etc then concurrent aggregation per correlation key could
        // improve performance as we can run aggregation repository get/add in parallel

        if (LOG.isTraceEnabled()) {
            LOG.trace("onAggregation +++ start +++ with correlation key: " + key);
        }

        Exchange answer;
        Exchange oldExchange = aggregationRepository.get(exchange.getContext(), key);
        Exchange newExchange = exchange;

        Integer size = 1;
        if (oldExchange != null) {
            size = oldExchange.getProperty(Exchange.AGGREGATED_SIZE, 0, Integer.class);
            size++;
        }

        // check if we are complete
        boolean complete = false;
        if (isEagerCheckCompletion()) {
            // put the current aggregated size on the exchange so its avail during completion check
            newExchange.setProperty(Exchange.AGGREGATED_SIZE, size);
            complete = isCompleted(key, newExchange);
            // remove it afterwards
            newExchange.removeProperty(Exchange.AGGREGATED_SIZE);
        }

        // prepare the exchanges for aggregation and aggregate it
        ExchangeHelper.prepareAggregation(oldExchange, newExchange);
        answer = onAggregation(oldExchange, exchange);
        answer.setProperty(Exchange.AGGREGATED_SIZE, size);

        // maybe we should check completion after the aggregation
        if (!isEagerCheckCompletion()) {
            // put the current aggregated size on the exchange so its avail during completion check
            answer.setProperty(Exchange.AGGREGATED_SIZE, size);
            complete = isCompleted(key, answer);
        }

        // only need to update aggregation repository if we are not complete
        if (!complete) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("In progress aggregated exchange: " + answer + " with correlation key:" + key);
            }
            aggregationRepository.add(exchange.getContext(), key, answer);
        }

        if (complete) {
            onCompletion(key, answer, false);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("onAggregation +++  end  +++ with correlation key: " + key);
        }

        return answer;
    }

    protected boolean isCompleted(Object key, Exchange exchange) {
        if (getCompletionPredicate() != null) {
            boolean answer = getCompletionPredicate().matches(exchange);
            if (answer) {
                exchange.setProperty(Exchange.AGGREGATED_COMPLETED_BY, "predicate");
                return true;
            }
        }

        if (getCompletionSizeExpression() != null) {
            Integer value = getCompletionSizeExpression().evaluate(exchange, Integer.class);
            if (value != null && value > 0) {
                int size = exchange.getProperty(Exchange.AGGREGATED_SIZE, 1, Integer.class);
                if (size >= value) {
                    exchange.setProperty(Exchange.AGGREGATED_COMPLETED_BY, "size");
                    return true;
                }
            }
        }
        if (getCompletionSize() > 0) {
            int size = exchange.getProperty(Exchange.AGGREGATED_SIZE, 1, Integer.class);
            if (size >= getCompletionSize()) {
                exchange.setProperty(Exchange.AGGREGATED_COMPLETED_BY, "size");
                return true;
            }
        }

        // timeout can be either evaluated based on an expression or from a fixed value
        // expression takes precedence
        boolean timeoutSet = false;
        if (getCompletionTimeoutExpression() != null) {
            Long value = getCompletionTimeoutExpression().evaluate(exchange, Long.class);
            if (value != null && value > 0) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Updating correlation key " + key + " to timeout after "
                            + value + " ms. as exchange received: " + exchange);
                }
                timeoutMap.put(key, exchange, value);
                timeoutSet = true;
            }
        }
        if (!timeoutSet && getCompletionTimeout() > 0) {
            // timeout is used so use the timeout map to keep an eye on this
            if (LOG.isTraceEnabled()) {
                LOG.trace("Updating correlation key " + key + " to timeout after "
                        + getCompletionTimeout() + " ms. as exchange received: " + exchange);
            }
            timeoutMap.put(key, exchange, getCompletionTimeout());
        }

        if (isCompletionFromBatchConsumer()) {
            batchConsumerCounter.incrementAndGet();
            int size = exchange.getProperty(Exchange.BATCH_SIZE, 0, Integer.class);
            if (size > 0 && batchConsumerCounter.intValue() >= size) {
                // batch consumer is complete then reset the counter
                batchConsumerCounter.set(0);
                exchange.setProperty(Exchange.AGGREGATED_COMPLETED_BY, "consumer");
                return true;
            }
        }

        return false;
    }

    protected Exchange onAggregation(Exchange oldExchange, Exchange newExchange) {
        return aggregationStrategy.aggregate(oldExchange, newExchange);
    }

    protected void onCompletion(final Object key, final Exchange exchange, boolean fromTimeout) {
        // store the correlation key as property
        exchange.setProperty(Exchange.AGGREGATED_CORRELATION_KEY, key);
        // remove from repository as its completed
        aggregationRepository.remove(exchange.getContext(), key, exchange);
        if (!fromTimeout && timeoutMap != null) {
            // cleanup timeout map if it was a incoming exchange which triggered the timeout (and not the timeout checker)
            timeoutMap.remove(key);
        }

        // this key has been closed so add it to the closed map
        if (closedCorrelationKeys != null) {
            closedCorrelationKeys.put(key, key);
        }

        onSubmitCompletion(key, exchange);
    }

    private void onSubmitCompletion(final Object key, final Exchange exchange) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Aggregation complete for correlation key " + key + " sending aggregated exchange: " + exchange);
        }

        // add this as in progress
        inProgressCompleteExchanges.add(exchange.getExchangeId());

        // send this exchange
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    processor.process(exchange);
                } catch (Exception e) {
                    exchange.setException(e);
                } catch (Throwable t) {
                    // must catch throwable so we will handle all exceptions as the executor service will by default ignore them
                    exchange.setException(new CamelExchangeException("Error processing aggregated exchange", exchange, t));
                }

                try {
                    // was it good or bad?
                    if (exchange.getException() == null) {
                        // only confirm if we processed without a problem
                        aggregationRepository.confirm(exchange.getContext(), exchange.getExchangeId());
                    } else {
                        // if there was an exception then let the exception handler handle it
                        getExceptionHandler().handleException("Error processing aggregated exchange", exchange, exchange.getException());
                    }
                } finally {
                    // must remember to remove when we are done
                    inProgressCompleteExchanges.remove(exchange.getExchangeId());
                }
            }
        });
    }

    public Predicate getCompletionPredicate() {
        return completionPredicate;
    }

    public void setCompletionPredicate(Predicate completionPredicate) {
        this.completionPredicate = completionPredicate;
    }

    public boolean isEagerCheckCompletion() {
        return eagerCheckCompletion;
    }

    public void setEagerCheckCompletion(boolean eagerCheckCompletion) {
        this.eagerCheckCompletion = eagerCheckCompletion;
    }

    public long getCompletionTimeout() {
        return completionTimeout;
    }

    public void setCompletionTimeout(long completionTimeout) {
        this.completionTimeout = completionTimeout;
    }

    public Expression getCompletionTimeoutExpression() {
        return completionTimeoutExpression;
    }

    public void setCompletionTimeoutExpression(Expression completionTimeoutExpression) {
        this.completionTimeoutExpression = completionTimeoutExpression;
    }

    public int getCompletionSize() {
        return completionSize;
    }

    public void setCompletionSize(int completionSize) {
        this.completionSize = completionSize;
    }

    public Expression getCompletionSizeExpression() {
        return completionSizeExpression;
    }

    public void setCompletionSizeExpression(Expression completionSizeExpression) {
        this.completionSizeExpression = completionSizeExpression;
    }

    public boolean isIgnoreBadCorrelationKeys() {
        return ignoreBadCorrelationKeys;
    }

    public void setIgnoreBadCorrelationKeys(boolean ignoreBadCorrelationKeys) {
        this.ignoreBadCorrelationKeys = ignoreBadCorrelationKeys;
    }

    public Integer getCloseCorrelationKeyOnCompletion() {
        return closeCorrelationKeyOnCompletion;
    }

    public void setCloseCorrelationKeyOnCompletion(Integer closeCorrelationKeyOnCompletion) {
        this.closeCorrelationKeyOnCompletion = closeCorrelationKeyOnCompletion;
    }

    public boolean isCompletionFromBatchConsumer() {
        return completionFromBatchConsumer;
    }

    public void setCompletionFromBatchConsumer(boolean completionFromBatchConsumer) {
        this.completionFromBatchConsumer = completionFromBatchConsumer;
    }

    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new LoggingExceptionHandler(getClass());
        }
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public boolean isParallelProcessing() {
        return parallelProcessing;
    }

    public void setParallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;
    }

    public AggregationRepository<Object> getAggregationRepository() {
        return aggregationRepository;
    }

    public void setAggregationRepository(AggregationRepository<Object> aggregationRepository) {
        this.aggregationRepository = aggregationRepository;
    }

    /**
     * Background task that looks for aggregated exchanges which is triggered by completion timeouts.
     */
    private final class AggregationTimeoutMap extends DefaultTimeoutMap<Object, Exchange> {

        private AggregationTimeoutMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
            super(executor, requestMapPollTimeMillis);
        }

        @Override
        public void onEviction(Object key, Exchange exchange) {
            if (log.isDebugEnabled()) {
                log.debug("Completion timeout triggered for correlation key: " + key);
            }

            exchange.setProperty(Exchange.AGGREGATED_COMPLETED_BY, "timeout");
            onCompletion(key, exchange, true);
        }
    }

    /**
     * Background task that looks for aggregated exchanges to recover.
     */
    private final class RecoverTask implements Runnable {
        private final RecoverableAggregationRepository<Object> recoverable;

        private RecoverTask(RecoverableAggregationRepository<Object> recoverable) {
            this.recoverable = recoverable;
        }

        public void run() {
            AggregateProcessor.this.doRecover(recoverable);
        }

    }

    private void doRecover(RecoverableAggregationRepository<Object> recoverable) {
        LOG.trace("Starting recover check");

        Set<String> exchangeIds = recoverable.scan(camelContext);
        for (String exchangeId : exchangeIds) {

            // we may shutdown while doing recovery
            if (!isRunAllowed()) {
                LOG.info("We are shutting down so stop recovering");
                return;
            }

            boolean inProgress = inProgressCompleteExchanges.contains(exchangeId);
            if (inProgress) {
                LOG.debug("Aggregated exchange with id " + exchangeId + " is already in progress");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Recovering aggregated exchange with id " + exchangeId);
                }
                Exchange exchange = recoverable.recover(camelContext, exchangeId);
                if (exchange != null) {
                    // get the correlation key
                    String key = exchange.getProperty(Exchange.AGGREGATED_CORRELATION_KEY, String.class);
                    // resubmit the recovered exchange
                    onSubmitCompletion(key, exchange);
                }
            }
        }

        LOG.trace("Recover check complete");
    }

    @Override
    protected void doStart() throws Exception {
        if (getCompletionTimeout() <= 0 && getCompletionSize() <= 0 && getCompletionPredicate() == null
                && !isCompletionFromBatchConsumer() && getCompletionTimeoutExpression() == null
                && getCompletionSizeExpression() == null) {
            throw new IllegalStateException("At least one of the completions options"
                    + " [completionTimeout, completionSize, completionPredicate, completionFromBatchConsumer] must be set");
        }

        if (getCloseCorrelationKeyOnCompletion() != null) {
            if (getCloseCorrelationKeyOnCompletion() > 0) {
                LOG.info("Using ClosedCorrelationKeys with a LRUCache with a capacity of " + getCloseCorrelationKeyOnCompletion());
                closedCorrelationKeys = new LRUCache<Object, Object>(getCloseCorrelationKeyOnCompletion());
            } else {
                LOG.info("Using ClosedCorrelationKeys with unbounded capacity");
                closedCorrelationKeys = new HashMap<Object, Object>();
            }

        }

        ServiceHelper.startService(aggregationRepository);

        // should we use recover checker
        if (aggregationRepository instanceof RecoverableAggregationRepository) {
            RecoverableAggregationRepository<Object> recoverable = (RecoverableAggregationRepository<Object>) aggregationRepository;
            if (recoverable.isUseRecovery()) {
                long interval = recoverable.getCheckIntervalInMillis();
                if (interval > 0) {
                    // create a background recover thread to check once ev
                    recoverService = camelContext.getExecutorServiceStrategy().newScheduledThreadPool(this, "AggregateRecoverChecker", 1);
                    Runnable recoverTask = new RecoverTask(recoverable);
                    LOG.info("Scheduling recover checker to run every " + interval + " millis.");
                    recoverService.scheduleAtFixedRate(recoverTask, 1000L, interval, TimeUnit.MILLISECONDS);
                } else {
                    // its a one shot recover during startup
                    LOG.info("Running recover checker once at startup to recover existing aggregated exchanges");
                    doRecover(recoverable);
                }
            }
        }

        // start timeout service if its in use
        if (getCompletionTimeout() > 0 || getCompletionTimeoutExpression() != null) {
            ScheduledExecutorService scheduler = camelContext.getExecutorServiceStrategy().newScheduledThreadPool(this, "AggregateTimeoutChecker", 1);
            // check for timed out aggregated messages once every second
            timeoutMap = new AggregationTimeoutMap(scheduler, 1000L);
            ServiceHelper.startService(timeoutMap);
        }
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopService(timeoutMap);
        ServiceHelper.stopService(recoverService);

        ServiceHelper.stopService(aggregationRepository);

        if (closedCorrelationKeys != null) {
            closedCorrelationKeys.clear();
        }
    }

}