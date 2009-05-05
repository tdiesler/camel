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
package org.apache.camel;

import java.util.Map;

/**
 * Template (named like Spring's TransactionTemplate & JmsTemplate
 * et al) for working with Camel and sending {@link Message} instances in an
 * {@link Exchange} to an {@link Endpoint}.
 * <p/>
 * <b>All</b> methods throws {@link RuntimeCamelException} if processing of
 * the {@link Exchange} failed and an Exception occured. The <tt>getCause</tt>
 * method on {@link RuntimeCamelException} returns the wrapper original caused
 * exception.
 * <p/>
 * All the send<b>Body</b> methods will return the content according to this strategy
 * <ul>
 *   <li>throws {@link RuntimeCamelException} as stated above</li>
 *   <li>The <tt>fault.body</tt> if there is a fault message set and its not <tt>null</tt></li>
 *   <li>Either <tt>IN</tt> or <tt>OUT</tt> body according to the message exchange pattern. If the pattern is
 *   Out capable then the <tt>OUT</tt> body is returned, otherwise <tt>IN</tt>.
 * </ul>
 * <p/>
 * <b>Important note on usage:</b> See this
 * <a href="http://camel.apache.org/why-does-camel-use-too-many-threads-with-producertemplate.html">FAQ entry</a>
 * before using.
 *
 * @version $Revision$
 */
public interface ProducerTemplate extends Service {

    /**
     * Sends the exchange to the default endpoint
     *
     * @param exchange the exchange to send
     * @return the returned exchange
     */
    Exchange send(Exchange exchange);

    /**
     * Sends an exchange to the default endpoint using a supplied processor
     *
     * @param processor the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange
     * @return the returned exchange
     */
    Exchange send(Processor processor);

    /**
     * Sends the body to the default endpoint
     *
     * @param body the payload to send
     */
    void sendBody(Object body);

    /**
     * Sends the body to the default endpoint with a specified header and header
     * value
     *
     * @param body the payload to send
     * @param header the header name
     * @param headerValue the header value
     */
    void sendBodyAndHeader(Object body, String header, Object headerValue);

    /**
     * Sends the body to the default endpoint with a specified property and property
     * value
     *
     * @param body          the payload to send
     * @param property      the property name
     * @param propertyValue the property value
     */
    void sendBodyAndProperty(Object body, String property, Object propertyValue);
    
    /**
     * Sends the body to the default endpoint with the specified headers and
     * header values
     *
     * @param body the payload to send
     * @param headers      the headers
     */
    void sendBodyAndHeaders(Object body, Map<String, Object> headers);

    // Allow sending to arbitrary endpoints
    // -----------------------------------------------------------------------

