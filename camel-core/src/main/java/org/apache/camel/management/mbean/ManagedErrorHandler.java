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
package org.apache.camel.management.mbean;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.processor.RedeliveryErrorHandler;
import org.apache.camel.spi.RouteContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @version $Revision$
 */
@ManagedResource(description = "Managed ErrorHandler")
public class ManagedErrorHandler {

    private RouteContext routeContext;
    private Processor errorHandler;
    private ErrorHandlerBuilder errorHandlerBuilder;

    public ManagedErrorHandler(RouteContext routeContext, Processor errorHandler, ErrorHandlerBuilder builder) {
        this.routeContext = routeContext;
        this.errorHandler = errorHandler;
        this.errorHandlerBuilder = builder;
    }

    public RouteContext getRouteContext() {
        return routeContext;
    }

    public Processor getErrorHandler() {
        return errorHandler;
    }

    public ErrorHandlerBuilder getErrorHandlerBuilder() {
        return errorHandlerBuilder;
    }

    @ManagedAttribute(description = "Camel id")
    public String getCamelId() {
        return routeContext.getCamelContext().getName();
    }

    @ManagedAttribute(description = "Does the error handler support redelivery")
    public boolean isSupportRedelivery() {
        return errorHandler instanceof RedeliveryErrorHandler;
    }

    @ManagedAttribute(description = "RedeliveryPolicy for maximum redeliveries")
    public Integer getMaximumRedeliveries() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getMaximumRedeliveries();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for maximum redeliveries")
    public void setMaximumRedeliveries(Integer maximum) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setMaximumRedeliveries(maximum);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for maximum redelivery delay")
    public Long getMaximumRedeliveryDelay() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getMaximumRedeliveryDelay();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for maximum redelivery delay")
    public void setMaximumRedeliveryDelay(Long delay) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setMaximumRedeliveryDelay(delay);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for redelivery delay")
    public Long getRedeliveryDelay() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getRedeliverDelay();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for redelivery delay")
    public void setRedeliveryDelay(Long delay) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setRedeliverDelay(delay);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for backoff multipler")
    public Double getBackOffMultiplier() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getBackOffMultiplier();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for backoff multipler")
    public void setBackOffMultiplier(Double multiplier) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setBackOffMultiplier(multiplier);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for collision avoidance factor")
    public Double getCollisionAvoidanceFactor() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getCollisionAvoidanceFactor();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for collision avoidance factor")
    public void setCollisionAvoidanceFactor(Double factor) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setCollisionAvoidanceFactor(factor);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for collision avoidance percent")
    public Double getCollisionAvoidancePercent() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return (double) redelivery.getRedeliveryPolicy().getCollisionAvoidancePercent();  
    }

    @ManagedAttribute(description = "RedeliveryPolicy for collision avoidance percent")
    public void setCollisionAvoidancePercent(Double percent) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setCollisionAvoidancePercent(percent);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for delay pattern")
    public String getDelayPattern() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getDelayPattern();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for delay pattern")
    public void setDelayPattern(String pattern) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setDelayPattern(pattern);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging level when retries exhausted")
    public String getRetriesExhaustedLogLevel() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getRetriesExhaustedLogLevel().name();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging level when retries exhausted")
    public void setRetriesExhaustedLogLevel(String level) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setRetriesExhaustedLogLevel(LoggingLevel.valueOf(level));
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging level when attempting retry")
    public String getRetryAttemptedLogLevel() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().getRetryAttemptedLogLevel().name();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging level when attempting retry")
    public void setRetryAttemptedLogLevel(String level) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setRetryAttemptedLogLevel(LoggingLevel.valueOf(level));
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging stack traces")
    public Boolean getLogStackTrace() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().isLogStackTrace();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for logging stack traces")
    public void setLogStackTrace(Boolean log) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setLogStackTrace(log);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for using collision avoidance")
    public Boolean getUseCollisionAvoidance() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().isUseCollisionAvoidance();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for using collision avoidance")
    public void setUseCollisionAvoidance(Boolean avoidance) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setUseCollisionAvoidance(avoidance);
    }

    @ManagedAttribute(description = "RedeliveryPolicy for using exponential backoff")
    public Boolean getUseExponentialBackOff() {
        if (!isSupportRedelivery()) {
            return null;
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        return redelivery.getRedeliveryPolicy().isUseExponentialBackOff();
    }

    @ManagedAttribute(description = "RedeliveryPolicy for using exponential backoff")
    public void setUseExponentialBackOff(Boolean backoff) {
        if (!isSupportRedelivery()) {
            throw new IllegalArgumentException("This error handler does not support redelivery");
        }

        RedeliveryErrorHandler redelivery = (RedeliveryErrorHandler) errorHandler;
        redelivery.getRedeliveryPolicy().setUseExponentialBackOff(backoff);
    }

}
