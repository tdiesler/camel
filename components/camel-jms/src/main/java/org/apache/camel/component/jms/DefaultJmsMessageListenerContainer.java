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
package org.apache.camel.component.jms;

import org.apache.camel.util.concurrent.CamelThreadFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * The default {@link DefaultMessageListenerContainer container} which listen for messages
 * on the JMS destination.
 * <p/>
 * This implementation extends Springs {@link DefaultMessageListenerContainer} supporting
 * automatic recovery and throttling.
 *
 * @version 
 */
public class DefaultJmsMessageListenerContainer extends DefaultMessageListenerContainer {

    private final JmsEndpoint endpoint;
    private final boolean allowQuickStop;

    public DefaultJmsMessageListenerContainer(JmsEndpoint endpoint) {
        this(endpoint, true);
    }

    public DefaultJmsMessageListenerContainer(JmsEndpoint endpoint, boolean allowQuickStop) {
        this.endpoint = endpoint;
        this.allowQuickStop = allowQuickStop;
    }

    /**
     * Whether this {@link DefaultMessageListenerContainer} allows the {@link #runningAllowed()} to quick stop
     * in case {@link JmsConfiguration#isAcceptMessagesWhileStopping()} is enabled, and {@link org.apache.camel.CamelContext}
     * is currently being stopped.
     */
    protected boolean isAllowQuickStop() {
        return allowQuickStop;
    }

    @Override
    protected boolean runningAllowed() {
        // we can stop quickly if CamelContext is being stopped, and we do not accept messages while stopping
        // this allows a more cleanly shutdown of the message listener
        boolean quickStop = false;
        if (isAllowQuickStop() && !endpoint.isAcceptMessagesWhileStopping()) {
            quickStop = endpoint.getCamelContext().getStatus().isStopping();
        }

        if (quickStop) {
            // log at debug level so its quicker to see we are stopping quicker from the logs
            logger.debug("runningAllowed() -> false due CamelContext is stopping and endpoint configured to not accept messages while stopping");
            return false;
        } else {
            // otherwise we only run if the endpoint is running
            boolean answer = endpoint.isRunning();
            // log at trace level as otherwise this can be noisy during normal operation
            if (logger.isTraceEnabled()) {
                logger.trace("runningAllowed() -> " + answer);
            }
            return answer;
        }
    }

    /**
     * Create a default TaskExecutor. Called if no explicit TaskExecutor has been specified.
     * <p />
     * The type of {@link TaskExecutor} will depend on the value of
     * {@link JmsConfiguration#getDefaultTaskExecutorType()}. For more details, refer to the Javadoc of
     * {@link DefaultTaskExecutorType}.
     * <p />
     * In all cases, it uses the specified bean name and Camel's {@link org.apache.camel.spi.ExecutorServiceManager}
     * to resolve the thread name.
     * @see JmsConfiguration#setDefaultTaskExecutorType(DefaultTaskExecutorType)
     * @see ThreadPoolTaskExecutor#setBeanName(String)
     */
    @Override
    protected TaskExecutor createDefaultTaskExecutor() {
        String pattern = endpoint.getCamelContext().getExecutorServiceManager().getThreadNamePattern();
        String beanName = getBeanName() == null ? endpoint.getThreadName() : getBeanName();

        if (endpoint.getDefaultTaskExecutorType() == DefaultTaskExecutorType.ThreadPool) {
            ThreadPoolTaskExecutor answer = new ThreadPoolTaskExecutor();
            answer.setBeanName(beanName);
            answer.setThreadFactory(new CamelThreadFactory(pattern, beanName, true));
            answer.setCorePoolSize(endpoint.getConcurrentConsumers());
            // Direct hand-off mode. Do not queue up tasks: assign it to a thread immediately.
            // We set no upper-bound on the thread pool (no maxPoolSize) as it's already implicitly constrained by
            // maxConcurrentConsumers on the DMLC itself (i.e. DMLC will only grow up to a level of concurrency as
            // defined by maxConcurrentConsumers).
            answer.setQueueCapacity(0);
            answer.initialize();
            return answer;
        } else {
            SimpleAsyncTaskExecutor answer = new SimpleAsyncTaskExecutor(beanName);
            answer.setThreadFactory(new CamelThreadFactory(pattern, beanName, true));
            return answer;
        }
    }

