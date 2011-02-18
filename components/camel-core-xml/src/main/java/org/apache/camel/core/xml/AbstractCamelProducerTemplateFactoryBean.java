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
package org.apache.camel.core.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.util.ServiceHelper;

/**
 * A factory for creating a new {@link org.apache.camel.ProducerTemplate}
 * instance with a minimum of XML
 *
 * @version 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractCamelProducerTemplateFactoryBean extends AbstractCamelFactoryBean<ProducerTemplate> {
    @XmlTransient
    private ProducerTemplate template;
    @XmlAttribute(required = false)
    private String defaultEndpoint;
    @XmlAttribute
    private Integer maximumCacheSize;

    public ProducerTemplate getObject() throws Exception {
        CamelContext context = getCamelContext();
        if (defaultEndpoint != null) {
            Endpoint endpoint = context.getEndpoint(defaultEndpoint);
            if (endpoint == null) {
                throw new IllegalArgumentException("No endpoint found for URI: " + defaultEndpoint);
            } else {
                template = new DefaultProducerTemplate(context, endpoint);
            }
        } else {
            template = new DefaultProducerTemplate(context);
        }

        // set custom cache size if provided
        if (maximumCacheSize != null) {
            template.setMaximumCacheSize(maximumCacheSize);
        }

        // must start it so its ready to use
        ServiceHelper.startService(template);
        return template;
    }

    public Class<DefaultProducerTemplate> getObjectType() {
        return DefaultProducerTemplate.class;
    }

    public void destroy() throws Exception {
        ServiceHelper.stopService(template);
    }

    // Properties
    // -------------------------------------------------------------------------

    public String getDefaultEndpoint() {
        return defaultEndpoint;
    }

    /**
     * Sets the default endpoint URI used by default for sending message exchanges
     */
    public void setDefaultEndpoint(String defaultEndpoint) {
        this.defaultEndpoint = defaultEndpoint;
    }

    public Integer getMaximumCacheSize() {
        return maximumCacheSize;
    }

    public void setMaximumCacheSize(Integer maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
    }

}
