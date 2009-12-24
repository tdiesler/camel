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
package org.apache.camel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.NoSuchPropertyException;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.UnitOfWork;

/**
 * Some helper methods for working with {@link Exchange} objects
 *
 * @version $Revision$
 */
public final class ExchangeHelper {

    /**
     * Utility classes should not have a public constructor.
     */
    private ExchangeHelper() {
    }

    /**
     * Extracts the Exchange.BINDING of the given type or null if not present
     *
     * @param exchange the message exchange
     * @param type the expected binding type
     * @return the binding object of the given type or null if it could not be found or converted
     */
    public static <T> T getBinding(Exchange exchange, Class<T> type) {
        return exchange != null ? exchange.getProperty(Exchange.BINDING, type) : null;
    }

    /**
     * Attempts to resolve the endpoint for the given value
     *
     * @param exchange the message exchange being processed
     * @param value the value which can be an {@link Endpoint} or an object
     *                which provides a String representation of an endpoint via
     *                {@link #toString()}
     *
     * @return the endpoint
     * @throws NoSuchEndpointException if the endpoint cannot be resolved
     */
    public static Endpoint resolveEndpoint(Exchange exchange, Object value) throws NoSuchEndpointException {
        Endpoint endpoint;
        if (value instanceof Endpoint) {
            endpoint = (Endpoint)value;
        } else {
            String uri = value.toString();
            endpoint = CamelContextHelper.getMandatoryEndpoint(exchange.getContext(), uri);
        }
        return endpoint;
    }

    public static <T> T getMandatoryProperty(Exchange exchange, String propertyName, Class<T> type) throws NoSuchPropertyException {
        T result = exchange.getProperty(propertyName, type);
        if (result != null) {
            return result;
        }
        throw new NoSuchPropertyException(exchange, propertyName, type);
    }

    public static <T> T getMandatoryHeader(Exchange exchange, String propertyName, Class<T> type) throws NoSuchHeaderException {
        T answer = exchange.getIn().getHeader(propertyName, type);
        if (answer == null) {
            throw new NoSuchHeaderException(exchange, propertyName, type);
        }
        return answer;
    }

    /**
     * Returns the mandatory inbound message body of the correct type or throws
     * an exception if it is not present
     */
    public static Object getMandatoryInBody(Exchange exchange) throws InvalidPayloadException {
        return exchange.getIn().getMandatoryBody();
    }

    /**
     * Returns the mandatory inbound message body of the correct type or throws
     * an exception if it is not present
     */
    public static <T> T getMandatoryInBody(Exchange exchange, Class<T> type) throws InvalidPayloadException {
        return exchange.getIn().getMandatoryBody(type);
    }

    /**
     * Returns the mandatory outbound message body of the correct type or throws
     * an exception if it is not present
     */
    public static Object getMandatoryOutBody(Exchange exchange) throws InvalidPayloadException {
        return exchange.getOut().getMandatoryBody();
    }

    /**
     * Returns the mandatory outbound message body of the correct type or throws
     * an exception if it is not present
     */
    public static <T> T getMandatoryOutBody(Exchange exchange, Class<T> type) throws InvalidPayloadException {
        return exchange.getOut().getMandatoryBody(type);
    }

    /**
     * Converts the value to the given expected type or throws an exception
     */
    public static <T> T convertToMandatoryType(Exchange exchange, Class<T> type, Object value) throws NoTypeConversionAvailableException {
        CamelContext camelContext = exchange.getContext();
        TypeConverter converter = camelContext.getTypeConverter();
        if (converter != null) {
            return converter.mandatoryConvertTo(type, exchange, value);
        }
        throw new NoTypeConversionAvailableException(value, type);
    }

    /**
     * Converts the value to the given expected type returning null if it could
     * not be converted
     */
    public static <T> T convertToType(Exchange exchange, Class<T> type, Object value) {
        CamelContext camelContext = exchange.getContext();
        TypeConverter converter = camelContext.getTypeConverter();
        if (converter != null) {
            return converter.convertTo(type, exchange, value);
        }
        return null;
    }

