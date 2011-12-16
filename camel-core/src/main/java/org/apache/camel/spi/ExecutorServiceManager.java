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
package org.apache.camel.spi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.ShutdownableService;

/**
 * Strategy to create thread pools.
 * <p/>
 * This manager is pluggable so you can plugin a custom provider, for example if you want to leverage
 * the WorkManager for a JEE server.
 * <p/>
 * You may want to just implement a custom {@link ThreadPoolFactory} and rely on the
 * {@link org.apache.camel.impl.DefaultExecutorServiceManager}, if that is sufficient. The {@link ThreadPoolFactory}
 * is always used for creating the actual thread pools. You can implement a custom {@link ThreadPoolFactory}
 * to leverage the WorkManager for a JEE server.
 * <p/>
 * The {@link ThreadPoolFactory} has pure JDK API, where as this {@link ExecutorServiceManager} has Camel API
 * concepts such as {@link ThreadPoolProfile}. Therefore it may be easier to only implement a custom
 * {@link ThreadPoolFactory}.
 * <p/>
 * This manager has fine grained methods for creating various thread pools, however custom strategies
 * do not have to exactly create those kind of pools. Feel free to return a shared or different kind of pool.
 * <p/>
 * If you use the <tt>newXXX</tt> methods to create thread pools, then Camel will by default take care of
 * shutting down those created pools when {@link org.apache.camel.CamelContext} is shutting down.
 * <p/>
 * @see ThreadPoolFactory
 */
public interface ExecutorServiceManager extends ShutdownableService {

    /**
     * Gets the {@link ThreadPoolFactory} to use for creating the thread pools.
     *
     * @return the thread pool factory
     */
    ThreadPoolFactory getThreadPoolFactory();

    /**
     * Sets a custom {@link ThreadPoolFactory} to use
     *
     * @param threadPoolFactory the thread pool factory
     */
    void setThreadPoolFactory(ThreadPoolFactory threadPoolFactory);

    /**
     * Creates a full thread name
     *
     * @param name name which is appended to the full thread name
     * @return the full thread name
     */
    String resolveThreadName(String name);

    /**
     * Gets the thread pool profile by the given id
     *
     * @param id id of the thread pool profile to get
     * @return the found profile, or <tt>null</tt> if not found
     */
    ThreadPoolProfile getThreadPoolProfile(String id);

    /**
     * Registers the given thread pool profile
     *
     * @param profile the profile
     */
    void registerThreadPoolProfile(ThreadPoolProfile profile);

    /**
     * Sets the default thread pool profile
     *
     * @param defaultThreadPoolProfile the new default thread pool profile
     */
    void setDefaultThreadPoolProfile(ThreadPoolProfile defaultThreadPoolProfile);

    /**
     * Gets the default thread pool profile
     *
     * @return the default profile which are newer <tt>null</tt>
     */
    ThreadPoolProfile getDefaultThreadPoolProfile();

    /**
     * Sets the thread name pattern used for creating the full thread name.
     * <p/>
     * The default pattern is: <tt>Camel (${camelId}) thread #${counter} - ${name}</tt>
     * <p/>
     * Where <tt>${camelId}</tt> is the name of the {@link org.apache.camel.CamelContext}
     * <br/>and <tt>${counter}</tt> is a unique incrementing counter.
     * <br/>and <tt>${name}</tt> is the regular thread name.
     * <br/>You can also use <tt>${longName}</tt> is the long thread name which can includes endpoint parameters etc.
     *
     * @param pattern the pattern
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    void setThreadNamePattern(String pattern) throws IllegalArgumentException;

    /**
     * Gets the thread name patter to use
     *
     * @return the pattern
     */
    String getThreadNamePattern();

    /**
     * Creates a new thread pool using the default thread pool profile.
     *
     * @param source the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name   name which is appended to the thread name
     * @return the created thread pool
     */
    ExecutorService newDefaultThreadPool(Object source, String name);

    /**
     * Creates a new scheduled thread pool using the default thread pool profile.
     *
     * @param source the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name   name which is appended to the thread name
     * @return the created thread pool
     */
    ScheduledExecutorService newDefaultScheduledThreadPool(Object source, String name);

    /**
     * Creates a new thread pool using the given profile
     *
     * @param source   the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name     name which is appended to the thread name
     * @param profile the profile with the thread pool settings to use
     * @return the created thread pool
     */
    ExecutorService newThreadPool(Object source, String name, ThreadPoolProfile profile);

    /**
     * Creates a new thread pool using using the given profile id
     *
     * @param source    the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name      name which is appended to the thread name
     * @param profileId the id of the profile with the thread pool settings to use
     * @return the created thread pool, or <tt>null</tt> if the thread pool profile could not be found
     */
    ExecutorService newThreadPool(Object source, String name, String profileId);

    /**
     * Creates a new thread pool.
     * <p/>
     * Will fallback and use values from the default thread pool profile for keep alive time, rejection policy
     * and other parameters which cannot be specified.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @param poolSize    the core pool size
     * @param maxPoolSize the maximum pool size
     * @return the created thread pool
     */
    ExecutorService newThreadPool(Object source, String name, int poolSize, int maxPoolSize);

    /**
     * Creates a new single-threaded thread pool. This is often used for background threads.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @return the created thread pool
     */
    ExecutorService newSingleThreadExecutor(Object source, String name);

    /**
     * Creates a new cached thread pool.
     * <p/>
     * <b>Important:</b> Using cached thread pool is discouraged as they have no upper bound and can overload the JVM.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @return the created thread pool
     */
    ExecutorService newCachedThreadPool(Object source, String name);

    /**
     * Creates a new fixed thread pool.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @param poolSize    the core pool size
     * @return the created thread pool
     */
    ExecutorService newFixedThreadPool(Object source, String name, int poolSize);

    /**
     * Creates a new scheduled thread pool.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @param poolSize    the core pool size
     * @return the created thread pool
     */
    ScheduledExecutorService newScheduledThreadPool(Object source, String name, int poolSize);

    /**
     * Creates a new single-threaded thread pool. This is often used for background threads.
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @return the created thread pool
     */
    ScheduledExecutorService newSingleThreadScheduledExecutor(Object source, String name);
    
    /**
     * Creates a new scheduled thread pool using a profile
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @param profile     the profile with the thread pool settings to use
     * @return created thread pool
     */
    ScheduledExecutorService newScheduledThreadPool(Object source, String name, ThreadPoolProfile profile);

    /**
     * Creates a new scheduled thread pool using a profile id
     *
     * @param source      the source object, usually it should be <tt>this</tt> passed in as parameter
     * @param name        name which is appended to the thread name
     * @param profileId   the id of the profile with the thread pool settings to use
     * @return created thread pool
     */
    ScheduledExecutorService newScheduledThreadPool(Object source, String name, String profileId);

    /**
     * Shutdown the given executor service.
     *
     * @param executorService the executor service to shutdown
     * @see java.util.concurrent.ExecutorService#shutdown()
     */
    void shutdown(ExecutorService executorService);

    /**
     * Shutdown now the given executor service.
     *
     * @param executorService the executor service to shutdown now
     * @return list of tasks that never commenced execution
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     */
    List<Runnable> shutdownNow(ExecutorService executorService);

}
