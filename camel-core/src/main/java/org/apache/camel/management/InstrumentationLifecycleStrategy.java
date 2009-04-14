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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.spi.InstrumentationAgent;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX agent that registeres Camel lifecycle events in JMX.
 *
 * @version $Revision$
 */
public class InstrumentationLifecycleStrategy implements LifecycleStrategy {
    private static final transient Log LOG = LogFactory.getLog(InstrumentationProcessor.class);

    private static final String MANAGED_RESOURCE_CLASSNAME = "org.springframework.jmx.export.annotation.ManagedResource";
    private InstrumentationAgent agent;
    private CamelNamingStrategy namingStrategy;
    private boolean initialized;

    // A map (Endpoint -> InstrumentationProcessor) to facilitate
    // adding per-route interceptor and registering ManagedRoute MBean
    private final Map<Endpoint, InstrumentationProcessor> interceptorMap =
        new HashMap<Endpoint, InstrumentationProcessor>();

    public InstrumentationLifecycleStrategy() {
        this(new DefaultInstrumentationAgent());
    }

    public InstrumentationLifecycleStrategy(InstrumentationAgent agent) {
        this.agent = agent;
    }
    /**
     * Constructor for camel context that has been started.
     *
     * @param agent    the agent
     * @param context  the camel context
     */
    public InstrumentationLifecycleStrategy(InstrumentationAgent agent, CamelContext context) {
        this.agent = agent;
        onContextStart(context);
    }

