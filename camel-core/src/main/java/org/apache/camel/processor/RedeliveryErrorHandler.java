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

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.impl.converter.AsyncProcessorTypeConverter;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.EventHelper;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ServiceHelper;

/**
 * Base redeliverable error handler that also supports a final dead letter queue in case
 * all redelivery attempts fail.
 * <p/>
 * This implementation should contain all the error handling logic and the sub classes
 * should only configure it according to what they support.
 *
 * @version $Revision$
 */
public abstract class RedeliveryErrorHandler extends ErrorHandlerSupport implements AsyncProcessor {

    protected final Processor deadLetter;
    protected final String deadLetterUri;
    protected final Processor output;
    protected final AsyncProcessor outputAsync;
    protected final Processor redeliveryProcessor;
    protected final RedeliveryPolicy redeliveryPolicy;
    protected final Predicate handledPolicy;
    protected final Logger logger;
    protected final boolean useOriginalMessagePolicy;

    protected class RedeliveryData {
        boolean sync = true;
        int redeliveryCounter;
        long redeliveryDelay;
        Predicate retryUntilPredicate;

        // default behavior which can be overloaded on a per exception basis
        RedeliveryPolicy currentRedeliveryPolicy = redeliveryPolicy;
        Processor deadLetterProcessor = deadLetter;
        Processor failureProcessor;
        Processor onRedeliveryProcessor = redeliveryProcessor;
        Predicate handledPredicate = handledPolicy;
        Predicate continuedPredicate;
        boolean useOriginalInMessage = useOriginalMessagePolicy;
    }

    public RedeliveryErrorHandler(Processor output, Logger logger, Processor redeliveryProcessor,
                                  RedeliveryPolicy redeliveryPolicy, Predicate handledPolicy, Processor deadLetter,
                                  String deadLetterUri, boolean useOriginalMessagePolicy) {
        this.redeliveryProcessor = redeliveryProcessor;
        this.deadLetter = deadLetter;
        this.output = output;
        this.outputAsync = AsyncProcessorTypeConverter.convert(output);
        this.redeliveryPolicy = redeliveryPolicy;
        this.logger = logger;
        this.deadLetterUri = deadLetterUri;
        this.handledPolicy = handledPolicy;
        this.useOriginalMessagePolicy = useOriginalMessagePolicy;
    }

    public boolean supportTransacted() {
        return false;
    }

    public void process(Exchange exchange) throws Exception {
        if (output == null) {
            // no output then just return
            return;
        }
        AsyncProcessorHelper.process(this, exchange);
    }

    public boolean process(Exchange exchange, final AsyncCallback callback) {
        return processErrorHandler(exchange, callback, new RedeliveryData());
    }

