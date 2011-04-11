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
package org.apache.camel.blueprint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.builder.LoggingErrorHandlerBuilder;
import org.apache.camel.core.xml.AbstractCamelFactoryBean;
import org.apache.camel.model.RedeliveryPolicyDefinition;
import org.apache.camel.processor.RedeliveryPolicy;
import org.apache.camel.util.CamelContextHelper;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.NoSuchComponentException;

@XmlRootElement(name = "errorHandler")
@XmlAccessorType(XmlAccessType.FIELD)
public class CamelErrorHandlerFactoryBean extends AbstractCamelFactoryBean<ErrorHandlerBuilder> {

    @XmlAttribute
    private ErrorHandlerType type = ErrorHandlerType.DefaultErrorHandler;
    @XmlAttribute
    private String deadLetterUri;
    @XmlAttribute
    private LoggingLevel level = LoggingLevel.ERROR;
    @XmlAttribute
    private String logName;
    @XmlAttribute
    private Boolean useOriginalMessage;
    @XmlAttribute
    private String onRedeliveryRef;
    @XmlAttribute
    private String retryWhileRef;
    @XmlAttribute
    private String executorServiceRef;
    @XmlAttribute
    private String redeliveryPolicyRef;
    @XmlElement
    private RedeliveryPolicyDefinition redeliveryPolicy;
    @XmlTransient
    private BlueprintContainer blueprintContainer;

    @Override
    public ErrorHandlerBuilder getObject() throws Exception {
        ErrorHandlerBuilder errorHandler = getObjectType().newInstance();
        if (errorHandler instanceof DefaultErrorHandlerBuilder) {
            DefaultErrorHandlerBuilder handler = (DefaultErrorHandlerBuilder) errorHandler;
            if (deadLetterUri != null) {
                handler.setDeadLetterUri(deadLetterUri);
            }
            if (useOriginalMessage != null) {
                handler.setUseOriginalMessage(useOriginalMessage);
            }
            if (redeliveryPolicy != null) {
                handler.setRedeliveryPolicy(redeliveryPolicy.createRedeliveryPolicy(getCamelContext(), null));
            }
            if (redeliveryPolicyRef != null) {
                // lookup redelivery
                RedeliveryPolicy policy = CamelContextHelper.mandatoryLookup(getCamelContext(), redeliveryPolicyRef, RedeliveryPolicy.class);
                handler.setRedeliveryPolicy(policy);
            }
            if (onRedeliveryRef != null) {
                handler.setOnRedelivery(lookup(onRedeliveryRef, Processor.class));
            }
            if (retryWhileRef != null) {
                handler.setRetryWhileRef(retryWhileRef);
            }
            if (executorServiceRef != null) {
                handler.setExecutorServiceRef(executorServiceRef);
            }
        } else if (errorHandler instanceof LoggingErrorHandlerBuilder) {
            LoggingErrorHandlerBuilder handler = (LoggingErrorHandlerBuilder) errorHandler;
            if (level != null) {
                handler.setLevel(level);
            }
            if (logName != null) {
                handler.setLogName(logName);
            }
        }
        return errorHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ErrorHandlerBuilder> getObjectType() {
        return (Class<ErrorHandlerBuilder>) type.getTypeAsClass();
    }

    public void setBlueprintContainer(BlueprintContainer blueprintContainer) {
        this.blueprintContainer = blueprintContainer;
    }

    protected CamelContext getCamelContextWithId(String camelContextId) {
        if (blueprintContainer != null) {
            return (CamelContext) blueprintContainer.getComponentInstance(camelContextId);
        }
        return null;
    }

    protected <T> T lookup(String name, Class<T> type) {
        try {
            return type.cast(blueprintContainer.getComponentInstance(name));
        } catch (NoSuchComponentException e) {
            return null;
        }
    }

}
