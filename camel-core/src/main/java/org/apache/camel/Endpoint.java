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
 * An <a href="http://camel.apache.org/endpoint.html">endpoint</a>
 * implements the <a
 * href="http://camel.apache.org/message-endpoint.html">Message
 * Endpoint</a> pattern and represents an endpoint that can send and receive
 * message exchanges
 *
 * @see Exchange
 * @see Message
 * @version $Revision$
 */
public interface Endpoint {

    /**
     * Returns if the endpoint should be a CamelContext singleton. If the
     * endpoint is a Singleton, then a single Endpoint instance will be shared
     * by all routes with the same URI. Because the endpoint is shared, it
     * should be treated as an immutable.
     */
    boolean isSingleton();

    /**
     * Returns the string representation of the endpoint URI
     */
    String getEndpointUri();

    /**
     * Create a new exchange for communicating with this endpoint
     */
    Exchange createExchange();

    /**
     * Create a new exchange for communicating with this endpoint
     * with the specified {@link ExchangePattern} such as whether its going
     * to be an {@link ExchangePattern#InOnly} or {@link ExchangePattern#InOut} exchange
     *
     * @param pattern the message exchange pattern for the exchange
     */
    Exchange createExchange(ExchangePattern pattern);

    /**
     * Creates a new exchange for communicating with this exchange using the
     * given exchange to pre-populate the values of the headers and messages
     *
     * @param exchange given exchange to use for pre-polulate
     */
    Exchange createExchange(Exchange exchange);

    /**
     * Returns the context which created the endpoint
     *
     * @return the context which created the endpoint
     */
    CamelContext getCamelContext();

    /**
     * Creates a new producer which is used send messages into the endpoint
     *
     * @return a newly created producer
     * @throws Exception can be thrown
     */
    Producer createProducer() throws Exception;

    /**
     * Creates a new <a
     * href="http://camel.apache.org/event-driven-consumer.html">Event
     * Driven Consumer</a> which consumes messages from the endpoint using the
     * given processor
     *
     * @param processor  the given processor
     * @return a newly created consumer
     * @throws Exception can be thrown
     */
    Consumer createConsumer(Processor processor) throws Exception;

    /**
     * Creates a new <a
     * href="http://camel.apache.org/polling-consumer.html">Polling
     * Consumer</a> so that the caller can poll message exchanges from the
     * consumer using {@link PollingConsumer#receive()},
     * {@link PollingConsumer#receiveNoWait()} or
     * {@link PollingConsumer#receive(long)} whenever it is ready to do so
     * rather than using the <a
     * href="http://camel.apache.org/event-driven-consumer.html">Event
     * Based Consumer</a> returned by {@link #createConsumer(Processor)}
     *
     * @return a newly created pull consumer
     * @throws Exception if the pull consumer could not be created
     */
    PollingConsumer createPollingConsumer() throws Exception;

    /**
     * Configure properties on this endpoint.
     * 
     * @param options  the options (properties)
     */
    void configureProperties(Map options);

    /**
     * Sets the camel context.
     *
     * @param context the camel context
     */
    void setCamelContext(CamelContext context);

    /**
     * Should all properties be known or does the endpoint allow unknown options?
     * <p/>
     * <tt>lenient = false</tt> means that the endpoint should validate that all
     * given options is known and configured properly.
     * <tt>lenient = true</tt> means that the endpoint allows additional unknown options to
     * be passed to it but does not throw a ResolveEndpointFailedException when creating
     * the endpoint.
     * <p/>
     * This options is used by a few components for instance the HTTP based that can have
     * dynamic URI options appended that is targeted for an external system.
     * <p/>
     * Most endpoints is configured to be <b>not</b> lenient.
     */
    boolean isLenientProperties();
}