    /**
     * Creates a new instance and copies from the current message exchange so that it can be
     * forwarded to another destination as a new instance. Unlike regular copy this operation
     * will not share the same {@link org.apache.camel.spi.UnitOfWork} so its should be used
     * for async messaging, where the original and copied exchange are independent.
     *
     * @param exchange original copy of the exchange
     * @param handover whether the on completion callbacks should be handed over to the new copy.
     */
    public static Exchange createCorrelatedCopy(Exchange exchange, boolean handover) {
        Exchange copy = exchange.copy();
        // do not share the unit of work
        copy.setUnitOfWork(null);
        // hand over on completion to the copy if we got any
        UnitOfWork uow = exchange.getUnitOfWork();
        if (handover && uow != null) {
            uow.handoverSynchronization(copy);
        }
        // set a correlation id so we can track back the original exchange
        copy.setProperty(Exchange.CORRELATION_ID, exchange.getExchangeId());
        return copy;
    }

    /**
     * Copies the results of a message exchange from the source exchange to the result exchange
     * which will copy the out and fault message contents and the exception
     *
     * @param result the result exchange which will have the output and error state added
     * @param source the source exchange which is not modified
     */
    public static void copyResults(Exchange result, Exchange source) {

        // --------------------------------------------------------------------
        //  TODO: merge logic with that of copyResultsPreservePattern()
        // --------------------------------------------------------------------
        
        if (result != source) {
            result.setException(source.getException());
            if (source.hasOut()) {
                result.getOut().copyFrom(source.getOut());
            } else if (result.getPattern() == ExchangePattern.InOptionalOut) {
                // special case where the result is InOptionalOut and with no OUT response
                // so we should return null to indicate this fact
                result.setOut(null);
            } else {
                // no results so lets copy the last input
                // as the final processor on a pipeline might not
                // have created any OUT; such as a mock:endpoint
                // so lets assume the last IN is the OUT
                if (result.getPattern().isOutCapable()) {
                    // only set OUT if its OUT capable
                    result.getOut().copyFrom(source.getIn());
                } else {
                    // if not replace IN instead to keep the MEP
                    result.getIn().copyFrom(source.getIn());
                }
            }

            if (source.hasProperties()) {
                result.getProperties().putAll(source.getProperties());
            }
        }
    }

    /**
     * Copies the <code>source</code> exchange to <code>target</code> exchange
     * preserving the {@link ExchangePattern} of <code>target</code>.  
     * 
     * @param source source exchange.
     * @param result target exchange.
     */
    public static void copyResultsPreservePattern(Exchange result, Exchange source) {

        // --------------------------------------------------------------------
        //  TODO: merge logic with that of copyResults()
        // --------------------------------------------------------------------
        
        if (source == result) {
            // no need to copy
            return;
        }

        // copy in message
        result.getIn().copyFrom(source.getIn());
    
        // copy out message
        if (source.hasOut()) {
            // exchange pattern sensitive
            Message resultMessage = source.getOut().isFault() ? result.getOut() : getResultMessage(result);
            resultMessage.copyFrom(source.getOut());
        }

        // copy exception
        result.setException(source.getException());
        
        // copy properties
        if (source.hasProperties()) {
            result.getProperties().putAll(source.getProperties());
        }
    }

    /**
     * Returns the message where to write results in an
     * exchange-pattern-sensitive way.
     * 
     * @param exchange message exchange.
     * @return result message.
     */
    public static Message getResultMessage(Exchange exchange) {
        if (exchange.getPattern().isOutCapable()) {
            return exchange.getOut();
        } else {
            return exchange.getIn();
        }
    }

    /**
     * Returns true if the given exchange pattern (if defined) can support OUT messages
     *
     * @param exchange the exchange to interrogate
     * @return true if the exchange is defined as an {@link ExchangePattern} which supports
     * OUT messages
     */
    public static boolean isOutCapable(Exchange exchange) {
        ExchangePattern pattern = exchange.getPattern();
        return pattern != null && pattern.isOutCapable();
    }

    /**
     * Creates a new instance of the given type from the injector
     */
    public static <T> T newInstance(Exchange exchange, Class<T> type) {
        return exchange.getContext().getInjector().newInstance(type);
    }

    /**
     * Creates a Map of the variables which are made available to a script or template
     *
     * @param exchange the exchange to make available
     * @return a Map populated with the require variables
     */
    public static Map<String, Object> createVariableMap(Exchange exchange) {
        Map<String, Object> answer = new HashMap<String, Object>();
        populateVariableMap(exchange, answer);
        return answer;
    }

