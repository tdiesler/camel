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
package org.apache.camel.component.cxf.spring;

import java.util.List;

import org.apache.camel.component.cxf.jaxrs.BeanIdAware;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.version.Version;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringJAXRSClientFactoryBean extends JAXRSClientFactoryBean
    implements ApplicationContextAware, BeanIdAware {
    private String beanId;
    private int loggingSizeLimit;
    private LoggingFeature loggingFeature;

    public SpringJAXRSClientFactoryBean() {
        super();
    }
    
    public boolean isLoggingFeatureEnabled() {
        return loggingFeature != null;
    }

    public void setLoggingFeatureEnabled(boolean loggingFeatureEnabled) {
        if (loggingFeature != null) {
            getFeatures().remove(loggingFeature);
            loggingFeature = null;
        }
        if (loggingFeatureEnabled) {
            if (getLoggingSizeLimit() > 0) {
                loggingFeature = new LoggingFeature(getLoggingSizeLimit());
            } else {
                loggingFeature = new LoggingFeature();
            }
            getFeatures().add(loggingFeature);
        }
        
    }

    public int getLoggingSizeLimit() {
        return loggingSizeLimit;
    }

    public void setLoggingSizeLimit(int loggingSizeLimit) {
        this.loggingSizeLimit = loggingSizeLimit;
        if (loggingFeature != null) {
            getFeatures().remove(loggingFeature);
            if (loggingSizeLimit > 0) {
                loggingFeature = new LoggingFeature(loggingSizeLimit);
            } else {
                loggingFeature = new LoggingFeature();
            }
            getFeatures().add(loggingFeature);
        }
    }

    
    
    @SuppressWarnings("deprecation")
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        if (bus == null) {
            if (Version.getCurrentVersion().startsWith("2.3")) {
                // Don't relate on the DefaultBus
                BusFactory factory = new SpringBusFactory(ctx);
                bus = factory.createBus();    
                BusWiringBeanFactoryPostProcessor.updateBusReferencesInContext(bus, ctx);
                setBus(bus);
            } else {
                setBus(BusWiringBeanFactoryPostProcessor.addDefaultBus(ctx));
            }
        }
    }

    public String getBeanId() {            
        return beanId;
    }

    public void setBeanId(String id) {            
        beanId = id;            
    }
    
    // add this mothod for testing
    List<String> getSchemaLocations() {
        return schemaLocations;
    }
}