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
package org.apache.camel.management;

import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.management.mbean.ManagedCamelContext;
import org.apache.camel.management.mbean.ManagedComponent;
import org.apache.camel.management.mbean.ManagedConsumer;
import org.apache.camel.management.mbean.ManagedEndpoint;
import org.apache.camel.management.mbean.ManagedErrorHandler;
import org.apache.camel.management.mbean.ManagedEventNotifier;
import org.apache.camel.management.mbean.ManagedProcessor;
import org.apache.camel.management.mbean.ManagedProducer;
import org.apache.camel.management.mbean.ManagedRoute;
import org.apache.camel.management.mbean.ManagedService;
import org.apache.camel.management.mbean.ManagedThreadPool;
import org.apache.camel.management.mbean.ManagedTracer;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.ManagementAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.commons.management.Statistic;
import org.fusesource.commons.management.basic.StatisticImpl;

/**
 * A JMX capable {@link org.apache.camel.spi.ManagementStrategy} that Camel by default uses if possible.
 * <p/>
 * Camel detects whether its possible to use this JMX capable strategy and if <b>not</b> then Camel
 * will fallback to the {@link org.apache.camel.management.DefaultManagementStrategy} instead.
 *
 * @see org.apache.camel.spi.ManagementStrategy
 * @see org.apache.camel.management.DefaultManagementStrategy
 * @version $Revision$
 */
public class ManagedManagementStrategy extends DefaultManagementStrategy {

    private static final transient Log LOG = LogFactory.getLog(ManagedManagementStrategy.class);

    public ManagedManagementStrategy() {
    }

    public ManagedManagementStrategy(CamelContext camelContext) {
        this(new DefaultManagementAgent(camelContext));
    }

    public ManagedManagementStrategy(ManagementAgent managementAgent) {
        setManagementAgent(managementAgent);
    }

    public void manageObject(Object managedObject) throws Exception {
        manageNamedObject(managedObject, null);
    }

    public void manageNamedObject(Object managedObject, Object preferedName) throws Exception {
        ObjectName objectName = getObjectName(managedObject, preferedName);

        if (objectName != null) {
            getManagementAgent().register(managedObject, objectName);
        }
    }

    public <T> T getManagedObjectName(Object managedObject, String customName, Class<T> nameType) throws Exception {
        if (managedObject == null) {
            return null;
        }

        ObjectName objectName = null;

        if (managedObject instanceof ManagedCamelContext) {
            ManagedCamelContext mcc = (ManagedCamelContext) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForCamelContext(mcc.getContext());
        } else if (managedObject instanceof ManagedComponent) {
            ManagedComponent mc = (ManagedComponent) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForComponent(mc.getComponent(), mc.getComponentName());
        } else if (managedObject instanceof ManagedEndpoint) {
            ManagedEndpoint me = (ManagedEndpoint) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForEndpoint(me.getEndpoint());
        } else if (managedObject instanceof Endpoint) {
            objectName = getManagementNamingStrategy().getObjectNameForEndpoint((Endpoint) managedObject);
        } else if (managedObject instanceof ManagedRoute) {
            ManagedRoute mr = (ManagedRoute) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForRoute(mr.getRoute());
        } else if (managedObject instanceof ManagedErrorHandler) {
            ManagedErrorHandler meh = (ManagedErrorHandler) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForErrorHandler(meh.getRouteContext(), meh.getErrorHandler(), meh.getErrorHandlerBuilder());
        } else if (managedObject instanceof ManagedProcessor) {
            ManagedProcessor mp = (ManagedProcessor) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForProcessor(mp.getContext(), mp.getProcessor(), mp.getDefinition());
        } else if (managedObject instanceof ManagedConsumer) {
            ManagedConsumer ms = (ManagedConsumer) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForConsumer(ms.getContext(), ms.getConsumer());
        } else if (managedObject instanceof ManagedProducer) {
            ManagedProducer ms = (ManagedProducer) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForProducer(ms.getContext(), ms.getProducer());
        } else if (managedObject instanceof ManagedTracer) {
            ManagedTracer mt = (ManagedTracer) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForTracer(mt.getContext(), mt.getTracer());
        } else if (managedObject instanceof ManagedEventNotifier) {
            ManagedEventNotifier men = (ManagedEventNotifier) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForEventNotifier(men.getContext(), men.getEventNotifier());
        } else if (managedObject instanceof ManagedThreadPool) {
            ManagedThreadPool mes = (ManagedThreadPool) managedObject;
            objectName = getManagementNamingStrategy().getObjectNameForThreadPool(mes.getContext(), mes.getThreadPool(), mes.getId(), mes.getSourceId());
        } else if (managedObject instanceof ManagedService) {
            // check for managed service should be last
            ManagedService ms = (ManagedService) managedObject;
            // skip endpoints as they are already managed
            if (ms.getService() instanceof Endpoint) {
                return null;
            }
            objectName = getManagementNamingStrategy().getObjectNameForService(ms.getContext(), ms.getService());
        }

        return nameType.cast(objectName);
    }

    public void unmanageObject(Object managedObject) throws Exception {
        ObjectName objectName = getManagedObjectName(managedObject, null, ObjectName.class);
        unmanageNamedObject(objectName);
    }

    public void unmanageNamedObject(Object name) throws Exception {
        ObjectName objectName = getObjectName(null, name);
        if (objectName != null) {
            getManagementAgent().unregister(objectName);
        }
    }

    public boolean isManaged(Object managableObject, Object name) {
        try {
            ObjectName objectName = getObjectName(managableObject, name);
            if (objectName != null) {
                return getManagementAgent().isRegistered(objectName);
            }
        } catch (Exception e) {
            LOG.warn("Cannot check whether the managed object is registered. This exception will be ignored.", e);
        }
        return false;
    }

    @Override
    public boolean manageProcessor(ProcessorDefinition<?> definition) {
        return true;
    }

    @Override
    public Statistic createStatistic(String name, Object owner, Statistic.UpdateMode updateMode) {
        return new StatisticImpl(updateMode);
    }

    private ObjectName getObjectName(Object managedObject, Object preferedName) throws Exception {
        ObjectName objectName;

        if (preferedName != null && preferedName instanceof String) {
            String customName = (String) preferedName;
            objectName = getManagedObjectName(managedObject, customName, ObjectName.class);
        } else if (preferedName != null && preferedName instanceof ObjectName) {
            objectName = (ObjectName) preferedName;
        } else {
            objectName = getManagedObjectName(managedObject, null, ObjectName.class);
        }
        return objectName;
    }

}
