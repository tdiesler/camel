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
package org.apache.camel.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.processor.RedeliveryPolicy;
import org.apache.camel.util.CamelContextHelper;

/**
 * Represents an XML &lt;redeliveryPolicy/&gt; element
 *
 * @version $Revision$
 */
@XmlRootElement(name = "redeliveryPolicy")
@XmlAccessorType(XmlAccessType.FIELD)
public class RedeliveryPolicyDefinition {
    @XmlAttribute()
    private String ref;
    @XmlAttribute
    private Integer maximumRedeliveries;
    @XmlAttribute
    private Long redeliveryDelay;
    @XmlAttribute
    private Double backOffMultiplier;
    @XmlAttribute
    private Boolean useExponentialBackOff;
    @XmlAttribute
    private Double collisionAvoidanceFactor;
    @XmlAttribute
    private Boolean useCollisionAvoidance;
    @XmlAttribute
    private Long maximumRedeliveryDelay;
    @XmlAttribute
    private LoggingLevel retriesExhaustedLogLevel;
    @XmlAttribute
    private LoggingLevel retryAttemptedLogLevel;
    @XmlAttribute
    private Boolean logStackTrace;
    @XmlAttribute
    private Boolean disableRedelivery;

    public RedeliveryPolicy createRedeliveryPolicy(CamelContext context, RedeliveryPolicy parentPolicy) {
        if (ref != null) {
            // lookup in registry if ref provided
            return CamelContextHelper.mandatoryLookup(context, ref, RedeliveryPolicy.class);
        }

        RedeliveryPolicy answer = parentPolicy.copy();

        // copy across the properties - if they are set
        if (maximumRedeliveries != null) {
            answer.setMaximumRedeliveries(maximumRedeliveries);
        }
        if (redeliveryDelay != null) {
            answer.setDelay(redeliveryDelay);
        }
        if (retriesExhaustedLogLevel != null) {
            answer.setRetriesExhaustedLogLevel(retriesExhaustedLogLevel);
        }
        if (retryAttemptedLogLevel != null) {
            answer.setRetryAttemptedLogLevel(retryAttemptedLogLevel);
        }
        if (backOffMultiplier != null) {
            answer.setBackOffMultiplier(backOffMultiplier);
        }
        if (useExponentialBackOff != null) {
            answer.setUseExponentialBackOff(useExponentialBackOff);
        }
        if (collisionAvoidanceFactor != null) {
            answer.setCollisionAvoidanceFactor(collisionAvoidanceFactor);
        }
        if (useCollisionAvoidance != null) {
            answer.setUseCollisionAvoidance(useCollisionAvoidance);
        }
        if (maximumRedeliveryDelay != null) {
            answer.setMaximumRedeliveryDelay(maximumRedeliveryDelay);
        }
        if (logStackTrace != null) {
            answer.setLogStackTrace(logStackTrace);
        }
        if (disableRedelivery != null && disableRedelivery) {
            answer.setMaximumRedeliveries(0);
        }

        return answer;
    }

    public String toString() {
        return "RedeliveryPolicy[maximumRedeliveries: " + maximumRedeliveries + "]";
    }

    // Fluent API
    //-------------------------------------------------------------------------
    /**
     * Sets the back off multiplier
     *
     * @param backOffMultiplier  the back off multiplier
     * @return the builder
     */
    public RedeliveryPolicyDefinition backOffMultiplier(double backOffMultiplier) {
        setBackOffMultiplier(backOffMultiplier);
        return this;
    }

    /**
     * Sets the collision avoidance percentage
     *
     * @param collisionAvoidancePercent  the percentage
     * @return the builder
     */
    public RedeliveryPolicyDefinition collisionAvoidancePercent(double collisionAvoidancePercent) {
        setCollisionAvoidanceFactor(collisionAvoidancePercent * 0.01d);
        return this;
    }

    /**
     * Sets the collision avoidance factor
     *
     * @param collisionAvoidanceFactor  the factor
     * @return the builder
     */
    public RedeliveryPolicyDefinition collisionAvoidanceFactor(double collisionAvoidanceFactor) {
        setCollisionAvoidanceFactor(collisionAvoidanceFactor);
        return this;
    }

    /**
     * Sets the fixed delay between redeliveries
     *
     * @param delay  delay in millis
     * @return the builder
     */
    public RedeliveryPolicyDefinition redeliveryDelay(long delay) {
        setRedeliveryDelay(delay);
        return this;
    }

    /**
     * Sets the logging level to use when retries has exhausted
     *
     * @param retriesExhaustedLogLevel  the logging level
     * @return the builder
     */
    public RedeliveryPolicyDefinition retriesExhaustedLogLevel(LoggingLevel retriesExhaustedLogLevel) {
        setRetriesExhaustedLogLevel(retriesExhaustedLogLevel);
        return this;
    }    
    
