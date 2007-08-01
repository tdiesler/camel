/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.impl.LoggingExceptionHandler;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;

/**
 * A base class for any kind of {@link Processor} which implements some kind of batch processing.
 *
 * @version $Revision: 1.1 $
 */
public class BatchProcessor extends ServiceSupport implements Runnable {
    private static final transient Log log = LogFactory.getLog(Resequencer.class);
    private Endpoint endpoint;
    private Processor processor;
    private Collection<Exchange> collection;
    private long batchTimeout = 1000L;
    private int batchSize = 100;
    private PollingConsumer consumer;
    private ExceptionHandler exceptionHandler;

    public BatchProcessor(Endpoint endpoint, Processor processor, Collection<Exchange> collection) {
        this.endpoint = endpoint;
        this.processor = processor;
        this.collection = collection;
    }

    @Override
    public String toString() {
        return "BatchProcessor[to: " + processor + "]";
    }

    public void run() {
        log.debug("Starting thread for " + this);
        while (!isStopped() && !isStopping()) {
            try {
                processBatch();
            }
            catch (Exception e) {
                getExceptionHandler().handleException(e);
            }
        }
        collection.clear();
    }

    // Properties
    //-------------------------------------------------------------------------
    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new LoggingExceptionHandler(getClass());
        }
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Processor getProcessor() {
        return processor;
    }

    /**
     * A transactional method to process a batch of messages up to a timeout period
     * or number of messages reached.
     */
    protected synchronized void processBatch() throws Exception {
        long start = System.currentTimeMillis();
        long end = start + batchTimeout;
        for (int i = 0; i < batchSize; i++) {
            long timeout = end - System.currentTimeMillis();

            Exchange exchange = consumer.receive(timeout);
            if (exchange == null) {
                break;
            }
            collection.add(exchange);
        }

        if (log.isDebugEnabled()) {
            log.debug("Finsihed batch size: " + batchSize + " timeout: " + batchTimeout + " so sending set: " + collection);
        }

        // lets send the batch
        Iterator<Exchange> iter = collection.iterator();
        while (iter.hasNext()) {
            Exchange exchange = iter.next();
            iter.remove();
            processExchange(exchange);
        }
    }

    /**
     * Strategy Method to process an exchange in the batch. This method allows derived classes
     * to perform custom processing before or after an individual exchange is processed
     */
    protected void processExchange(Exchange exchange) throws Exception {
        processor.process(exchange);
    }

    protected void doStart() throws Exception {
        consumer = endpoint.createPollingConsumer();

        ServiceHelper.startServices(processor, consumer);

        Thread thread = new Thread(this, this + " Polling Thread");
        thread.start();
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopServices(consumer, processor);
        consumer = null;
        collection.clear();
    }

    protected Collection<Exchange> getCollection() {
        return collection;
    }
}
