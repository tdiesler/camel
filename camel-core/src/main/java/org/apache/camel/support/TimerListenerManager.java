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
package org.apache.camel.support;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.camel.TimerListener;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TimerListener} manager which triggers the
 * {@link org.apache.camel.TimerListener} listeners once every second.
 * <p/>
 * The {@link #setExecutorService(java.util.concurrent.ScheduledExecutorService)} method
 * must be invoked prior to starting this manager using the {@link #start()} method.
 *
 * @see TimerListener
 */
public class TimerListenerManager extends ServiceSupport implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TimerListenerManager.class);
    private final Set<TimerListener> listeners = new LinkedHashSet<TimerListener>();
    private ScheduledExecutorService executorService;
    private volatile ScheduledFuture<?> task;
    private long interval = 1000L;

    public TimerListenerManager() {
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Gets the interval in millis.
     * <p/>
     * The default interval is 1000 millis.
     *
     * @return interval in millis.
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Sets the interval in millis.
     *
     * @param interval interval in millis.
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        LOG.trace("Running scheduled TimerListener task");

        if (!isRunAllowed()) {
            LOG.debug("TimerListener task cannot run as its not allowed");
            return;
        }

        for (TimerListener listener : listeners) {
            try {
                LOG.trace("Invoking onTimer on {}", listener);
                listener.onTimer();
            } catch (Throwable e) {
                // ignore
                LOG.debug("Error occurred during onTimer for TimerListener: " + listener + ". This exception will be ignored.", e);
            }
        }
    }

    public void addTimerListener(TimerListener listener) {
        listeners.add(listener);
        LOG.debug("Added TimerListener: {}", listener);
    }

    public void removeTimerListener(TimerListener listener) {
        listeners.remove(listener);
        LOG.debug("Removed TimerListener: {}", listener);
    }

    @Override
    protected void doStart() throws Exception {
        ObjectHelper.notNull(executorService, "executorService", this);
        task = executorService.scheduleAtFixedRate(this, 1000L, interval, TimeUnit.MILLISECONDS);
        LOG.debug("Started scheduled TimerListener task to run with interval {} ms", interval);
    }

    @Override
    protected void doStop() throws Exception {
        // executor service will be shutdown by CamelContext
        task.cancel(true);
    }

}

