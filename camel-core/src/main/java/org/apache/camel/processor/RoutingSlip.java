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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.FailedToCreateProducerException;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ProducerCallback;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ProducerCache;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.model.RoutingSlipDefinition;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * Implements a <a href="http://camel.apache.org/routing-slip.html">Routing Slip</a>
 * pattern where the list of actual endpoints to send a message exchange to are
 * dependent on the value of a message header.
 */
public class RoutingSlip extends ServiceSupport implements Processor, Traceable {
    private static final transient Log LOG = LogFactory.getLog(RoutingSlip.class);
    private ProducerCache producerCache;
    private boolean ignoreInvalidEndpoints;
    private final String header;
    private final String uriDelimiter;
    private final CamelContext camelContext;

    public RoutingSlip(CamelContext camelContext, String header) {
        this(camelContext, header, RoutingSlipDefinition.DEFAULT_DELIMITER);
    }

    public RoutingSlip(CamelContext camelContext, String header, String uriDelimiter) {
        notNull(camelContext, "camelContext");
        notNull(header, "header");
        notNull(uriDelimiter, "uriDelimiter");

        this.camelContext = camelContext;
        this.header = header;
        this.uriDelimiter = uriDelimiter;
    }
    
    public boolean isIgnoreInvalidEndpoints() {
        return ignoreInvalidEndpoints;
    }
    
    public void setIgnoreInvalidEndpoints(boolean ignoreInvalidEndpoints) {
        this.ignoreInvalidEndpoints = ignoreInvalidEndpoints;
    }

    @Override
    public String toString() {
        return "RoutingSlip[header=" + header + " uriDelimiter=" + uriDelimiter + "]";
    }

    public String getTraceLabel() {
        return "routingSlip[" + header + "]";
    }

    public void process(Exchange exchange) throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("RoutingSlip has not been started: " + this);
        }

        Message message = exchange.getIn();
        String[] recipients = recipients(message);
        Exchange current = exchange;

        for (String nextRecipient : recipients) {
            Endpoint endpoint = null;
            try {
                endpoint = resolveEndpoint(exchange, nextRecipient.trim());
            } catch (Exception ex) {
                if (isIgnoreInvalidEndpoints()) {
                    LOG.info("Cannot resolve the endpoint with " + nextRecipient, ex);
                    continue;
                } else {
                    throw ex;
                }
            }

            Exchange copy = new DefaultExchange(current);
            updateRoutingSlip(current);
            copyOutToIn(copy, current);

            try {                
                producerCache.doInProducer(endpoint, copy, null, new ProducerCallback<Object>() {
                    public Object doInProducer(Producer producer, Exchange exchange, ExchangePattern exchangePattern) throws Exception {
                        // set property which endpoint we send to
                        exchange.setProperty(Exchange.TO_ENDPOINT, producer.getEndpoint().getEndpointUri());
                        producer.process(exchange);
                        return exchange;
                    }
                });  
            } catch (Exception e) {
                // Need to check the if the exception is thrown when camel try to create and start the producer 
                if (e instanceof FailedToCreateProducerException && isIgnoreInvalidEndpoints()) {
                    LOG.info("An Invalid endpoint with " + nextRecipient, e);
                    continue;
                } else {
                    // catch exception so we can decide if we want to continue or not
                    copy.setException(e);
                }
            } finally {
                current = copy;
            }
            
            // Decide whether to continue with the recipients or not; similar logic to the Pipeline
            boolean exceptionHandled = hasExceptionBeenHandledByErrorHandler(current);
            if (current.isFailed() || current.isRollbackOnly() || exceptionHandled) {
                // The Exchange.ERRORHANDLED_HANDLED property is only set if satisfactory handling was done
                // by the error handler. It's still an exception, the exchange still failed.
                if (LOG.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Message exchange has failed so breaking out of the routing slip: ").append(current);
                    if (current.isRollbackOnly()) {
                        sb.append(" Marked as rollback only.");
                    }
                    if (current.getException() != null) {
                        sb.append(" Exception: ").append(current.getException());
                    }
                    if (current.hasOut() && current.getOut().isFault()) {
                        sb.append(" Fault: ").append(current.getOut());
                    }
                    if (exceptionHandled) {
                        sb.append(" Handled by the error handler.");
                    }
                    LOG.debug(sb.toString());
                }
                break;
            }
        }
        ExchangeHelper.copyResults(exchange, current);
    }

    private static boolean hasExceptionBeenHandledByErrorHandler(Exchange nextExchange) {
        return Boolean.TRUE.equals(nextExchange.getProperty(Exchange.ERRORHANDLER_HANDLED));
    }
    
    protected Endpoint resolveEndpoint(Exchange exchange, Object recipient) {
        return ExchangeHelper.resolveEndpoint(exchange, recipient);
    }

    protected void doStart() throws Exception {
        if (producerCache == null) {
            producerCache = new ProducerCache(this, camelContext);
            // add it as a service so we can manage it
            camelContext.addService(producerCache);
        }
        ServiceHelper.startService(producerCache);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopService(producerCache);
    }

    private void updateRoutingSlip(Exchange current) {
        Message message = getResultMessage(current);
        String oldSlip = message.getHeader(header, String.class);
        if (oldSlip != null) {
            int delimiterIndex = oldSlip.indexOf(uriDelimiter);
            String newSlip = delimiterIndex > 0 ? oldSlip.substring(delimiterIndex + 1) : "";
            message.setHeader(header, newSlip);
        }
    }

    /**
     * Returns the outbound message if available. Otherwise return the inbound
     * message.
     */
    private Message getResultMessage(Exchange exchange) {
        if (exchange.hasOut()) {
            return exchange.getOut();
        } else {
            // if this endpoint had no out (like a mock endpoint) just take the in
            return exchange.getIn();
        }
    }

    /**
     * Return the list of recipients defined in the routing slip in the
     * specified message.
     */
    private String[] recipients(Message message) {
        Object headerValue = message.getHeader(header);
        if (ObjectHelper.isNotEmpty(headerValue)) {
            return headerValue.toString().split(uriDelimiter);
        }
        return new String[]{};
    }

    /**
     * Copy the outbound data in 'source' to the inbound data in 'result'.
     */
    private void copyOutToIn(Exchange result, Exchange source) {
        result.setException(source.getException());

        if (source.hasOut() && source.getOut().isFault()) {
            result.getOut().copyFrom(source.getOut());
        }

        result.setIn(getResultMessage(source));

        result.getProperties().clear();
        result.getProperties().putAll(source.getProperties());
    }
}
