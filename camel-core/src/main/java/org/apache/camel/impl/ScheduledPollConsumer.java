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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.SuspendableService;
import org.apache.camel.spi.PollingConsumerPollStrategy;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.concurrent.ExecutorServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A useful base class for any consumer which is polling based
 * 
 * @version $Revision$
 */
public abstract class ScheduledPollConsumer extends DefaultConsumer implements Runnable, SuspendableService {
    private static final int DEFAULT_THREADPOOL_SIZE = 10;
    private static final transient Log LOG = LogFactory.getLog(ScheduledPollConsumer.class);

    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    // if adding more options then align with ScheduledPollEndpoint#configureScheduledPollConsumerProperties
    private long initialDelay = 1000;
    private long delay = 500;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private boolean useFixedDelay;
    private PollingConsumerPollStrategy pollStrategy = new DefaultPollingConsumerPollStrategy();
    private boolean suspended;

    public ScheduledPollConsumer(DefaultEndpoint endpoint, Processor processor) {
        super(endpoint, processor);

        ScheduledExecutorService scheduled;
        ExecutorService service = endpoint.getExecutorService();
        if (service instanceof ScheduledExecutorService) {
            scheduled = (ScheduledExecutorService) service;
        } else {
            scheduled = ExecutorServiceHelper.newScheduledThreadPool(DEFAULT_THREADPOOL_SIZE, getEndpoint().getEndpointUri(), true);
        }

        this.executor = scheduled;
        ObjectHelper.notNull(executor, "executor");
    }

    public ScheduledPollConsumer(Endpoint endpoint, Processor processor, ScheduledExecutorService executor) {
        super(endpoint, processor);
        this.executor = executor;
        ObjectHelper.notNull(executor, "executor");
    }

    /**
     * Invoked whenever we should be polled
     */
    public void run() {
        if (suspended) {
            return;
        }

        int retryCounter = -1;
        boolean done = false;

        while (!done) {
            try {
                // eager assume we are done
                done = true;
                if (isRunAllowed()) {

                    if (retryCounter == -1) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Starting to poll: " + this.getEndpoint());
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Retrying attempt " + retryCounter + " to poll: " + this.getEndpoint());
                            }
                        }
                    }

                    pollStrategy.begin(this, getEndpoint());
                    retryCounter++;
                    poll();
                    pollStrategy.commit(this, getEndpoint());
                }
            } catch (Exception e) {
                try {
                    boolean retry = pollStrategy.rollback(this, getEndpoint(), retryCounter, e);
                    if (retry) {
                        done = false;
                    }
                } catch (Exception re) {
                    throw ObjectHelper.wrapRuntimeCamelException(re);
                }
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Finished polling: " + this.getEndpoint());
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public boolean isUseFixedDelay() {
        return useFixedDelay;
    }

    public void setUseFixedDelay(boolean useFixedDelay) {
        this.useFixedDelay = useFixedDelay;
    }

    public PollingConsumerPollStrategy getPollStrategy() {
        return pollStrategy;
    }

    public void setPollStrategy(PollingConsumerPollStrategy pollStrategy) {
        this.pollStrategy = pollStrategy;
    }

    public void suspend() {
        suspended = true;
    }

    public void resume() {
        suspended = false;
    }

    public boolean isSuspended() {
        return suspended;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * The polling method which is invoked periodically to poll this consumer
     * 
     * @throws Exception can be thrown if an exception occurred during polling
     */
    protected abstract void poll() throws Exception;

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (isUseFixedDelay()) {
            future = executor.scheduleWithFixedDelay(this, getInitialDelay(), getDelay(), getTimeUnit());
        } else {
            future = executor.scheduleAtFixedRate(this, getInitialDelay(), getDelay(), getTimeUnit());
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (future != null) {
            future.cancel(false);
        }
        super.doStop();
    }
}
