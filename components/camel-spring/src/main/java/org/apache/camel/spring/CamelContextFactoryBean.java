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
package org.apache.camel.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.Routes;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultLifecycleStrategy;
import org.apache.camel.management.DefaultInstrumentationAgent;
import org.apache.camel.management.InstrumentationLifecycleStrategy;
import org.apache.camel.model.ExceptionType;
import org.apache.camel.model.IdentifiedType;
import org.apache.camel.model.InterceptType;
import org.apache.camel.model.ProceedType;
import org.apache.camel.model.ProcessorType;
import org.apache.camel.model.RouteBuilderRef;
import org.apache.camel.model.RouteContainer;
import org.apache.camel.model.RouteType;
import org.apache.camel.model.config.PropertiesType;
import org.apache.camel.model.dataformat.DataFormatsType;
import org.apache.camel.processor.interceptor.Debugger;
import org.apache.camel.processor.interceptor.Delayer;
import org.apache.camel.processor.interceptor.TraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.PackageScanClassResolver;
import org.apache.camel.spi.Registry;
import org.apache.camel.util.ProcessorTypeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException;

/**
 * A Spring {@link FactoryBean} to create and initialize a
 * {@link SpringCamelContext} and install routes either explicitly configured in
 * Spring XML or found by searching the classpath for Java classes which extend
 * {@link RouteBuilder} using the nested {@link #setPackages(String[])}.
 *
 * @version $Revision$
 */