    /**
     * Sets the logging level to use for logging retry attempts
     *
     * @param retryAttemptedLogLevel  the logging level
     * @return the builder
     */
    public RedeliveryPolicyDefinition retryAttemptedLogLevel(LoggingLevel retryAttemptedLogLevel) {
        setRetryAttemptedLogLevel(retryAttemptedLogLevel);
        return this;
    }

    /**
     * Sets wheter stack traces should be logged, can be used to reduce verbose.
     *
     * @param logStackTrace  wheter stack traces should be logged or not
     * @return the builder
     */
    public RedeliveryPolicyDefinition logStackTrace(boolean logStackTrace) {
        setLogStackTrace(logStackTrace);
        return this;
    }


        
    /**
     * Sets the maximum redeliveries
     * <ul>
     *   <li>5 = default value</li>
     *   <li>0 = no redeliveries</li>
     *   <li>-1 = redeliver forever</li>
     * </ul>
     *
     * @param maximumRedeliveries  the value
     * @return the builder
     */
    public RedeliveryPolicyDefinition maximumRedeliveries(int maximumRedeliveries) {
        setMaximumRedeliveries(maximumRedeliveries);
        return this;
    }

    /**
     * Turn on collision avoidance.
     *
     * @return the builder
     */
    public RedeliveryPolicyDefinition useCollisionAvoidance() {
        setUseCollisionAvoidance(Boolean.TRUE);
        return this;
    }

    /**
     * Turn on exponential backk off
     *
     * @return the builder
     */
    public RedeliveryPolicyDefinition useExponentialBackOff() {
        setUseExponentialBackOff(Boolean.TRUE);
        return this;
    }

    /**
     * Sets the maximum delay between redelivery
     *
     * @param maximumRedeliveryDelay  the delay in millis
     * @return the builder
     */
    public RedeliveryPolicyDefinition maximumRedeliveryDelay(long maximumRedeliveryDelay) {
        setMaximumRedeliveryDelay(maximumRedeliveryDelay);
        return this;
    }

    /**
     * Use redelivery policy looked up in the registry
     *
     * @param ref  reference to the redelivery policy to lookup and use
     * @return the builder
     */
    public RedeliveryPolicyDefinition ref(String ref) {
        setRef(ref);
        return this;
    }

    // Properties
    //-------------------------------------------------------------------------

    public Double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(Double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public Double getCollisionAvoidanceFactor() {
        return collisionAvoidanceFactor;
    }

    public void setCollisionAvoidanceFactor(Double collisionAvoidanceFactor) {
        this.collisionAvoidanceFactor = collisionAvoidanceFactor;
    }

    public Long getRedeliveryDelay() {
        return redeliveryDelay;
    }

    public void setRedeliveryDelay(Long delay) {
        this.redeliveryDelay = delay;
    }

    public Integer getMaximumRedeliveries() {
        return maximumRedeliveries;
    }

    public void setMaximumRedeliveries(Integer maximumRedeliveries) {
        this.maximumRedeliveries = maximumRedeliveries;
    }

    public Boolean getUseCollisionAvoidance() {
        return useCollisionAvoidance;
    }

    public void setUseCollisionAvoidance(Boolean useCollisionAvoidance) {
        this.useCollisionAvoidance = useCollisionAvoidance;
    }

    public Boolean getUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    public void setUseExponentialBackOff(Boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    public Long getMaximumRedeliveryDelay() {
        return maximumRedeliveryDelay;
    }

    public void setMaximumRedeliveryDelay(Long maximumRedeliveryDelay) {
        this.maximumRedeliveryDelay = maximumRedeliveryDelay;
    }

    public void setRetriesExhaustedLogLevel(LoggingLevel retriesExhaustedLogLevel) {
        this.retriesExhaustedLogLevel = retriesExhaustedLogLevel;
    }

    public LoggingLevel getRetriesExhaustedLogLevel() {
        return retriesExhaustedLogLevel;
    } 

    public void setRetryAttemptedLogLevel(LoggingLevel retryAttemptedLogLevel) {
        this.retryAttemptedLogLevel = retryAttemptedLogLevel;
    }

    public LoggingLevel getRetryAttemptedLogLevel() {
        return retryAttemptedLogLevel;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Boolean getLogStackTrace() {
        return logStackTrace;
    }

    public void setLogStackTrace(Boolean logStackTrace) {
        this.logStackTrace = logStackTrace;
    }

    public Boolean getDisableRedelivery() {
        return disableRedelivery;
    }

    public void setDisableRedelivery(Boolean disableRedelivery) {
        this.disableRedelivery = disableRedelivery;
    }
}
