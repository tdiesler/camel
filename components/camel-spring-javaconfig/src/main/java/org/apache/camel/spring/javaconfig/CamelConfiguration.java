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
package org.apache.camel.spring.javaconfig;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.CamelBeanPostProcessor;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * A useful base class for writing
 * <a href="http://www.springsource.org/javaconfig">Spring JavaConfig</a>
 * configurations for working with Camel
 *
 * @version $Revision$
 */
@Configuration
public abstract class CamelConfiguration implements BeanFactoryAware, ApplicationContextAware {
    
    private BeanFactory beanFactory;

    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private ApplicationContext applicationContext;

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof AutowireCapableBeanFactory) {
            autowireCapableBeanFactory = (AutowireCapableBeanFactory) beanFactory;
        }
    }

    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public void setApplicationContext(ApplicationContext ac) {
        this.applicationContext = ac;
    }

    protected ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    public <T> T getBean(String beanName, Class<T> type) {
        return beanFactory.getBean(beanName, type);
    }
   

    /**
     * Invoke callbacks on the object, as though it were configured in the factory. If appropriate,
     * the object may be wrapped before being returned. For this reason, it is recommended to always
     * respect the return value when using this method.
     *
     * @param   object  object to configure
     *
     * @return  either the original object or a wrapped one after callbacks called on it.
     */
    protected <T> T getConfigured(T object) {
        if (this.autowireCapableBeanFactory == null) {
            throw new UnsupportedOperationException(
                "Cannot configure object - not running in an AutowireCapableBeanFactory");
        }

        @SuppressWarnings("unchecked") // See SPR-4955
        T configuredObject = (T) autowireCapableBeanFactory.initializeBean(object, null);

        // this block copied from ApplicationContextAwareProcessor.  See SJC-149.
        if (this.applicationContext != null) {
            if (configuredObject instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware) configuredObject).setResourceLoader(this.applicationContext);
            }

            if (configuredObject instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) configuredObject).setApplicationEventPublisher(this.applicationContext);
            }

            if (configuredObject instanceof MessageSourceAware) {
                ((MessageSourceAware) configuredObject).setMessageSource(this.applicationContext);
            }

            if (configuredObject instanceof ApplicationContextAware) {
                ((ApplicationContextAware) configuredObject).setApplicationContext(this.applicationContext);
            }
        }

        return configuredObject;
    }

    @Bean
    public CamelBeanPostProcessor camelBeanPostProcessor() throws Exception {
        CamelBeanPostProcessor answer = new CamelBeanPostProcessor();

        CamelContext camelContext = getBean(CamelContext.class);
        // lets lookup a bean
        answer.setCamelContext(camelContext);        
        return answer;
    }

    /**
     * Returns the CamelContext
     */
    @Bean
    public CamelContext camelContext() throws Exception {
        CamelContext camelContext = createCamelContext();
        setupCamelContext(camelContext);
        List<RouteBuilder> routes = routes();
        for (RoutesBuilder route : routes) {
            camelContext.addRoutes(route);
        }        
        return camelContext;
    }
    
    // Can register the camel component, language here
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        
    }
    
    protected CamelContext createCamelContext() throws Exception {
        return new SpringCamelContext(getApplicationContext());
    }

    /**
     * Returns the list of routes to use in this configuration
     */
    public abstract List<RouteBuilder> routes();

}
