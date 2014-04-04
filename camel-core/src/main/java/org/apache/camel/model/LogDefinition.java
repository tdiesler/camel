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

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.processor.LogProcessor;
import org.apache.camel.processor.aggregate.AggregateProcessor;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.CamelLogger;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an XML &lt;log/&gt; element
 *
 * @version 
 */
@XmlRootElement(name = "log")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogDefinition extends NoOutputDefinition<LogDefinition> {
    @XmlTransient
    private static final Logger LOG = LoggerFactory.getLogger(LogDefinition.class);
    @XmlAttribute(required = true)
    private String message;
    @XmlAttribute
    private LoggingLevel loggingLevel;
    @XmlAttribute
    private String logName;
    @XmlAttribute
    private String marker;
    @XmlAttribute
    private String loggerRef;
    @XmlTransient
    private Logger logger;

    public LogDefinition() {
    }

    public LogDefinition(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Log[" + message + "]";
    }
    
    @Override
    public String getLabel() {
        return "log";
    }

    @Override
    public String getShortName() {
        return "log";
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        ObjectHelper.notEmpty(message, "message", this);

        // use simple language for the message string to give it more power
        Expression exp = routeContext.getCamelContext().resolveLanguage("simple").createExpression(message);

        // get logger explicitely set in the definition
        Logger logger = this.getLogger();

        // get logger which may be set in XML definition
        if (logger == null && ObjectHelper.isNotEmpty(loggerRef)) {
            logger = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), loggerRef, Logger.class);
        }

        if (logger == null) {
            // first - try to lookup single instance in the registry, just like LogComponent
            Map<String, Logger> availableLoggers = routeContext.lookupByType(Logger.class);
            if (availableLoggers.size() == 1) {
                logger = availableLoggers.values().iterator().next();
            } else if (availableLoggers.size() > 1) {
                // we should log about this somewhere...
                LOG.info("More than one {} instance found in the registry. Falling back to create logger by name.", Logger.class.getName());
            }
        }

        if (logger == null) {
            String name = getLogName();
            if (name == null) {
                name = routeContext.getRoute().getId();
                LOG.info("The LogName is null. Falling back to create logger by using the route id {}.", name);
            }
            logger = LoggerFactory.getLogger(name);
        }

        // should be INFO by default
        LoggingLevel level = getLoggingLevel() != null ? getLoggingLevel() : LoggingLevel.INFO;
        CamelLogger camelLogger = new CamelLogger(logger, level, getMarker());

        return new LogProcessor(exp, camelLogger);
    }

    @Override
    public void addOutput(ProcessorDefinition<?> output) {
        // add outputs on parent as this log does not support outputs
        getParent().addOutput(output);
    }

    public LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(LoggingLevel loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getLoggerRef() {
        return loggerRef;
    }

    public void setLoggerRef(String loggerRef) {
        this.loggerRef = loggerRef;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}