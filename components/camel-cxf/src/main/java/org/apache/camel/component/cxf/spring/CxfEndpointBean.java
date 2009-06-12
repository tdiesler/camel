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

import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;

import org.springframework.beans.factory.DisposableBean;

/**
 *
 */
public class CxfEndpointBean extends AbstractWSDLBasedEndpointFactory
    implements DisposableBean {
    
    private List handlers;

    public CxfEndpointBean() {
        this(new ReflectionServiceFactoryBean());
    }
    
    public CxfEndpointBean(ReflectionServiceFactoryBean factory) {
        setServiceFactory(factory);
    }
    
    public List getHandlers() {
        return handlers;
    }
    
    public void setHandlers(List handlers) {
        this.handlers = handlers;
    }

    public void destroy() throws Exception {
        // Clean up the BusFactory's defaultBus
        BusFactory.setDefaultBus(null);
        BusFactory.setThreadDefaultBus(null);
        
    }
}
