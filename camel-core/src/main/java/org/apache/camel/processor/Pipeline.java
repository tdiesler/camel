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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.converter.AsyncProcessorTypeConverter;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a Pipeline pattern where the output of the previous step is sent as
 * input to the next step, reusing the same message exchanges
 *
 * @version $Revision$
 */
public class Pipeline extends MulticastProcessor implements AsyncProcessor, Traceable {
    private static final transient Log LOG = LogFactory.getLog(Pipeline.class);

    public Pipeline(CamelContext camelContext, Collection<Processor> processors) {
        super(camelContext, processors);
    }

    public static Processor newInstance(CamelContext camelContext, List<Processor> processors) {
        if (processors.isEmpty()) {
            return null;
        } else if (processors.size() == 1) {
            return processors.get(0);
        }
        return new Pipeline(camelContext, processors);
    }

    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        Iterator<Processor> processors = getProcessors().iterator();
        Exchange nextExchange = exchange;
        boolean first = true;

        while (continueRouting(processors, nextExchange)) {
            if (first) {
                first = false;
            } else {
                // prepare for next run
                nextExchange = createNextExchange(nextExchange);
            }

            // get the next processor
            Processor processor = processors.next();

            AsyncProcessor async = AsyncProcessorTypeConverter.convert(processor);
            boolean sync = process(exchange, nextExchange, callback, processors, async);

            // continue as long its being processed synchronously
            if (!sync) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Processing exchangeId: " + exchange.getExchangeId() + " is continued being processed asynchronously");
                }
                // the remainder of the pipeline will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Processing exchangeId: " + exchange.getExchangeId() + " is continued being processed synchronously");
            }

            // check for error if so we should break out
            boolean exceptionHandled = hasExceptionBeenHandledByErrorHandler(nextExchange);
            if (nextExchange.isFailed() || nextExchange.isRollbackOnly() || exceptionHandled) {
                // The Exchange.ERRORHANDLED_HANDLED property is only set if satisfactory handling was done
                // by the error handler. It's still an exception, the exchange still failed.
                if (LOG.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Message exchange has failed so breaking out of pipeline: ").append(nextExchange);
                    if (nextExchange.isRollbackOnly()) {
                        sb.append(" Marked as rollback only.");
                    }
                    if (nextExchange.getException() != null) {
                        sb.append(" Exception: ").append(nextExchange.getException());
                    }
                    if (nextExchange.hasOut() && nextExchange.getOut().isFault()) {
                        sb.append(" Fault: ").append(nextExchange.getOut());
                    }
                    if (exceptionHandled) {
                        sb.append(" Handled by the error handler.");
                    }
                    LOG.debug(sb.toString());
                }
                break;
            }
        }

        if (LOG.isTraceEnabled()) {
            // logging nextExchange as it contains the exchange that might have altered the payload and since
            // we are logging the completion if will be confusing if we log the original instead
            // we could also consider logging the original and the nextExchange then we have *before* and *after* snapshots
            LOG.trace("Processing complete for exchangeId: " + exchange.getExchangeId() + " >>> " + nextExchange);
        }

        // copy results back to the original exchange
        ExchangeHelper.copyResults(exchange, nextExchange);

        callback.done(true);
        return true;

    }

    private boolean process(final Exchange original, final Exchange exchange, final AsyncCallback callback,
                            final Iterator<Processor> processors, final AsyncProcessor asyncProcessor) {
        if (LOG.isTraceEnabled()) {
            // this does the actual processing so log at trace level
            LOG.trace("Processing exchangeId: " + exchange.getExchangeId() + " >>> " + exchange);
        }

        // implement asynchronous routing logic in callback so we can have the callback being
        // triggered and then continue routing where we left
        boolean sync = asyncProcessor.process(exchange, new AsyncCallback() {
            public void done(boolean sync) {
                // we only have to handle async completion of the pipeline
                if (sync) {
                    return;
                }

                // continue processing the pipeline asynchronously
                Exchange nextExchange = exchange;
                while (processors.hasNext()) {
                    AsyncProcessor processor = AsyncProcessorTypeConverter.convert(processors.next());

                    // check for error if so we should break out
                    boolean exceptionHandled = hasExceptionBeenHandledByErrorHandler(nextExchange);
                    if (nextExchange.isFailed() || nextExchange.isRollbackOnly() || exceptionHandled) {
                        // The Exchange.ERRORHANDLED_HANDLED property is only set if satisfactory handling was done
                        // by the error handler. It's still an exception, the exchange still failed.
                        if (LOG.isDebugEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Message exchange has failed so breaking out of pipeline: ").append(nextExchange);
                            if (nextExchange.isRollbackOnly()) {
                                sb.append(" Marked as rollback only.");
                            }
                            if (nextExchange.getException() != null) {
                                sb.append(" Exception: ").append(nextExchange.getException());
                            }
                            if (nextExchange.hasOut() && nextExchange.getOut().isFault()) {
                                sb.append(" Fault: ").append(nextExchange.getOut());
                            }
                            if (exceptionHandled) {
                                sb.append(" Handled by the error handler.");
                            }
                            LOG.debug(sb.toString());
                        }
                        break;
                    }

                    nextExchange = createNextExchange(nextExchange);
                    sync = process(original, nextExchange, callback, processors, processor);
                    if (!sync) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Processing exchangeId: " + exchange.getExchangeId() + " is continued being processed asynchronously");
                        }
                        return;
                    }
                }

                ExchangeHelper.copyResults(original, nextExchange);
                callback.done(false);
            }
        });


        return sync;
    }

    private static boolean hasExceptionBeenHandledByErrorHandler(Exchange nextExchange) {
        return Boolean.TRUE.equals(nextExchange.getProperty(Exchange.ERRORHANDLER_HANDLED));
    }

    /**
     * Strategy method to create the next exchange from the previous exchange.
     * <p/>
     * Remember to copy the original exchange id otherwise correlation of ids in the log is a problem
     *
     * @param previousExchange the previous exchange
     * @return a new exchange
     */
    protected Exchange createNextExchange(Exchange previousExchange) {
        Exchange answer = new DefaultExchange(previousExchange);
        // we must use the same id as this is a snapshot strategy where Camel copies a snapshot
        // before processing the next step in the pipeline, so we have a snapshot of the exchange
        // just before. This snapshot is used if Camel should do redeliveries (re try) using
        // DeadLetterChannel. That is why it's important the id is the same, as it is the *same*
        // exchange being routed.
        answer.setExchangeId(previousExchange.getExchangeId());

        answer.getProperties().putAll(previousExchange.getProperties());

        // now lets set the input of the next exchange to the output of the
        // previous message if it is not null
        answer.setIn(previousExchange.hasOut() 
            ? previousExchange.getOut().copy() : previousExchange.getIn().copy());
        return answer;
    }

    protected boolean continueRouting(Iterator<Processor> it, Exchange exchange) {
        Object stop = exchange.getProperty(Exchange.ROUTE_STOP);
        if (stop != null) {
            boolean doStop = exchange.getContext().getTypeConverter().convertTo(Boolean.class, stop);
            if (doStop) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Exchange is marked to stop routing: " + exchange);
                }
                return false;
            }
        }

        // continue if there are more processors to route
        return it.hasNext();
    }

    @Override
    public String toString() {
        return "Pipeline[" + getProcessors() + "]";
    }

    @Override
    public String getTraceLabel() {
        return "pipeline";
    }
}
