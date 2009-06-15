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

import java.util.concurrent.RejectedExecutionException;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ServiceHelper;

/**
 * Base redeliverable error handler that also suppors a final dead letter queue in case
 * all redelivery attempts fail.
 * <p/>
 * This implementation should contain all the error handling logic and the sub classes
 * should only configure it according to what they support.
 *
 * @version $Revision$
 */
public abstract class RedeliveryErrorHandler extends ErrorHandlerSupport implements Processor {

    // TODO: support onException being able to use other onException to route they exceptions
    // (hard one to get working, has not been supported before)

    protected final Processor deadLetter;
    protected final String deadLetterUri;
    protected final Processor output;
    protected final Processor redeliveryProcessor;
    protected final RedeliveryPolicy redeliveryPolicy;
    protected final Predicate handledPolicy;
    protected final Logger logger;
    protected final boolean useOriginalBodyPolicy;

    protected class RedeliveryData {
        int redeliveryCounter;
        long redeliveryDelay;
        Predicate retryUntilPredicate;

        // default behavior which can be overloaded on a per exception basis
        RedeliveryPolicy currentRedeliveryPolicy = redeliveryPolicy;
        Processor deadLetterProcessor = deadLetter;
        Processor failureProcessor;
        Processor onRedeliveryProcessor = redeliveryProcessor;
        Predicate handledPredicate = handledPolicy;
        boolean useOriginalInBody = useOriginalBodyPolicy;
    }

    public RedeliveryErrorHandler(Processor output, Logger logger, Processor redeliveryProcessor,
                                  RedeliveryPolicy redeliveryPolicy, Predicate handledPolicy, Processor deadLetter,
                                  String deadLetterUri, boolean useOriginalBodyPolicy) {
        this.redeliveryProcessor = redeliveryProcessor;
        this.deadLetter = deadLetter;
        this.output = output;
        this.redeliveryPolicy = redeliveryPolicy;
        this.logger = logger;
        this.deadLetterUri = deadLetterUri;
        this.handledPolicy = handledPolicy;
        this.useOriginalBodyPolicy = useOriginalBodyPolicy;
    }

    public boolean supportTransacted() {
        return false;
    }

    /**
     * Whether this error handler supports dead letter queue or not
     */
    public abstract boolean supportDeadLetterQueue();

    public void process(Exchange exchange) throws Exception {
        processErrorHandler(exchange, new RedeliveryData());
    }

    /**
     * Processes the exchange decorated with this dead letter channel.
     */
    protected void processErrorHandler(final Exchange exchange, final RedeliveryData data) {

        while (true) {
            // we can't keep retrying if the route is being shutdown.
            if (!isRunAllowed()) {
                if (log.isDebugEnabled()) {
                    log.debug("Rejected execution as we are not started for exchange: " + exchange);
                }
                if (exchange.getException() == null) {
                    exchange.setException(new RejectedExecutionException());
                    return;
                }
            }

            // do not handle transacted exchanges that failed as this error handler does not support it
            if (exchange.isTransacted() && !supportTransacted() && exchange.getException() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("This error handler does not support transacted exchanges."
                        + " Bypassing this error handler: " + this + " for exchangeId: " + exchange.getExchangeId());
                }
                return;
            }

            // did previous processing cause an exception?
            if (exchange.getException() != null) {
                handleException(exchange, data);
            }

            // compute if we should redeliver or not
            boolean shouldRedeliver = shouldRedeliver(exchange, data);
            if (!shouldRedeliver) {
                // no we should not redeliver to the same output so either try an onException (if any given)
                // or the dead letter queue
                Processor target = data.failureProcessor != null ? data.failureProcessor : data.deadLetterProcessor;
                // deliver to the failure processor (either an on exception or dead letter queue
                deliverToFailureProcessor(target, exchange, data);
                // prepare the exchange for failure before returning
                prepareExchangeAfterFailure(exchange, data);
                // and then return
                return;
            }

            // if we are redelivering then sleep before trying again
            if (shouldRedeliver && data.redeliveryCounter > 0) {
                prepareExchangeForRedelivery(exchange);

                // wait until we should redeliver
                try {
                    data.redeliveryDelay = data.currentRedeliveryPolicy.sleep(data.redeliveryDelay, data.redeliveryCounter);
                } catch (InterruptedException e) {
                    log.debug("Sleep interrupted, are we stopping? " + (isStopping() || isStopped()));
                    // continue from top
                    continue;
                }

                // letting onRedeliver be executed
                deliverToRedeliveryProcessor(exchange, data);
            }

            // process the exchange (also redelivery)
            try {
                output.process(exchange);
            } catch (Exception e) {
                exchange.setException(e);
            }

            // only done if the exchange hasn't failed
            // and it has not been handled by the failure processor
            boolean done = exchange.getException() == null || ExchangeHelper.isFailureHandled(exchange);
            if (done) {
                return;
            }
            // error occurred so loop back around.....
        }

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