    /**
     * Populates the Map with the variables which are made available to a script or template
     *
     * @param exchange the exchange to make available
     * @param map      the map to populate
     */
    public static void populateVariableMap(Exchange exchange, Map<String, Object> map) {
        map.put("exchange", exchange);
        Message in = exchange.getIn();
        map.put("in", in);
        map.put("request", in);
        map.put("headers", in.getHeaders());
        map.put("body", in.getBody());
        if (isOutCapable(exchange)) {
            Message out = exchange.getOut();
            map.put("out", out);
            map.put("response", out);
        }
        map.put("camelContext", exchange.getContext());
    }

    /**
     * Returns the MIME content type on the input message or null if one is not defined
     */
    public static String getContentType(Exchange exchange) {
        return MessageHelper.getContentType(exchange.getIn());        
    }

    /**
     * Returns the MIME content encoding on the input message or null if one is not defined
     */
    public static String getContentEncoding(Exchange exchange) {
        return MessageHelper.getContentEncoding(exchange.getIn());
    }

    /**
     * Performs a lookup in the registry of the mandatory bean name and throws an exception if it could not be found
     */
    public static Object lookupMandatoryBean(Exchange exchange, String name) {
        Object value = lookupBean(exchange, name);
        if (value == null) {
            throw new NoSuchBeanException(name);
        }
        return value;
    }

    /**
     * Performs a lookup in the registry of the mandatory bean name and throws an exception if it could not be found
     */
    public static <T> T lookupMandatoryBean(Exchange exchange, String name, Class<T> type) {
        T value = lookupBean(exchange, name, type);
        if (value == null) {
            throw new NoSuchBeanException(name);
        }
        return value;
    }

    /**
     * Performs a lookup in the registry of the bean name
     */
    public static Object lookupBean(Exchange exchange, String name) {
        return exchange.getContext().getRegistry().lookup(name);
    }

    /**
     * Performs a lookup in the registry of the bean name and type
     */
    public static <T> T lookupBean(Exchange exchange, String name, Class<T> type) {
        return exchange.getContext().getRegistry().lookup(name, type);
    }

    /**
     * Returns the first exchange in the given collection of exchanges which has the same exchange ID as the one given
     * or null if none could be found
     */
    public static Exchange getExchangeById(Iterable<Exchange> exchanges, String exchangeId) {
        for (Exchange exchange : exchanges) {
            String id = exchange.getExchangeId();
            if (id != null && id.equals(exchangeId)) {
                return exchange;
            }
        }
        return null;
    }

    /**
     * Prepares the exchanges for aggregation.
     * <p/>
     * This implementation will copy the OUT body to the IN body so when you do
     * aggregation the body is <b>only</b> in the IN body to avoid confusing end users.
     *
     * @param oldExchange  the old exchange
     * @param newExchange  the new exchange
     */
    public static void prepareAggregation(Exchange oldExchange, Exchange newExchange) {
        // move body/header from OUT to IN
        if (oldExchange != null) {
            if (oldExchange.hasOut()) {
                oldExchange.setIn(oldExchange.getOut());
                oldExchange.setOut(null);
            }
        }

        if (newExchange != null) {
            if (newExchange.hasOut()) {
                newExchange.setIn(newExchange.getOut());
                newExchange.setOut(null);
            }
        }
    }

    public static boolean isFailureHandled(Exchange exchange) {
        Boolean handled = exchange.getProperty(Exchange.FAILURE_HANDLED, Boolean.class);
        return handled != null && handled;
    }

    public static void setFailureHandled(Exchange exchange) {
        exchange.setProperty(Exchange.FAILURE_HANDLED, Boolean.TRUE);
        // clear exception since its failure handled
        exchange.setException(null);
    }

    public static boolean isRedelieryExhausted(Exchange exchange) {
        Boolean exhausted = exchange.getProperty(Exchange.REDELIVERY_EXHAUSTED, Boolean.class);
        return exhausted != null && exhausted;
    }