    public void onContextStart(CamelContext context) {
        if (context instanceof DefaultCamelContext) {
            try {
                initialized = true;
                DefaultCamelContext dc = (DefaultCamelContext)context;
                // call addService so that context will start and stop the agent
                dc.addService(agent);
                namingStrategy = new CamelNamingStrategy(agent.getMBeanObjectDomainName());
                ManagedService ms = new ManagedService(dc);
                agent.register(ms, getNamingStrategy().getObjectName(dc));
            } catch (Exception e) {
                // must rethrow to allow CamelContext fallback to non JMX agent to allow
                // Camel to continue to run
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }
    }

    /**
     * If the endpoint is an instance of ManagedResource then register it with the
     * mbean server, if it is not then wrap the endpoint in a {@link ManagedEndpoint} and
     * register that with the mbean server.
     * @param endpoint the Endpoint attempted to be added
     */
    @SuppressWarnings("unchecked")
    public void onEndpointAdd(Endpoint endpoint) {
        // the agent hasn't been started
        if (!initialized) {
            return;
        }

        Class annotationClass = resolveManagedAnnotation(endpoint);
        if (annotationClass == null) {
            registerEndpointAsManagedEndpoint(endpoint);
            return;
        }

        Object annotation = endpoint.getClass().getAnnotation(annotationClass);
        if (annotation == null) {
            registerEndpointAsManagedEndpoint(endpoint);
            return;
        }

        attemptToRegisterManagedResource(endpoint, annotation);
    }

    private Class resolveManagedAnnotation(Endpoint endpoint) {

        CamelContext context = endpoint.getCamelContext();

        ClassResolver resolver = context.getClassResolver();
        return resolver.resolveClass(MANAGED_RESOURCE_CLASSNAME);
    }

    private void attemptToRegisterManagedResource(Endpoint endpoint, Object annotation) {
        try {
            Method m = annotation.getClass().getMethod("objectName");

            String objectNameStr = (String) m.invoke(annotation);

            ObjectName objectName = new ObjectName(objectNameStr);
            agent.register(endpoint, objectName);
        } catch (Exception e) {
            LOG.debug("objectName method not present, wrapping endpoint in ManagedEndpoint instead");
            registerEndpointAsManagedEndpoint(endpoint);
        }
    }

    private void registerEndpointAsManagedEndpoint(Endpoint endpoint) {
        try {
            ManagedEndpoint me = new ManagedEndpoint(endpoint);
            agent.register(me, getNamingStrategy().getObjectName(me));
        } catch (JMException e) {
            LOG.warn("Could not register Endpoint MBean for uri: " + endpoint.getEndpointUri(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void onRoutesAdd(Collection<Route> routes) {
        // the agent hasn't been started
        if (!initialized) {
            return;
        }

        for (Route route : routes) {
            try {
                ManagedRoute mr = new ManagedRoute(route);
                // retrieve the per-route intercept for this route
                InstrumentationProcessor interceptor = interceptorMap.get(route.getEndpoint());
                if (interceptor == null) {
                    LOG.warn("Instrumentation processor not found for route endpoint: " + route.getEndpoint());
                } else {
                    interceptor.setCounter(mr);
                }
                agent.register(mr, getNamingStrategy().getObjectName(mr));
            } catch (JMException e) {
                LOG.warn("Could not register Route MBean", e);
            }
        }
    }

    public void onServiceAdd(CamelContext context, Service service) {
        // the agent hasn't been started
        if (!initialized) {
            return;
        }
        if (service instanceof ServiceSupport && service instanceof Consumer) {
            // TODO: add support for non-consumer services?
            try {
                ManagedService ms = new ManagedService((ServiceSupport)service);
                agent.register(ms, getNamingStrategy().getObjectName(context, ms));
            } catch (JMException e) {
                LOG.warn("Could not register Service MBean", e);
            }
        }
    }

    public void onRouteContextCreate(RouteContext routeContext) {
        // the agent hasn't been started
        if (!initialized) {
            return;
        }

        // Create a map (ProcessorType -> PerformanceCounter)
        // to be passed to InstrumentationInterceptStrategy.
        Map<ProcessorDefinition, PerformanceCounter> counterMap =
            new HashMap<ProcessorDefinition, PerformanceCounter>();

        // Each processor in a route will have its own performance counter
        // The performance counter are MBeans that we register with MBeanServer.
        // These performance counter will be embedded
        // to InstrumentationProcessor and wrap the appropriate processor
        // by InstrumentationInterceptStrategy.
        RouteDefinition route = routeContext.getRoute();
        
        for (ProcessorDefinition processor : route.getOutputs()) {
            ObjectName name = null;
            try {
                // get the mbean name
                name = getNamingStrategy().getObjectName(routeContext, processor);

                // register mbean wrapped in the performance counter mbean
                PerformanceCounter pc = new PerformanceCounter();
                agent.register(pc, name);

                // add to map now that it has been registered
                counterMap.put(processor, pc);
            } catch (MalformedObjectNameException e) {
                LOG.warn("Could not create MBean name: " + name, e);
            } catch (JMException e) {
                LOG.warn("Could not register PerformanceCounter MBean: " + name, e);
            }
        }

        // TODO: align this code with InstrumentationLifecycleStrategy
        routeContext.addInterceptStrategy(new InstrumentationInterceptStrategy(counterMap));
        routeContext.setErrorHandlerWrappingStrategy(new InstrumentationErrorHandlerWrappingStrategy(routeContext, counterMap));

        // Add an InstrumentationProcessor at the beginning of each route and
        // set up the interceptorMap for onRoutesAdd() method to register the
        // ManagedRoute MBeans.

        RouteDefinition routeType = routeContext.getRoute();
        if (routeType.getInputs() != null && !routeType.getInputs().isEmpty()) {
            if (routeType.getInputs().size() > 1) {
                LOG.warn("Addding InstrumentationProcessor to first input only.");
            }

            Endpoint endpoint  = routeType.getInputs().get(0).getEndpoint();

            List<ProcessorDefinition> exceptionHandlers = new ArrayList<ProcessorDefinition>();
            List<ProcessorDefinition> outputs = new ArrayList<ProcessorDefinition>();

            // separate out the exception handers in the outputs
            for (ProcessorDefinition output : routeType.getOutputs()) {
                if (output instanceof OnExceptionDefinition) {
                    exceptionHandlers.add(output);
                } else {
                    outputs.add(output);
                }
            }

            // clearing the outputs
            routeType.clearOutput();

            // add exception handlers as top children
            routeType.getOutputs().addAll(exceptionHandlers);

            // add an interceptor to instrument the route
            InstrumentationProcessor processor = new InstrumentationProcessor();
            routeType.intercept(processor);

            // add the output
            for (ProcessorDefinition processorType : outputs) {
                routeType.addOutput(processorType);
            }

            interceptorMap.put(endpoint, processor);
        }

    }

    public CamelNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    public void setNamingStrategy(CamelNamingStrategy strategy) {
        this.namingStrategy = strategy;
    }

    public void setAgent(InstrumentationAgent agent) {
        this.agent = agent;
    }

}