    /**
     * Processes the exchange decorated with this dead letter channel.
     */
    protected boolean processErrorHandler(final Exchange exchange, final AsyncCallback callback, final RedeliveryData data) {
        while (true) {

            // did previous processing cause an exception?
            boolean handle = shouldHandleException(exchange);
            if (handle) {
                handleException(exchange, data);
            }

            // compute if we should redeliver or not
            boolean shouldRedeliver = shouldRedeliver(exchange, data);
            if (!shouldRedeliver) {
                // no we should not redeliver to the same output so either try an onException (if any given)
                // or the dead letter queue
                Processor target = data.failureProcessor != null ? data.failureProcessor : data.deadLetterProcessor;
                // deliver to the failure processor (either an on exception or dead letter queue
                boolean sync = deliverToFailureProcessor(target, exchange, data, callback);
                // we are breaking out
                return sync;
            }

            if (shouldRedeliver && data.redeliveryCounter > 0) {
                // prepare for redelivery
                prepareExchangeForRedelivery(exchange);

                // if we are redelivering then sleep before trying again
                // wait until we should redeliver
                try {
                    data.redeliveryDelay = data.currentRedeliveryPolicy.sleep(data.redeliveryDelay, data.redeliveryCounter);
                } catch (InterruptedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Sleep interrupted, are we stopping? " + (isStopping() || isStopped()));
                    }
                    // continue from top
                    continue;
                }

                // letting onRedeliver be executed
                deliverToRedeliveryProcessor(exchange, data);
            }

            // process the exchange (also redelivery)
            boolean sync = outputAsync.process(exchange, new AsyncCallback() {
                public void done(boolean sync) {
                    // this callback should only handle the async case
                    if (sync) {
                        return;
                    }

                    // mark we are in async mode now
                    data.sync = false;
                    // only process if the exchange hasn't failed
                    // and it has not been handled by the error processor
                    if (!isDone(exchange)) {
                        // TODO: async process redelivery (eg duplicate the error handler logic)
                        // And have a timer task scheduled when redelivery should occur to avoid blocking thread
                        log.debug("Not done continuing error handling asynchronously: " + exchange);
                    } else {
                        callback.done(sync);
                    }
                }
            });
            if (!sync) {
                // the remainder of the Exchange is being processed asynchronously so we should return
                return false;
            }

            boolean done = isDone(exchange);
            if (done) {
                callback.done(true);
                return true;
            }
            // error occurred so loop back around.....
        }

    }

    /**
     * Strategy whether the exchange has an exception that we should try to handle.
     * <p/>
     * Standard implementations should just look for an exception.
     */
    protected boolean shouldHandleException(Exchange exchange) {
        return exchange.getException() != null;
    }

    /**
     * Strategy to determine if the exchange is done so we can continue
     */
    protected boolean isDone(Exchange exchange) {
        // only done if the exchange hasn't failed
        // and it has not been handled by the failure processor
        // or we are exhausted
        return exchange.getException() == null
            || ExchangeHelper.isFailureHandled(exchange)
            || ExchangeHelper.isRedeliveryExhausted(exchange);
    }

    /**
     * Returns the output processor
     */
    public Processor getOutput() {
        return output;
    }

    /**
     * Returns the dead letter that message exchanges will be sent to if the
     * redelivery attempts fail
     */
    public Processor getDeadLetter() {
        return deadLetter;
    }

    public String getDeadLetterUri() {
        return deadLetterUri;
    }

    public boolean isUseOriginalMessagePolicy() {
        return useOriginalMessagePolicy;
    }

    public RedeliveryPolicy getRedeliveryPolicy() {
        return redeliveryPolicy;
    }

    public Logger getLogger() {
        return logger;
    }

    protected void prepareExchangeForContinue(Exchange exchange, RedeliveryData data) {
        Exception caught = exchange.getException();

        // continue is a kind of redelivery so reuse the logic to prepare
        prepareExchangeForRedelivery(exchange);
        // its continued then remove traces of redelivery attempted and caught exception
        exchange.getIn().removeHeader(Exchange.REDELIVERED);
        exchange.getIn().removeHeader(Exchange.REDELIVERY_COUNTER);
        // keep the Exchange.EXCEPTION_CAUGHT as property so end user knows the caused exception

        // create log message
        String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId();
        msg = msg + ". Exhausted after delivery attempt: " + data.redeliveryCounter + " caught: " + caught;
        msg = msg + ". Handled and continue routing.";

        // log that we failed but want to continue
        logFailedDelivery(false, false, true, exchange, msg, data, null);
    }

    protected void prepareExchangeForRedelivery(Exchange exchange) {
        // okay we will give it another go so clear the exception so we can try again
        if (exchange.getException() != null) {
            exchange.setException(null);
        }

        // clear rollback flags
        exchange.setProperty(Exchange.ROLLBACK_ONLY, null);

        // reset cached streams so they can be read again
        MessageHelper.resetStreamCache(exchange.getIn());
    }

    protected void handleException(Exchange exchange, RedeliveryData data) {
        Exception e = exchange.getException();

        // store the original caused exception in a property, so we can restore it later
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);

        // find the error handler to use (if any)
        OnExceptionDefinition exceptionPolicy = getExceptionPolicy(exchange, e);
        if (exceptionPolicy != null) {
            data.currentRedeliveryPolicy = exceptionPolicy.createRedeliveryPolicy(exchange.getContext(), data.currentRedeliveryPolicy);
            data.handledPredicate = exceptionPolicy.getHandledPolicy();
            data.continuedPredicate = exceptionPolicy.getContinuedPolicy();
            data.retryUntilPredicate = exceptionPolicy.getRetryUntilPolicy();
            data.useOriginalInMessage = exceptionPolicy.getUseOriginalMessagePolicy();

            // route specific failure handler?
            Processor processor = exceptionPolicy.getErrorHandler();
            if (processor != null) {
                data.failureProcessor = processor;
            }
            // route specific on redelivery?
            processor = exceptionPolicy.getOnRedelivery();
            if (processor != null) {
                data.onRedeliveryProcessor = processor;
            }
        }

        String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId()
                + ". On delivery attempt: " + data.redeliveryCounter + " caught: " + e;
        logFailedDelivery(true, false, false, exchange, msg, data, e);

        data.redeliveryCounter = incrementRedeliveryCounter(exchange, e);
    }

    /**
     * Gives an optional configure redelivery processor a chance to process before the Exchange
     * will be redelivered. This can be used to alter the Exchange.
     */
    protected void deliverToRedeliveryProcessor(final Exchange exchange, final RedeliveryData data) {
        if (data.onRedeliveryProcessor == null) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Redelivery processor " + data.onRedeliveryProcessor + " is processing Exchange: " + exchange
                    + " before its redelivered");
        }

        // run this synchronously as its just a Processor
        try {
            data.onRedeliveryProcessor.process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        }
        log.trace("Redelivery processor done");
    }

    /**
     * All redelivery attempts failed so move the exchange to the dead letter queue
     */
    protected boolean deliverToFailureProcessor(final Processor processor, final Exchange exchange,
                                                final RedeliveryData data, final AsyncCallback callback) {
        boolean sync = true;

        Exception caught = exchange.getException();

        // we did not success with the redelivery so now we let the failure processor handle it
        // clear exception as we let the failure processor handle it
        exchange.setException(null);

        boolean handled = false;
        // regard both handled or continued as being handled
        if (shouldHandled(exchange, data) || shouldContinue(exchange, data)) {
            // its handled then remove traces of redelivery attempted
            exchange.getIn().removeHeader(Exchange.REDELIVERED);
            exchange.getIn().removeHeader(Exchange.REDELIVERY_COUNTER);
            handled = true;
        } else {
            // must decrement the redelivery counter as we didn't process the redelivery but is
            // handling by the failure handler. So we must -1 to not let the counter be out-of-sync
            decrementRedeliveryCounter(exchange);
        }

        // is the a failure processor to process the Exchange
        if (processor != null) {

            // reset cached streams so they can be read again
            MessageHelper.resetStreamCache(exchange.getIn());

            // prepare original IN body if it should be moved instead of current body
            if (data.useOriginalInMessage) {
                if (log.isTraceEnabled()) {
                    log.trace("Using the original IN message instead of current");
                }

                Message original = exchange.getUnitOfWork().getOriginalInMessage();
                exchange.setIn(original);
            }

            if (log.isTraceEnabled()) {
                log.trace("Failure processor " + processor + " is processing Exchange: " + exchange);
            }

            // store the last to endpoint as the failure endpoint
            exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));

            // the failure processor could also be asynchronous
            AsyncProcessor afp = AsyncProcessorTypeConverter.convert(processor);
            sync = afp.process(exchange, new AsyncCallback() {
                public void done(boolean sync) {
                    if (log.isTraceEnabled()) {
                        log.trace("Failure processor done: " + processor + " processing Exchange: " + exchange);
                    }
                    try {
                        prepareExchangeAfterFailure(exchange, data);
                        // fire event as we had a failure processor to handle it
                        boolean deadLetterChannel = processor == data.deadLetterProcessor && data.deadLetterProcessor != null;
                        EventHelper.notifyExchangeFailureHandled(exchange.getContext(), exchange, processor, deadLetterChannel);
                    } finally {
                        // if the fault was handled asynchronously, this should be reflected in the callback as well
                        data.sync &= sync;
                        callback.done(data.sync);
                    }
                }
            });
        } else {
            try {
                // no processor but we need to prepare after failure as well
                prepareExchangeAfterFailure(exchange, data);
            } finally {
                // indicate we are done synchronously
                data.sync = true;
                callback.done(data.sync);
            }
        }

        // create log message
        String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId();
        msg = msg + ". Exhausted after delivery attempt: " + data.redeliveryCounter + " caught: " + caught;
        if (processor != null) {
            msg = msg + ". Processed by failure processor: " + processor;
        }

        // log that we failed delivery as we are exhausted
        logFailedDelivery(false, handled, false, exchange, msg, data, null);

        return sync;
    }

    protected void prepareExchangeAfterFailure(final Exchange exchange, final RedeliveryData data) {
        // we could not process the exchange so we let the failure processor handled it
        ExchangeHelper.setFailureHandled(exchange);

        // honor if already set a handling
        boolean alreadySet = exchange.getProperty(Exchange.ERRORHANDLER_HANDLED) != null;
        if (alreadySet) {
            boolean handled = exchange.getProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.class);
            if (log.isDebugEnabled()) {
                log.debug("This exchange has already been marked for handling: " + handled);
            }
            if (handled) {
                exchange.setException(null);
            } else {
                // exception not handled, put exception back in the exchange
                exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                // and put failure endpoint back as well
                exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));
            }
            return;
        }

        if (shouldHandled(exchange, data)) {
            if (log.isDebugEnabled()) {
                log.debug("This exchange is handled so its marked as not failed: " + exchange);
            }
            exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.TRUE);
        } else if (shouldContinue(exchange, data)) {
            if (log.isDebugEnabled()) {
                log.debug("This exchange is continued: " + exchange);
            }
            // okay we want to continue then prepare the exchange for that as well
            prepareExchangeForContinue(exchange, data);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("This exchange is not handled or continued so its marked as failed: " + exchange);
            }
            // exception not handled, put exception back in the exchange
            exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.FALSE);
            exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
            // and put failure endpoint back as well
            exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));
        }
    }

    private void logFailedDelivery(boolean shouldRedeliver, boolean handled, boolean continued, Exchange exchange, String message, RedeliveryData data, Throwable e) {
        if (logger == null) {
            return;
        }

        if (handled && !data.currentRedeliveryPolicy.isLogHandled()) {
            // do not log handled
            return;
        }

        if (continued && !data.currentRedeliveryPolicy.isLogContinued()) {
            // do not log handled
            return;
        }

        if (shouldRedeliver && !data.currentRedeliveryPolicy.isLogRetryAttempted()) {
            // do not log retry attempts
            return;
        }

        if (!shouldRedeliver && !data.currentRedeliveryPolicy.isLogExhausted()) {
            // do not log exhausted
            return;
        }

        LoggingLevel newLogLevel;
        boolean logStrackTrace;
        if (shouldRedeliver) {
            newLogLevel = data.currentRedeliveryPolicy.getRetryAttemptedLogLevel();
            logStrackTrace = data.currentRedeliveryPolicy.isLogRetryStackTrace();
        } else {
            newLogLevel = data.currentRedeliveryPolicy.getRetriesExhaustedLogLevel();
            logStrackTrace = data.currentRedeliveryPolicy.isLogStackTrace();
        }
        if (e == null) {
            e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }

        if (exchange.isRollbackOnly()) {
            String msg = "Rollback exchange";
            if (exchange.getException() != null) {
                msg = msg + " due: " + exchange.getException().getMessage();
            }
            if (newLogLevel == LoggingLevel.ERROR || newLogLevel == LoggingLevel.FATAL) {
                // log intended rollback on maximum WARN level (no ERROR or FATAL)
                logger.log(msg, LoggingLevel.WARN);
            } else {
                // otherwise use the desired logging level
                logger.log(msg, newLogLevel);
            }
        } else if (e != null && logStrackTrace) {
            logger.log(message, e, newLogLevel);
        } else {
            logger.log(message, newLogLevel);
        }
    }

    /**
     * Determines whether or not to we should try to redeliver
     *
     * @param exchange the current exchange
     * @param data     the redelivery data
     * @return <tt>true</tt> to redeliver, or <tt>false</tt> to exhaust.
     */
    private boolean shouldRedeliver(Exchange exchange, RedeliveryData data) {
        // if marked as rollback only then do not redeliver
        boolean rollbackOnly = exchange.getProperty(Exchange.ROLLBACK_ONLY, false, Boolean.class);
        if (rollbackOnly) {
            if (log.isTraceEnabled()) {
                log.trace("This exchange is marked as rollback only, should not be redelivered: " + exchange);
            }
            return false;
        }
        return data.currentRedeliveryPolicy.shouldRedeliver(exchange, data.redeliveryCounter, data.retryUntilPredicate);
    }

    /**
     * Determines whether or not to continue if we are exhausted.
     *
     * @param exchange the current exchange
     * @param data     the redelivery data
     * @return <tt>true</tt> to continue, or <tt>false</tt> to exhaust.
     */
    private boolean shouldContinue(Exchange exchange, RedeliveryData data) {
        if (data.continuedPredicate != null) {
            return data.continuedPredicate.matches(exchange);
        }
        // do not continue by default
        return false;
    }

    /**
     * Determines whether or not to handle if we are exhausted.
     *
     * @param exchange the current exchange
     * @param data     the redelivery data
     * @return <tt>true</tt> to handle, or <tt>false</tt> to exhaust.
     */
    private boolean shouldHandled(Exchange exchange, RedeliveryData data) {
        if (data.handledPredicate != null) {
            return data.handledPredicate.matches(exchange);
        }
        // do not handle by default
        return false;
    }

    /**
     * Increments the redelivery counter and adds the redelivered flag if the
     * message has been redelivered
     */
    private int incrementRedeliveryCounter(Exchange exchange, Throwable e) {
        Message in = exchange.getIn();
        Integer counter = in.getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
        int next = 1;
        if (counter != null) {
            next = counter + 1;
        }
        in.setHeader(Exchange.REDELIVERY_COUNTER, next);
        in.setHeader(Exchange.REDELIVERED, Boolean.TRUE);
        return next;
    }

    /**
     * Prepares the redelivery counter and boolean flag for the failure handle processor
     */
    private void decrementRedeliveryCounter(Exchange exchange) {
        Message in = exchange.getIn();
        Integer counter = in.getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
        if (counter != null) {
            int prev = counter - 1;
            in.setHeader(Exchange.REDELIVERY_COUNTER, prev);
            // set boolean flag according to counter
            in.setHeader(Exchange.REDELIVERED, prev > 0 ? Boolean.TRUE : Boolean.FALSE);
        } else {
            // not redelivered
            in.setHeader(Exchange.REDELIVERY_COUNTER, 0);
            in.setHeader(Exchange.REDELIVERED, Boolean.FALSE);
        }
    }

    @Override
    protected void doStart() throws Exception {
        ServiceHelper.startServices(output, outputAsync, deadLetter);
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopServices(deadLetter, output, outputAsync);
    }

}
