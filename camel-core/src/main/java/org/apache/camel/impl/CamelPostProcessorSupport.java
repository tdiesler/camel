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
package org.apache.camel.impl;

import org.apache.camel.*;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.camel.util.CamelContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Method;

/**
 * A helper class for Camel based injector or post processing hooks which can be reused by
 * both the <a href="http://activemq.apache.org/camel/spring.html">Spring</a>
 * and <a href="http://activemq.apache.org/camel/guice.html">Guice</a> support.
 *
 * @version $Revision: 1.1 $
 */
public class CamelPostProcessorSupport implements CamelContextAware {
    private static final transient Log LOG = LogFactory.getLog(CamelPostProcessorSupport.class);

    @XmlTransient
    private CamelContext camelContext;

    public CamelPostProcessorSupport(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void consumerInjection(Method method, Object bean) {
        MessageDriven annotation = method.getAnnotation(MessageDriven.class);
        if (annotation != null) {
            LOG.info("Creating a consumer for: " + annotation);
            subscribeMethod(method, bean, annotation.uri(), annotation.name());
        }

        Consume consume = method.getAnnotation(Consume.class);
        if (consume != null) {
            LOG.info("Creating a consumer for: " + consume);
            subscribeMethod(method, bean, consume.uri(), consume.ref());
        }
    }

    protected void subscribeMethod(Method method, Object bean, String endpointUri, String endpointName) {
        // lets bind this method to a listener
        String injectionPointName = method.getName();
        Endpoint endpoint = getEndpointInjection(endpointUri, endpointName, injectionPointName);
        if (endpoint != null) {
            try {
                Processor processor = createConsumerProcessor(bean, method, endpoint);
                LOG.info("Created processor: " + processor);
                Consumer consumer = endpoint.createConsumer(processor);
                startService(consumer);
            } catch (Exception e) {
                LOG.warn(e);
                throw org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException(e);
            }
        }
    }

    public void startService(Service service) throws Exception {
        if (camelContext instanceof DefaultCamelContext) {
            DefaultCamelContext defaultCamelContext = (DefaultCamelContext) camelContext;
            defaultCamelContext.addService(service);
        }
        else {
            service.start();
        }
    }

    /**
     * Create a processor which invokes the given method when an incoming
     * message exchange is received
     */
    protected Processor createConsumerProcessor(final Object pojo, final Method method, final Endpoint endpoint) {
        BeanProcessor answer = new BeanProcessor(pojo, getCamelContext());
        answer.setMethodObject(method);
        return answer;
    }

    protected Endpoint getEndpointInjection(String uri, String name, String injectionPointName) {
        return CamelContextHelper.getEndpointInjection(camelContext, uri, name, injectionPointName);
    }

    /**
     * Creates the object to be injected for an {@link org.apache.camel.EndpointInject} or {@link org.apache.camel.Produce} injection point
     */
    public Object getInjectionValue(Class<?> type, String endpointUri, String endpointRef, String injectionPointName) {
        Endpoint endpoint = getEndpointInjection(endpointUri, endpointRef, injectionPointName);
        if (endpoint != null) {
            if (type.isInstance(endpoint)) {
                return endpoint;
            } else if (type.isAssignableFrom(Producer.class)) {
                return createInjectionProducer(endpoint);
            } else if (type.isAssignableFrom(DefaultProducerTemplate.class)) {
                return new DefaultProducerTemplate(getCamelContext(), endpoint);
            } else if (type.isAssignableFrom(PollingConsumer.class)) {
                return createInjectionPollingConsumer(endpoint);
            } else if (type.isInterface()) {
                // lets create a proxy
                try {
                    return ProxyHelper.createProxy(endpoint, type);
                } catch (Exception e) {
                    throw createProxyInstantiationRuntimeException(type, endpoint, e);
                }
            } else {
                throw new IllegalArgumentException("Invalid type: " + type.getName() + " which cannot be injected via @EndpointInject for " + endpoint);
            }
        }
        return null;
    }

    protected RuntimeException createProxyInstantiationRuntimeException(Class<?> type, Endpoint endpoint, Exception e) {
        return new ProxyInstantiationException(type, endpoint, e);
    }

    /**
     * Factory method to create a started {@link org.apache.camel.PollingConsumer} to be injected
     * into a POJO
     */
    protected PollingConsumer createInjectionPollingConsumer(Endpoint endpoint) {
        try {
            PollingConsumer pollingConsumer = endpoint.createPollingConsumer();
            startService(pollingConsumer);
            return pollingConsumer;
        } catch (Exception e) {
            throw org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException(e);
        }
    }

    /**
     * A Factory method to create a started {@link org.apache.camel.Producer} to be injected into
     * a POJO
     */
    protected Producer createInjectionProducer(Endpoint endpoint) {
        try {
            Producer producer = endpoint.createProducer();
            startService(producer);
            return producer;
        } catch (Exception e) {
            throw org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
}