    public RedeliveryPolicy getRedeliveryPolicy() {
        return redeliveryPolicy;
    }

    public Logger getLogger() {
        return logger;
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
        Throwable e = exchange.getException();

        // store the original caused exception in a property, so we can restore it later
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);

        // find the error handler to use (if any)
        OnExceptionDefinition exceptionPolicy = getExceptionPolicy(exchange, e);
        if (exceptionPolicy != null) {
            data.currentRedeliveryPolicy = exceptionPolicy.createRedeliveryPolicy(exchange.getContext(), data.currentRedeliveryPolicy);
            data.handledPredicate = exceptionPolicy.getHandledPolicy();
            data.retryUntilPredicate = exceptionPolicy.getRetryUntilPolicy();
            data.useOriginalInBody = exceptionPolicy.getUseOriginalBodyPolicy();

            // route specific failure handler?
            Processor processor = exceptionPolicy.getErrorHandler();
            if (processor != null) {
                data.failureProcessor = processor;
            }
            // route specific on redelivey?
            processor = exceptionPolicy.getOnRedelivery();
            if (processor != null) {
                data.onRedeliveryProcessor = processor;
            }
        }

        String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId()
                + ". On delivery attempt: " + data.redeliveryCounter + " caught: " + e;
        logFailedDelivery(true, exchange, msg, data, e);

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
    protected void deliverToFailureProcessor(final Processor processor, final Exchange exchange,
                                             final RedeliveryData data) {
        // we did not success with the redelivery so now we let the failure processor handle it
        ExchangeHelper.setFailureHandled(exchange);
        // must decrement the redelivery counter as we didn't process the redelivery but is
        // handling by the failure handler. So we must -1 to not let the counter be out-of-sync
        decrementRedeliveryCounter(exchange);
        // reset cached streams so they can be read again
        MessageHelper.resetStreamCache(exchange.getIn());

        if (processor != null) {
            // prepare original IN body if it should be moved instead of current body
            if (data.useOriginalInBody) {
                if (log.isTraceEnabled()) {
                    log.trace("Using the original IN body instead of the current IN body");
                }

                Object original = exchange.getUnitOfWork().getOriginalInBody();
                exchange.getIn().setBody(original);
            }

            if (log.isTraceEnabled()) {
                log.trace("Failure processor " + processor + " is processing Exchange: " + exchange);
            }
            try {
                processor.process(exchange);
            } catch (Exception e) {
                exchange.setException(e);
            }
            log.trace("Failure processor done");

            String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId()
                    + ". Processed by failure processor: " + processor;
            logFailedDelivery(false, exchange, msg, data, null);
        }
    }

    protected void prepareExchangeAfterFailure(Exchange exchange, final RedeliveryData data) {
        Predicate handledPredicate = data.handledPredicate;

        if (handledPredicate == null || !handledPredicate.matches(exchange)) {
            if (log.isDebugEnabled()) {
                log.debug("This exchange is not handled so its marked as failed: " + exchange);
            }
            // exception not handled, put exception back in the exchange
            exchange.setProperty(Exchange.EXCEPTION_HANDLED, Boolean.FALSE);
            exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("This exchange is handled so its marked as not failed: " + exchange);
            }
            exchange.setProperty(Exchange.EXCEPTION_HANDLED, Boolean.TRUE);
        }
    }

    private void logFailedDelivery(boolean shouldRedeliver, Exchange exchange, String message, RedeliveryData data, Throwable e) {
        if (logger == null) {
            return;
        }

        LoggingLevel newLogLevel;
        if (shouldRedeliver) {
            newLogLevel = data.currentRedeliveryPolicy.getRetryAttemptedLogLevel();
        } else {
            newLogLevel = data.currentRedeliveryPolicy.getRetriesExhaustedLogLevel();
        }
        if (exchange.isRollbackOnly()) {
            String msg = "Rollback exchange";
            if (exchange.getException() != null) {
                msg = msg + " due: " + exchange.getException().getMessage();
            }
            if (newLogLevel == LoggingLevel.ERROR || newLogLevel == LoggingLevel.FATAL) {
                // log intented rollback on maximum WARN level (no ERROR or FATAL)
                logger.log(msg, LoggingLevel.WARN);
            } else {
                // otherwise use the desired logging level
                logger.log(msg, newLogLevel);
            }
        } else if (data.currentRedeliveryPolicy.isLogStackTrace() && e != null) {
            logger.log(message, e, newLogLevel);
        } else {
            logger.log(message, newLogLevel);
        }
    }

    private boolean shouldRedeliver(Exchange exchange, RedeliveryData data) {
        return data.currentRedeliveryPolicy.shouldRedeliver(exchange, data.redeliveryCounter, data.retryUntilPredicate);
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
        ServiceHelper.startServices(output, deadLetter);
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopServices(deadLetter, output);
    }

}
