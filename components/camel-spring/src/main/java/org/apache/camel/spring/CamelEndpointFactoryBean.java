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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.model.IdentifiedType;
import org.apache.camel.spring.util.CamelContextResolverHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * A {@link FactoryBean} which instantiates {@link Endpoint} objects
 *
 * @version $Revision$
 */
@XmlRootElement(name = "endpoint")
@XmlAccessorType(XmlAccessType.FIELD)
public class CamelEndpointFactoryBean extends IdentifiedType implements FactoryBean, CamelContextAware, ApplicationContextAware {
    @XmlAttribute
    private String uri;
    @XmlAttribute
    private String camelContextId;
    @XmlTransient
    private CamelContext context;    
    @XmlTransient
    private Endpoint endpoint;
    @XmlTransient
    private ApplicationContext applicationContext;

    public Object getObject() throws Exception {
        if (endpoint == null) {
            endpoint = createEndpoint();
        }
        return endpoint;
    }

    public Class getObjectType() {
        return Endpoint.class;
    }
    
    public boolean isSingleton() {
        return true;
    }
    
    public CamelContext getCamelContext() {
        return context;
    }
    

    /**
     * Sets the context to use to resolve endpoints
     *
     * @param context the context used to resolve endpoints
     */
    public void setCamelContext(CamelContext context) {
        this.context = context;
    }

    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI to use to resolve the endpoint
     *
     * @param uri the URI used to set the endpoint
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    protected Endpoint createEndpoint() {
        if (context == null && camelContextId != null) {
            context = CamelContextResolverHelper.getCamelContextWithId(applicationContext, camelContextId);
        }
        notNull(context, "context");
        notNull(uri, "uri");
        
        Endpoint endpoint = context.getEndpoint(uri);
        if (endpoint == null) {
            throw new NoSuchEndpointException(uri);
        }
        return endpoint;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setCamelContextId(String camelContextId) {
        this.camelContextId = camelContextId;
    }
   
}