    /**
     * Extracts the body from the given exchange.
     * <p/>
     * If the exchange pattern is provided it will try to honor it and retrive the body
     * from either IN or OUT according to the pattern.
     *
     * @param exchange   the exchange
     * @param pattern    exchange pattern if given, can be <tt>null</tt>
     * @return the result body, can be <tt>null</tt>.
     * @throws CamelExecutionException if the processing of the exchange failed
     */
    public static Object extractResultBody(Exchange exchange, ExchangePattern pattern) {
        Object answer = null;
        if (exchange != null) {
            // rethrow if there was an exception during execution
            if (exchange.getException() != null) {
                throw ObjectHelper.wrapCamelExecutionException(exchange, exchange.getException());
            }

            // result could have a fault message
            if (hasFaultMessage(exchange)) {
                return exchange.getOut().getBody();
            }

            // okay no fault then return the response according to the pattern
            // try to honor pattern if provided
            boolean notOut = pattern != null && !pattern.isOutCapable();
            boolean hasOut = exchange.hasOut();
            if (hasOut && !notOut) {
                // we have a response in out and the pattern is out capable
                answer = exchange.getOut().getBody();
            } else if (!hasOut && exchange.getPattern() == ExchangePattern.InOptionalOut) {
                // special case where the result is InOptionalOut and with no OUT response
                // so we should return null to indicate this fact
                answer = null;
            } else {
                // use IN as the response
                answer = exchange.getIn().getBody();
            }
        }
        return answer;
    }

    /**
     * Tests whether the exchange has a fault message set and that its not null.
     *
     * @param exchange  the exchange
     * @return <tt>true</tt> if fault message exists
     */
    public static boolean hasFaultMessage(Exchange exchange) {
        return exchange.hasOut() && exchange.getOut().isFault() && exchange.getOut().getBody() != null;
    }

    /**
     * Extracts the body from the given future, that represents a handle to an asynchronous exchange.
     * <p/>
     * Will wait until the future task is complete.
     *
     * @param context the camel context
     * @param future the future handle
     * @param type the expected body response type
     * @return the result body, can be <tt>null</tt>.
     * @throws CamelExecutionException if the processing of the exchange failed
     */
    public static <T> T extractFutureBody(CamelContext context, Future<Object> future, Class<T> type) {
        try {
            return doExtractFutureBody(context, future.get(), type);
        } catch (InterruptedException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        } catch (ExecutionException e) {
            // execution failed due to an exception so rethrow the cause
            throw ObjectHelper.wrapCamelExecutionException(null, e.getCause());
        } finally {
            // its harmless to cancel if task is already completed
            // and in any case we do not want to get hold of the task a 2nd time
            // and its recommended to cancel according to Brian Goetz in his Java Concurrency in Practice book
            future.cancel(true);
        }
    }

    /**
     * Extracts the body from the given future, that represents a handle to an asynchronous exchange.
     * <p/>
     * Will wait for the future task to complete, but waiting at most the timeout value.
     *
     * @param context the camel context
     * @param future the future handle
     * @param timeout timeout value
     * @param unit    timeout unit
     * @param type the expected body response type
     * @return the result body, can be <tt>null</tt>.
     * @throws CamelExecutionException if the processing of the exchange failed
     * @throws java.util.concurrent.TimeoutException is thrown if a timeout triggered
     */
    public static <T> T extractFutureBody(CamelContext context, Future<Object> future, long timeout, TimeUnit unit, Class<T> type) throws TimeoutException {
        try {
            if (timeout > 0) {
                return doExtractFutureBody(context, future.get(timeout, unit), type);
            } else {
                return doExtractFutureBody(context, future.get(), type);
            }
        } catch (InterruptedException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        } catch (ExecutionException e) {
            // execution failed due to an exception so rethrow the cause
            throw ObjectHelper.wrapCamelExecutionException(null, e.getCause());
        } finally {
            // its harmless to cancel if task is already completed
            // and in any case we do not want to get hold of the task a 2nd time
            // and its recommended to cancel according to Brian Goetz in his Java Concurrency in Practice book
            future.cancel(true);
        }
    }

    private static <T> T doExtractFutureBody(CamelContext context, Object result, Class<T> type) {
        if (result == null) {
            return null;
        }
        if (type.isAssignableFrom(result.getClass())) {
            return type.cast(result);
        }
        if (result instanceof Exchange) {
            Exchange exchange = (Exchange) result;
            Object answer = ExchangeHelper.extractResultBody(exchange, exchange.getPattern());
            return context.getTypeConverter().convertTo(type, answer);
        }
        return context.getTypeConverter().convertTo(type, result);
    }

}