@XmlRootElement(name = "camelContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class CamelContextFactoryBean extends IdentifiedType implements RouteContainer, FactoryBean, InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener {
    private static final Log LOG = LogFactory.getLog(CamelContextFactoryBean.class);

    @XmlAttribute(required = false)
    private Boolean autowireRouteBuilders = Boolean.TRUE;
    @XmlAttribute(required = false)
    private Boolean trace;
    @XmlAttribute(required = false)
    private Long delay;
    @XmlAttribute(required = false)
    private String errorHandlerRef;
    @XmlAttribute(required = false)
    private Boolean shouldStartContext = Boolean.TRUE;
    @XmlElement(name = "properties", required = false)
    private PropertiesType properties;
    @XmlElement(name = "package", required = false)
    private String[] packages = {};
    @XmlElement(name = "jmxAgent", type = CamelJMXAgentType.class, required = false)
    private CamelJMXAgentType camelJMXAgent;    
    @XmlElements({
        @XmlElement(name = "beanPostProcessor", type = CamelBeanPostProcessor.class, required = false),
        @XmlElement(name = "template", type = CamelTemplateFactoryBean.class, required = false),
        @XmlElement(name = "proxy", type = CamelProxyFactoryType.class, required = false),
        @XmlElement(name = "export", type = CamelServiceExporterType.class, required = false)})
    private List beans;    
    @XmlElement(name = "routeBuilderRef", required = false)
    private List<RouteBuilderRef> builderRefs = new ArrayList<RouteBuilderRef>();
    @XmlElement(name = "endpoint", required = false)
    private List<EndpointFactoryBean> endpoints;
    @XmlElement(name = "dataFormats", required = false)
    private DataFormatsType dataFormats;
    @XmlElement(name = "onException", required = false)
    private List<ExceptionType> exceptionClauses = new ArrayList<ExceptionType>();
    @XmlElement(name = "intercept", required = false)
    private List<InterceptType> intercepts = new ArrayList<InterceptType>();
    @XmlElement(name = "route", required = false)
    private List<RouteType> routes = new ArrayList<RouteType>();    
    @XmlTransient
    private SpringCamelContext context;
    @XmlTransient
    private RouteBuilder routeBuilder;
    @XmlTransient
    private List<Routes> additionalBuilders = new ArrayList<Routes>();
    @XmlTransient
    private ApplicationContext applicationContext;
    @XmlTransient
    private ClassLoader contextClassLoaderOnStart;
    @XmlTransient
    private BeanPostProcessor beanPostProcessor;

    public CamelContextFactoryBean() {
        // Lets keep track of the class loader for when we actually do start things up
        contextClassLoaderOnStart = Thread.currentThread().getContextClassLoader();
    }

    public Object getObject() throws Exception {
        return getContext();
    }

    public Class getObjectType() {
        return SpringCamelContext.class;
    }

    public boolean isSingleton() {
        return true;
    }
    
    public ClassLoader getContextClassLoaderOnStart() {
        return contextClassLoaderOnStart;
    }
    
    public List<Routes> getAdditionalBuilders() {
        return additionalBuilders;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO there should be a neater way to do this!
        if (properties != null) {
            getContext().setProperties(properties.asMap());
        }
        // set the resolvers first
        PackageScanClassResolver packageResolver = getBeanForType(PackageScanClassResolver.class);
        if (packageResolver != null) {
            getContext().setPackageScanClassResolver(packageResolver);
        }
        ClassResolver classResolver = getBeanForType(ClassResolver.class);
        if (classResolver != null) {
            getContext().setClassResolver(classResolver);
        }

        Debugger debugger = getBeanForType(Debugger.class);
        if (debugger != null) {
            getContext().addInterceptStrategy(debugger);
        }

        Tracer tracer = getBeanForType(Tracer.class);
        if (tracer != null) {
            // use formatter if there is a TraceFormatter bean defined
            TraceFormatter formatter = getBeanForType(TraceFormatter.class);
            if (formatter != null) {
                tracer.setFormatter(formatter);
            }
            getContext().addInterceptStrategy(tracer);
        }

        Delayer delayer = getBeanForType(Delayer.class);
        if (delayer != null) {
            getContext().addInterceptStrategy(delayer);
        }

        // set the lifecycle strategy if defined
        LifecycleStrategy lifecycleStrategy = getBeanForType(LifecycleStrategy.class);
        if (lifecycleStrategy != null) {
            getContext().setLifecycleStrategy(lifecycleStrategy);
        }

        // set the strategy if defined
        Registry registry = getBeanForType(Registry.class);
        if (registry != null) {
            getContext().setRegistry(registry);
        }

        // Set the application context and camelContext for the beanPostProcessor
        if (beanPostProcessor != null) {
            if (beanPostProcessor instanceof ApplicationContextAware) {
                ((ApplicationContextAware)beanPostProcessor).setApplicationContext(applicationContext);
            }
            if (beanPostProcessor instanceof CamelBeanPostProcessor) {
                ((CamelBeanPostProcessor)beanPostProcessor).setCamelContext(getContext());
            }
        }

        // setup the intercepts
        for (RouteType route : routes) {

            if (exceptionClauses != null) {
                route.getOutputs().addAll(exceptionClauses);
            }    
            
            for (InterceptType intercept : intercepts) {
                List<ProcessorType<?>> outputs = new ArrayList<ProcessorType<?>>();
                List<ProcessorType<?>> exceptionHandlers = new ArrayList<ProcessorType<?>>();
                for (ProcessorType output : route.getOutputs()) {
                    if (output instanceof ExceptionType) {
                        exceptionHandlers.add(output);
                    } else {
                        outputs.add(output);
                    }
                }

                // clearing the outputs
                route.clearOutput();

                // add exception handlers as top children
                route.getOutputs().addAll(exceptionHandlers);
                
                // add the interceptor but we must do some pre configuration beforehand
                intercept.afterPropertiesSet();
                InterceptType proxy = intercept.createProxy();
                route.addOutput(proxy);
                route.pushBlock(proxy.getProceed());

                // if there is a proceed in the interceptor proxy then we should add
                // the current outputs to out route so we will proceed and continue to route to them
                ProceedType proceed = ProcessorTypeHelper.findFirstTypeInOutputs(proxy.getOutputs(), ProceedType.class);
                if (proceed != null) {
                    proceed.getOutputs().addAll(outputs);
                }
            }

        }

        if (dataFormats != null) {
            getContext().setDataFormats(dataFormats.asMap());
        } 
        
        // lets force any lazy creation
        getContext().addRouteDefinitions(routes);

        if (camelJMXAgent != null && camelJMXAgent.isDisabled()) {
            LOG.debug("JMXAgent disabled");
            getContext().setLifecycleStrategy(new DefaultLifecycleStrategy());
        } else if (camelJMXAgent != null) {
            LOG.debug("JMXAgent enabled");

            if (lifecycleStrategy != null) {
                LOG.warn("lifecycleStrategy will be overriden by InstrumentationLifecycleStrategy");
            }

            DefaultInstrumentationAgent agent = new DefaultInstrumentationAgent();
            agent.setConnectorPort(camelJMXAgent.getConnectorPort());
            agent.setCreateConnector(camelJMXAgent.isCreateConnector());
            agent.setMBeanObjectDomainName(camelJMXAgent.getMbeanObjectDomainName());
            agent.setMBeanServerDefaultDomain(camelJMXAgent.getMbeanServerDefaultDomain());
            agent.setRegistryPort(camelJMXAgent.getRegistryPort());
            agent.setServiceUrlPath(camelJMXAgent.getServiceUrlPath());
            agent.setUsePlatformMBeanServer(camelJMXAgent.isUsePlatformMBeanServer());

            getContext().setLifecycleStrategy(new InstrumentationLifecycleStrategy(agent));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found JAXB created routes: " + getRoutes());
        }
        findRouteBuilders();
        installRoutes();
    }

    @SuppressWarnings("unchecked")
    private <T> T getBeanForType(Class<T> clazz) {
        T bean = null;
        String[] names = getApplicationContext().getBeanNamesForType(clazz, true, true);
        if (names.length == 1) {
            bean = (T) getApplicationContext().getBean(names[0], clazz);
        }
        if (bean == null) {
            ApplicationContext parentContext = getApplicationContext().getParent();
            if (parentContext != null) {
                names = parentContext.getBeanNamesForType(clazz, true, true);
                if (names.length == 1) {
                    bean = (T) parentContext.getBean(names[0], clazz);
                }
            }
        }
        return bean;

    }

    public void destroy() throws Exception {
        getContext().stop();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Publishing spring-event: " + event);
        }

        if (event instanceof ContextRefreshedEvent) {
            // now lets start the CamelContext so that all its possible
            // dependencies are initialized
            try {
                LOG.debug("Starting the context now!");
                getContext().start();
            } catch (Exception e) {
                throw wrapRuntimeCamelException(e);
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public SpringCamelContext getContext() throws Exception {
        if (context == null) {
            context = createContext();
        }
        return context;
    }

    public void setContext(SpringCamelContext context) {
        this.context = context;
    }

    public List<RouteType> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteType> routes) {
        this.routes = routes;
    }

    public List<InterceptType> getIntercepts() {
        return intercepts;
    }

    public void setIntercepts(List<InterceptType> intercepts) {
        this.intercepts = intercepts;
    }

    public RouteBuilder getRouteBuilder() {
        return routeBuilder;
    }

    /**
     * Set a single {@link RouteBuilder} to be used to create the default routes
     * on startup
     */
    public void setRouteBuilder(RouteBuilder routeBuilder) {
        this.routeBuilder = routeBuilder;
    }

    /**
     * Set a collection of {@link RouteBuilder} instances to be used to create
     * the default routes on startup
     */
    public void setRouteBuilders(RouteBuilder[] builders) {
        for (RouteBuilder builder : builders) {
            additionalBuilders.add(builder);
        }
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalArgumentException("No applicationContext has been injected!");
        }
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public PropertiesType getProperties() {
        return properties;
    }
    
    public void setProperties(PropertiesType properties) {        
        this.properties = properties;
    }

    public String[] getPackages() {
        return packages;
    }

    /**
     * Sets the package names to be recursively searched for Java classes which
     * extend {@link RouteBuilder} to be auto-wired up to the
     * {@link SpringCamelContext} as a route. Note that classes are excluded if
     * they are specifically configured in the spring.xml
     *
     * @param packages the package names which are recursively searched
     */
    public void setPackages(String[] packages) {
        this.packages = packages;
    }

    public void setBeanPostProcessor(BeanPostProcessor postProcessor) {
        this.beanPostProcessor = postProcessor;
    }

    public BeanPostProcessor getBeanPostProcessor() {
        return beanPostProcessor;
    }

    public void setCamelJMXAgent(CamelJMXAgentType agent) {
        camelJMXAgent = agent;
    }

    public Boolean getTrace() {
        return trace;
    }

    public void setTrace(Boolean trace) {
        this.trace = trace;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public CamelJMXAgentType getCamelJMXAgent() {
        return camelJMXAgent;
    }

    public List<RouteBuilderRef> getBuilderRefs() {
        return builderRefs;
    }

    public void setBuilderRefs(List<RouteBuilderRef> builderRefs) {
        this.builderRefs = builderRefs;
    }

    /**
     * If enabled this will force all {@link RouteBuilder} classes configured in the Spring
     * {@link ApplicationContext} to be registered automatically with this CamelContext.
     */
    public void setAutowireRouteBuilders(Boolean autowireRouteBuilders) {
        this.autowireRouteBuilders = autowireRouteBuilders;
    }

    public String getErrorHandlerRef() {
        return errorHandlerRef;
    }

    /**
     * Sets the name of the error handler object used to default the error handling strategy
     *
     * @param errorHandlerRef the Spring bean ref of the error handler
     */
    public void setErrorHandlerRef(String errorHandlerRef) {
        this.errorHandlerRef = errorHandlerRef;
    }

    public Boolean getShouldStartContext() {
        return shouldStartContext;
    }

    public void setShouldStartContext(Boolean shouldStartContext) {
        this.shouldStartContext = shouldStartContext;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * Create the context
     */
    protected SpringCamelContext createContext() {
        SpringCamelContext ctx = new SpringCamelContext(getApplicationContext());
        ctx.setName(getId());
        if (trace != null) {
            ctx.setTrace(trace);
        }
        if (delay != null) {
            ctx.setDelay(delay);
        }
        if (errorHandlerRef != null) {
            ErrorHandlerBuilder errorHandlerBuilder = (ErrorHandlerBuilder) getApplicationContext().getBean(errorHandlerRef, ErrorHandlerBuilder.class);
            if (errorHandlerBuilder == null) {
                throw new IllegalArgumentException("Could not find bean: " + errorHandlerRef);
            }
            ctx.setErrorHandlerBuilder(errorHandlerBuilder);
        }

        if (shouldStartContext != null) {
            ctx.setShouldStartContext(shouldStartContext);
        }

        return ctx;
    }

    /**
     * Strategy to install all available routes into the context
     */
    protected void installRoutes() throws Exception {
        if (autowireRouteBuilders != null && autowireRouteBuilders) {
            Map builders = getApplicationContext().getBeansOfType(RouteBuilder.class, true, true);
            if (builders != null) {
                for (Object builder : builders.values()) {
                    getContext().addRoutes((RouteBuilder) builder);
                }
            }
        }
        for (Routes routeBuilder : additionalBuilders) {
            getContext().addRoutes(routeBuilder);
        }
        if (routeBuilder != null) {
            getContext().addRoutes(routeBuilder);
        }

        // lets add route builders added from references
        if (builderRefs != null) {
            for (RouteBuilderRef builderRef : builderRefs) {
                RouteBuilder builder = builderRef.createRouteBuilder(getContext());
                getContext().addRoutes(builder);
            }
        }
    }

    /**
     * Strategy method to try find {@link RouteBuilder} instances on the
     * classpath
     */
    protected void findRouteBuilders() throws Exception, InstantiationException {
        if (getPackages() != null && getPackages().length > 0) {
            RouteBuilderFinder finder = new RouteBuilderFinder(getContext(), getPackages(), getContextClassLoaderOnStart(),
                    getBeanPostProcessor(), getContext().getPackageScanClassResolver());
            finder.appendBuilders(getAdditionalBuilders());
        }
    }
    
    public void setDataFormats(DataFormatsType dataFormats) {
        this.dataFormats = dataFormats;
    }

    public DataFormatsType getDataFormats() {
        return dataFormats;
    }

    public void setExceptionClauses(List<ExceptionType> exceptionClauses) {
        this.exceptionClauses = exceptionClauses;
    }

    public List<ExceptionType> getExceptionClauses() {
        return exceptionClauses;
    }
}
