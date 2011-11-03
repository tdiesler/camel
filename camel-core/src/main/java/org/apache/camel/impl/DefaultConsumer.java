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
package org.apache.camel.impl;

import org.apache.camel.AsyncProcessor;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.AsyncProcessorConverterHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default consumer useful for implementation inheritance.
 *
 * @version 
 */
public class DefaultConsumer extends ServiceSupport implements Consumer {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());
    private final Endpoint endpoint;
    private final Processor processor;
    private volatile AsyncProcessor asyncProcessor;
    private ExceptionHandler exceptionHandler;

    public DefaultConsumer(Endpoint endpoint, Processor processor) {
        this.endpoint = endpoint;
        this.processor = processor;
    }

    @Override
    public String toString() {
        return "Consumer[" + URISupport.sanitizeUri(endpoint.getEndpointUri()) + "]";
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Processor getProcessor() {
        return processor;
    }

    /**
     * Provides an {@link org.apache.camel.AsyncProcessor} interface to the configured
     * processor on the consumer. If the processor does not implement the interface,
     * it will be adapted so that it does.
     */
    public synchronized AsyncProcessor getAsyncProcessor() {
        if (asyncProcessor == null) {            
            asyncProcessor = AsyncProcessorConverterHelper.convert(processor);
        }
        return asyncProcessor;
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

    protected void doStop() throws Exception {
        log.debug("Stopping consumer: {}", this);
        ServiceHelper.stopServices(processor);
    }

    protected void doStart() throws Exception {
        log.debug("Starting consumer: {}", this);
        ServiceHelper.startServices(processor);
    }

    /**
     * Handles the given exception using the {@link #getExceptionHandler()}
     * 
     * @param t the exception to handle
     */
    protected void handleException(Throwable t) {
        Throwable newt = (t == null) ? new IllegalArgumentException("Handling [null] exception") : t;
        getExceptionHandler().handleException(newt);
    }
}