    @Override
    public void stop() throws JmsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Stopping listenerContainer: " + this + " with cacheLevel: " + getCacheLevel()
                    + " and sharedConnectionEnabled: " + sharedConnectionEnabled());
        }
        super.stop();
    }

    @Override
    public void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying listenerContainer: " + this + " with cacheLevel: " + getCacheLevel()
                    + " and sharedConnectionEnabled: " + sharedConnectionEnabled());
        }
        super.destroy();
    }

    @Override
    protected void stopSharedConnection() {
        if (logger.isDebugEnabled()) {
            if (sharedConnectionEnabled()) {
                logger.debug("Stopping shared connection on listenerContainer: " + this);
            }
        }
        super.stopSharedConnection();
    }
    
    /**
     * Refresh the underlying Connection, not returning before an attempt has been
     * successful. Called in case of a shared Connection as well as without shared
     * Connection, so either needs to operate on the shared Connection or on a
     * temporary Connection that just gets established for validation purposes.
     * <p>The default implementation retries until it successfully established a
     * Connection, for as long as this message listener container is running.
     * Applies the specified recovery interval between retries.
     * @see #setRecoveryInterval
     * @see #start()
     * @see #stop()
     */
    protected void refreshConnectionUntilSuccessful() {
        while (isRunning()) {
            try {
                if (sharedConnectionEnabled()) {
                    refreshSharedConnection();
                }
                else {
                    Connection con = createConnection();
                    JmsUtils.closeConnection(con);
                }
                logger.info("Successfully refreshed JMS Connection");
                break;
            }
            catch (NullPointerException npe) {
                // spring-jms can get in a really weird state with connection pooling enabled... basically when we are in this
                // state the DMLC sharedConnection is null and org.apache.activemq.jms.pool.PooledConnectionFactory pool is empty so 
                // we get in this loop (spring-jms will keep trying until it is shutdown) of trying to create a sharedConnection 
                // from a PooledConnectionFactory with nothing in its pool... to workaround we can restart spring-jms or 
                // manually reset the CF.
                logger.debug("Could not refresh JMS Connection. Reinitializing ConnectionFactory to prevent reconnect loop.", npe);
                endpoint.getComponent().getConfiguration().setConnectionFactory(null);
                ConnectionFactory newConnectionFactory = endpoint.getComponent().getConfiguration().getConnectionFactory();
                if (newConnectionFactory != null) {
                    // camel CF is updated but we also need to update this spring DMLC
                    setConnectionFactory(newConnectionFactory);
                } else {
                    logger.debug("ConnectionFactory could not be recreated automatically. Please restart spring-jms.");
                }
            } catch (Exception ex) {
                if (ex instanceof JMSException) {
                    invokeExceptionListener((JMSException) ex);
                }
                StringBuilder msg = new StringBuilder();
                msg.append("Could not refresh JMS Connection for destination '");
                msg.append(getDestinationDescription()).append("' - retrying in ");
                msg.append(DEFAULT_RECOVERY_INTERVAL).append(" ms. Cause: ");
                msg.append(ex instanceof JMSException ? JmsUtils.buildExceptionMessage((JMSException) ex) : ex.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.error(msg, ex);
                }
                else {
                    logger.error(msg);
                }
            }
            
            sleepInbetweenRecoveryAttempts();
        }
    }
    
    /**
     * Sleep according to the specified recovery interval.
     * Called between recovery attempts.
     */
    protected void sleepInbetweenRecoveryAttempts() {
            if (DEFAULT_RECOVERY_INTERVAL > 0) {
                    try {
                            Thread.sleep(DEFAULT_RECOVERY_INTERVAL);
                    }
                    catch (InterruptedException interEx) {
                            // Re-interrupt current thread, to allow other threads to react.
                            Thread.currentThread().interrupt();
                    }
            }
    }

}
