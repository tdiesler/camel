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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Consumer;
import org.apache.camel.Route;
import org.apache.camel.ShutdownRoute;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.SuspendableService;
import org.apache.camel.spi.RouteStartupOrder;
import org.apache.camel.spi.ShutdownAware;
import org.apache.camel.spi.ShutdownStrategy;
import org.apache.camel.util.EventHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default {@link org.apache.camel.spi.ShutdownStrategy} which uses graceful shutdown.
 * <p/>
 * Graceful shutdown ensures that any inflight and pending messages will be taken into account
 * and it will wait until these exchanges has been completed.
 * <p/>
 * As this strategy will politely wait until all exchanges has been completed it can potential wait
 * for a long time, and hence why a timeout value can be set. When the timeout triggers you can also
 * specify whether the remainder consumers should be shutdown now or ignore.
 * <p/>
 * Will by default use a timeout of 300 seconds (5 minutes) by which it will shutdown now the remaining consumers.
 * This ensures that when shutting down Camel it at some point eventually will shutdown.
 * This behavior can of course be configured using the {@link #setTimeout(long)} and
 * {@link #setShutdownNowOnTimeout(boolean)} methods.
 *
 * @version $Revision$
 */
public class DefaultShutdownStrategy extends ServiceSupport implements ShutdownStrategy, CamelContextAware {
    private static final transient Log LOG = LogFactory.getLog(DefaultShutdownStrategy.class);

    private CamelContext camelContext;
    private ExecutorService executor;
    private long timeout = 5 * 60;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private boolean shutdownNowOnTimeout = true;

    public DefaultShutdownStrategy() {
    }

    public DefaultShutdownStrategy(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void shutdown(CamelContext context, List<RouteStartupOrder> routes) throws Exception {
        shutdown(context, routes, getTimeout(), getTimeUnit());
    }

    public void shutdown(CamelContext context, List<RouteStartupOrder> routes, long timeout, TimeUnit timeUnit) throws Exception {
        long start = System.currentTimeMillis();

        if (timeout > 0) {
            LOG.info("Starting to graceful shutdown " + routes.size() + " routes (timeout " + timeout + " " + timeUnit.toString().toLowerCase() + ")");
        } else {
            LOG.info("Starting to graceful shutdown " + routes.size() + " routes (no timeout)");
        }

        // use another thread to perform the shutdowns so we can support timeout
        Future future = getExecutorService().submit(new ShutdownTask(context, routes));
        try {
            if (timeout > 0) {
                future.get(timeout, timeUnit);
            } else {
                future.get();
            }
        } catch (TimeoutException e) {
            // timeout then cancel the task
            future.cancel(true);

            if (shutdownNowOnTimeout) {
                LOG.warn("Timeout occurred. Now forcing the routes to be shutdown now.");
                // force the routes to shutdown now
                shutdownRoutesNow(routes);
            } else {
                LOG.warn("Timeout occurred. Will ignore shutting down the remainder route input consumers.");
            }
        } catch (ExecutionException e) {
            // unwrap execution exception
            throw ObjectHelper.wrapRuntimeCamelException(e.getCause());
        }

        long delta = System.currentTimeMillis() - start;
        // convert to seconds as its easier to read than a big milli seconds number
        long seconds = TimeUnit.SECONDS.convert(delta, TimeUnit.MILLISECONDS);

        LOG.info("Graceful shutdown of " + routes.size() + " routes completed in " + seconds + " seconds");
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setShutdownNowOnTimeout(boolean shutdownNowOnTimeout) {
        this.shutdownNowOnTimeout = shutdownNowOnTimeout;
    }

    public boolean isShutdownNowOnTimeout() {
        return shutdownNowOnTimeout;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Shutdown all the consumers immediately.
     *
     * @param routes the routes to shutdown
     */
    protected void shutdownRoutesNow(List<RouteStartupOrder> routes) {
        for (RouteStartupOrder order : routes) {

            // set the route to shutdown as fast as possible by stopping after
            // it has completed its current task
            ShutdownRunningTask current = order.getRoute().getRouteContext().getShutdownRunningTask();
            if (current != ShutdownRunningTask.CompleteCurrentTaskOnly) {
                LOG.info("Changing shutdownRunningTask from " + current + " to " +  ShutdownRunningTask.CompleteCurrentTaskOnly
                    + " on route " + order.getRoute().getId() + " to shutdown faster");
                order.getRoute().getRouteContext().setShutdownRunningTask(ShutdownRunningTask.CompleteCurrentTaskOnly);
            }

            for (Consumer consumer : order.getInputs()) {
                shutdownNow(consumer);
            }
        }
    }

    /**
     * Shutdown all the consumers immediately.
     *
     * @param consumers the consumers to shutdown
     */
    protected void shutdownNow(List<Consumer> consumers) {
        for (Consumer consumer : consumers) {
            shutdownNow(consumer);
        }
    }

    /**
     * Shutdown the consumer immediately.
     *
     * @param consumer the consumer to shutdown
     */
    protected void shutdownNow(Consumer consumer) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Shutting down: " + consumer);
        }

        // allow us to do custom work before delegating to service helper
        try {
            ServiceHelper.stopService(consumer);
        } catch (Exception e) {
            LOG.warn("Error occurred while shutting down route: " + consumer + ". This exception will be ignored.");
            // fire event
            EventHelper.notifyServiceStopFailure(consumer.getEndpoint().getCamelContext(), consumer, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutdown complete for: " + consumer);
        }
    }

    /**
     * Suspends the consumer immediately.
     *
     * @param service the suspendable consumer
     * @param consumer the consumer to suspend
     */
    protected void suspendNow(SuspendableService service, Consumer consumer) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Suspending: " + consumer);
        }

        try {
            service.suspend();
        } catch (Exception e) {
            LOG.warn("Error occurred while suspending route: " + consumer + ". This exception will be ignored.");
            // fire event
            EventHelper.notifyServiceStopFailure(consumer.getEndpoint().getCamelContext(), consumer, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Suspend complete for: " + consumer);
        }
    }

    private ExecutorService getExecutorService() {
        if (executor == null) {
            executor = camelContext.getExecutorServiceStrategy().newSingleThreadExecutor(this, "ShutdownTask");
        }
        return executor;
    }

    @Override
    protected void doStart() throws Exception {
        ObjectHelper.notNull(camelContext, "CamelContext");
    }

    @Override
    protected void doStop() throws Exception {
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = null;
    }

    class ShutdownDeferredConsumer {
        private final Route route;
        private final Consumer consumer;

        ShutdownDeferredConsumer(Route route, Consumer consumer) {
            this.route = route;
            this.consumer = consumer;
        }

        Route getRoute() {
            return route;
        }

        Consumer getConsumer() {
            return consumer;
        }
    }

    /**
     * Shutdown task which shutdown all the routes in a graceful manner.
     */
    class ShutdownTask implements Runnable {

        private final CamelContext context;
        private final List<RouteStartupOrder> routes;

        public ShutdownTask(CamelContext context, List<RouteStartupOrder> routes) {
            this.context = context;
            this.routes = routes;
        }

        public void run() {
            // the strategy in this run method is to
            // 1) go over the routes and shutdown those routes which can be shutdown asap
            //    some routes will be deferred to shutdown at the end, as they are needed
            //    by other routes so they can complete their tasks
            // 2) wait until all inflight and pending exchanges has been completed
            // 3) shutdown the deferred routes

            if (LOG.isDebugEnabled()) {
                LOG.debug("There are " + routes.size() + " routes to shutdown");
            }

            // list of deferred consumers to shutdown when all exchanges has been completed routed
            // and thus there are no more inflight exchanges so they can be safely shutdown at that time
            List<ShutdownDeferredConsumer> deferredConsumers = new ArrayList<ShutdownDeferredConsumer>();

            for (RouteStartupOrder order : routes) {

                ShutdownRoute shutdownRoute = order.getRoute().getRouteContext().getShutdownRoute();
                ShutdownRunningTask shutdownRunningTask = order.getRoute().getRouteContext().getShutdownRunningTask();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Shutting down route: " + order.getRoute().getId() + " with options [" + shutdownRoute + "," + shutdownRunningTask + "]");
                }

                for (Consumer consumer : order.getInputs()) {

                    boolean suspend = false;

                    // assume we should shutdown if we are not deferred
                    boolean shutdown = shutdownRoute != ShutdownRoute.Defer;

                    if (shutdown) {
                        // if we are to shutdown then check whether we can suspend instead as its a more
                        // gentle way to graceful shutdown

                        // some consumers do not support shutting down so let them decide
                        // if a consumer is suspendable then prefer to use that and then shutdown later
                        if (consumer instanceof ShutdownAware) {
                            shutdown = !((ShutdownAware) consumer).deferShutdown(shutdownRunningTask);
                        }
                        if (shutdown && consumer instanceof SuspendableService) {
                            // we prefer to suspend over shutdown
                            suspend = true;
                        }
                    }

                    if (suspend) {
                        // only suspend it and then later shutdown it
                        suspendNow((SuspendableService) consumer, consumer);
                        // add it to the deferred list so the route will be shutdown later
                        deferredConsumers.add(new ShutdownDeferredConsumer(order.getRoute(), consumer));
                        LOG.info("Route: " + order.getRoute().getId() + " suspended and shutdown deferred.");
                    } else if (shutdown) {
                        shutdownNow(consumer);
                        LOG.info("Route: " + order.getRoute().getId() + " shutdown complete.");
                    } else {
                        // we will stop it later, but for now it must run to be able to help all inflight messages
                        // be safely completed
                        deferredConsumers.add(new ShutdownDeferredConsumer(order.getRoute(), consumer));
                        LOG.info("Route: " + order.getRoute().getId() + " shutdown deferred.");
                    }
                }
            }

            // wait till there are no more pending and inflight messages
            boolean done = false;
            while (!done) {
                int size = 0;
                for (RouteStartupOrder order : routes) {
                    for (Consumer consumer : order.getInputs()) {
                        int inflight = context.getInflightRepository().size(consumer.getEndpoint());
                        // include any additional pending exchanges on some consumers which may have internal
                        // memory queues such as seda
                        if (consumer instanceof ShutdownAware) {
                            inflight += ((ShutdownAware) consumer).getPendingExchangesSize();
                        }
                        if (inflight > 0) {
                            size += inflight;
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(inflight + " inflight and pending exchanges for consumer: " + consumer);
                            }
                        }
                    }
                }
                if (size > 0) {
                    try {
                        LOG.info("Waiting as there are still " + size + " inflight and pending exchanges to complete before we can shutdown");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted while waiting during graceful shutdown, will force shutdown now.");
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    done = true;
                }
            }

            // now all messages has been completed then stop the deferred consumers
            for (ShutdownDeferredConsumer deferred : deferredConsumers) {
                shutdownNow(deferred.getConsumer());
                LOG.info("Route: " + deferred.getRoute().getId() + " shutdown complete.");
            }
        }

    }

}