    /**
     * Sends the exchange to the given endpoint
     *
     * @param endpointUri the endpoint URI to send the exchange to
     * @param exchange    the exchange to send
     * @return the returned exchange
     */
    Exchange send(String endpointUri, Exchange exchange);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpointUri the endpoint URI to send the exchange to
     * @param processor   the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange
     * @return the returned exchange
     */
    Exchange send(String endpointUri, Processor processor);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpointUri the endpoint URI to send the exchange to
     * @param pattern     the message {@link ExchangePattern} such as
     *                    {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param processor   the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange
     * @return the returned exchange
     */
    Exchange send(String endpointUri, ExchangePattern pattern, Processor processor);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpointUri the endpoint URI to send the exchange to
     * @param processor   the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange.
     * @param callback    the callback will be called when the exchange is completed.
     * @return the returned exchange
     * @deprecated a new async API is planned for Camel 2.0
     */
    Exchange send(String endpointUri, Processor processor, AsyncCallback callback);

    /**
     * Sends the exchange to the given endpoint
     *
     * @param endpoint the endpoint to send the exchange to
     * @param exchange the exchange to send
     * @return the returned exchange
     */
    Exchange send(Endpoint endpoint, Exchange exchange);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpoint  the endpoint to send the exchange to
     * @param processor the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange
     * @return the returned exchange
     */
    Exchange send(Endpoint endpoint, Processor processor);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpoint  the endpoint to send the exchange to
     * @param pattern   the message {@link ExchangePattern} such as
     *                  {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param processor the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange
     * @return the returned exchange
     */
    Exchange send(Endpoint endpoint, ExchangePattern pattern, Processor processor);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     *
     * @param endpoint  the endpoint to send the exchange to
     * @param processor the transformer used to populate the new exchange
     * {@link Processor} to populate the exchange.
     * @param callback  the callback will be called when the exchange is completed.
     * @return the returned exchange
     * @deprecated a new async API is planned for Camel 2.0
     */
    Exchange send(Endpoint endpoint, Processor processor, AsyncCallback callback);

    /**
     * Send the body to an endpoint
     *
     * @param endpoint   the endpoint to send the exchange to
     * @param body       the payload
     */
    void sendBody(Endpoint endpoint, Object body);

    /**
     * Send the body to an endpoint
     *
     * @param endpointUri   the endpoint URI to send the exchange to
     * @param body          the payload
     */
    void sendBody(String endpointUri, Object body);

    /**
     * Send the body to an endpoint with the given {@link ExchangePattern}
     * returning any result output body
     *
     * @param endpoint      the endpoint to send the exchange to
     * @param body          the payload
     * @param pattern       the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBody(Endpoint endpoint, ExchangePattern pattern, Object body);

    /**
     * Send the body to an endpoint returning any result output body
     *
     * @param endpointUri   the endpoint URI to send the exchange to
     * @param pattern       the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body          the payload
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBody(String endpointUri, ExchangePattern pattern, Object body);

    /**
     * Sends the body to an endpoint with a specified header and header value
     *
     * @param endpointUri the endpoint URI to send to
     * @param body the payload to send
     * @param header the header name
     * @param headerValue the header value
     */
    void sendBodyAndHeader(String endpointUri, Object body, String header, Object headerValue);

    /**
     * Sends the body to an endpoint with a specified header and header value
     *
     * @param endpoint the Endpoint to send to
     * @param body the payload to send
     * @param header the header name
     * @param headerValue the header value
     */
    void sendBodyAndHeader(Endpoint endpoint, Object body, String header, Object headerValue);

    /**
     * Sends the body to an endpoint with a specified header and header value
     *
     * @param endpoint the Endpoint to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param header the header name
     * @param headerValue the header value
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndHeader(Endpoint endpoint, ExchangePattern pattern, Object body,
                             String header, Object headerValue);

    /**
     * Sends the body to an endpoint with a specified header and header value
     *
     * @param endpoint the Endpoint URI to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param header the header name
     * @param headerValue the header value
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndHeader(String endpoint, ExchangePattern pattern, Object body,
                             String header, Object headerValue);

    /**
     * Sends the body to an endpoint with a specified property and property value
     *
     * @param endpointUri the endpoint URI to send to
     * @param body the payload to send
     * @param property the property name
     * @param propertyValue the property value
     */
    void sendBodyAndProperty(String endpointUri, Object body, String property, Object propertyValue);

    /**
     * Sends the body to an endpoint with a specified property and property value
     *
     * @param endpoint the Endpoint to send to
     * @param body the payload to send
     * @param property the property name
     * @param propertyValue the property value
     */
    void sendBodyAndProperty(Endpoint endpoint, Object body, String property, Object propertyValue);

    /**
     * Sends the body to an endpoint with a specified property and property value
     *
     * @param endpoint the Endpoint to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param property the property name
     * @param propertyValue the property value
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndProperty(Endpoint endpoint, ExchangePattern pattern, Object body,
                               String property, Object propertyValue);

    /**
     * Sends the body to an endpoint with a specified property and property value
     *
     * @param endpoint the Endpoint URI to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param property the property name
     * @param propertyValue the property value
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndProperty(String endpoint, ExchangePattern pattern, Object body,
                               String property, Object propertyValue);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values
     *
     * @param endpointUri the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     */
    void sendBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values
     *
     * @param endpoint the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     */
    void sendBodyAndHeaders(Endpoint endpoint, Object body, Map<String, Object> headers);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values
     *
     * @param endpointUri the endpoint URI to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param headers headers
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndHeaders(String endpointUri, ExchangePattern pattern, Object body,
                              Map<String, Object> headers);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values
     *
     * @param endpoint the endpoint URI to send to
     * @param pattern the message {@link ExchangePattern} such as
     *   {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut}
     * @param body the payload to send
     * @param headers headers
     * @return the result if {@link ExchangePattern} is OUT capable, otherwise <tt>null</tt>
     */
    Object sendBodyAndHeaders(Endpoint endpoint, ExchangePattern pattern, Object body,
                              Map<String, Object> headers);


    // Methods using an InOut ExchangePattern
    // -----------------------------------------------------------------------

    /**
     * Sends an exchange to an endpoint using a supplied processor
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint  the Endpoint to send to
     * @param processor the processor which will populate the exchange before sending
     * @return the result (see class javadoc)
     */
    Exchange request(Endpoint endpoint, Processor processor);

    /**
     * Sends an exchange to an endpoint using a supplied processor
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param processor the processor which will populate the exchange before sending
     * @return the result (see class javadoc)
     */
    Exchange request(String endpointUri, Processor processor);

    /**
     * Sends the body to the default endpoint and returns the result content
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param body the payload to send
     * @return the result (see class javadoc)
     */
    Object requestBody(Object body);

    /**
     * Sends the body to the default endpoint and returns the result content
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param body the payload to send
     * @return the result (see class javadoc)
     */
    <T> T requestBody(Object body, Class<T> type);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint the Endpoint to send to
     * @param body     the payload
     * @return the result (see class javadoc)
     */
    Object requestBody(Endpoint endpoint, Object body);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint the Endpoint to send to
     * @param body     the payload
     * @param type     the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBody(Endpoint endpoint, Object body, Class<T> type);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body        the payload
     * @return the result (see class javadoc)
     */
    Object requestBody(String endpointUri, Object body);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body        the payload
     * @param type        the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBody(String endpointUri, Object body, Class<T> type);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint    the Endpoint to send to
     * @param body        the payload
     * @param header      the header name
     * @param headerValue the header value
     * @return the result (see class javadoc)
     */
    Object requestBodyAndHeader(Endpoint endpoint, Object body, String header, Object headerValue);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint    the Endpoint to send to
     * @param body        the payload
     * @param header      the header name
     * @param headerValue the header value
     * @param type        the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBodyAndHeader(Endpoint endpoint, Object body, String header, Object headerValue, Class<T> type);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body        the payload
     * @param header      the header name
     * @param headerValue the header value
     * @return the result (see class javadoc)
     */
    Object requestBodyAndHeader(String endpointUri, Object body, String header, Object headerValue);

    /**
     * Send the body to an endpoint returning any result output body.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body        the payload
     * @param header      the header name
     * @param headerValue the header value
     * @param type        the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBodyAndHeader(String endpointUri, Object body, String header, Object headerValue, Class<T> type);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     * @return the result (see class javadoc)
     */
    Object requestBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpointUri the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     * @param type the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers, Class<T> type);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     * @return the result (see class javadoc)
     */
    Object requestBodyAndHeaders(Endpoint endpoint, Object body, Map<String, Object> headers);

    /**
     * Sends the body to an endpoint with the specified headers and header
     * values.
     * Uses an {@link ExchangePattern#InOut} message exchange pattern.
     *
     * @param endpoint the endpoint URI to send to
     * @param body the payload to send
     * @param headers headers
     * @param type the expected response type
     * @return the result (see class javadoc)
     */
    <T> T requestBodyAndHeaders(Endpoint endpoint, Object body, Map<String, Object> headers, Class<T> type);
}
