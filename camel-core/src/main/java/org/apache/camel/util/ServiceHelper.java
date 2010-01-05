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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.camel.Service;
import org.apache.camel.SuspendableService;
import org.apache.camel.impl.ServiceSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of helper methods for working with {@link Service} objects
 *
 * @version $Revision$
 */
public final class ServiceHelper {
    private static final transient Log LOG = LogFactory.getLog(ServiceHelper.class);

    /**
     * Utility classes should not have a public constructor.
     */
    private ServiceHelper() {
    }

    public static void startService(Object value) throws Exception {
        if (value instanceof Service) {
            Service service = (Service)value;
            if (LOG.isTraceEnabled()) {
                LOG.trace("Starting service: " + service);
            }
            service.start();
        } else if (value instanceof Collection) {
            startServices((Collection<?>)value);
        }
    }

    /**
     * Starts all of the given services
     */
    public static void startServices(Object... services) throws Exception {
        for (Object value : services) {
            startService(value);
        }
    }

    /**
     * Starts all of the given services
     */
    public static void startServices(Collection<?> services) throws Exception {
        for (Object value : services) {
            if (value instanceof Service) {
                Service service = (Service)value;
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Starting service: " + service);
                }
                service.start();
            }
        }
    }

    /**
     * Stops all of the given services, throwing the first exception caught
     */
    public static void stopServices(Object... services) throws Exception {
        List<Object> list = Arrays.asList(services);
        stopServices(list);
    }

    public static void stopService(Object value) throws Exception {
        if (value instanceof Service) {
            Service service = (Service)value;
            if (LOG.isTraceEnabled()) {
                LOG.trace("Stopping service " + value);
            }
            service.stop();
        } else if (value instanceof Collection) {
            stopServices((Collection<?>)value);
        }
    }

    /**
     * Stops all of the given services, throwing the first exception caught
     */
    public static void stopServices(Collection<?> services) throws Exception {
        Exception firstException = null;
        for (Object value : services) {
            if (value instanceof Service) {
                Service service = (Service)value;
                try {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Stopping service: " + service);
                    }
                    service.stop();
                } catch (Exception e) {
                    LOG.debug("Caught exception shutting down: " + e, e);
                    if (firstException == null) {
                        firstException = e;
                    }
                }
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * Resumes the given service.
     * <p/>
     * If the service is a {@link org.apache.camel.SuspendableService} then the <tt>resume</tt>
     * operation is <b>only</b> invoked if the service is suspended.
     * <p/>
     * If the service is a {@link org.apache.camel.impl.ServiceSupport} then the <tt>start</tt>
     * operation is <b>only</b> invoked if the service is startable.
     * <p/>
     * Otherwise the service is started.
     *
     * @param service the service
     * @return <tt>true</tt> if either <tt>resume</tt> or <tt>start</tt> was invoked,
     * <tt>false</tt> if the service is already in the desired state.
     * @throws Exception is thrown if error occurred
     */
    public static boolean resumeService(Service service) throws Exception {
        if (service instanceof SuspendableService) {
            SuspendableService ss = (SuspendableService) service;
            if (ss.isSuspended()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Resuming service " + service);
                }
                ss.resume();
                return true;
            } else {
                return false;
            }
        } else if (service instanceof ServiceSupport) {
            ServiceSupport ss = (ServiceSupport) service;
            if (ss.getStatus().isStartable()) {
                startService(service);
                return true;
            } else {
                return false;
            }
        } else {
            startService(service);
            return true;
        }
    }

    /**
     * Suspends the given service.
     * <p/>
     * If the service is a {@link org.apache.camel.SuspendableService} then the <tt>suspend</tt>
     * operation is <b>only</b> invoked if the service is <b>not</b> suspended.
     * <p/>
     * If the service is a {@link org.apache.camel.impl.ServiceSupport} then the <tt>stop</tt>
     * operation is <b>only</b> invoked if the service is stopable.
     * <p/>
     * Otherwise the service is stopped.
     *
     * @param service the service
     * @return <tt>true</tt> if either <tt>suspend</tt> or <tt>stop</tt> was invoked,
     * <tt>false</tt> if the service is already in the desired state.
     * @throws Exception is thrown if error occurred
     */
    public static boolean suspendService(Service service) throws Exception {
        if (service instanceof SuspendableService) {
            SuspendableService ss = (SuspendableService) service;
            if (!ss.isSuspended()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Suspending service " + service);
                }
                ss.suspend();
                return true;
            } else {
                return false;
            }
        } else if (service instanceof ServiceSupport) {
            ServiceSupport ss = (ServiceSupport) service;
            if (ss.getStatus().isStoppable()) {
                stopServices(service);
                return true;
            } else {
                return false;
            }
        } else {
            stopService(service);
            return true;
        }
    }

}
