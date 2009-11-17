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

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Processor;
import org.apache.camel.processor.RoutingSlip;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents an XML &lt;routingSlip/&gt; element
 */
@XmlRootElement(name = "routingSlip")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoutingSlipDefinition extends ProcessorDefinition<ProcessorDefinition<?>> {
    public static final String DEFAULT_DELIMITER = ",";

    @XmlAttribute
    private String headerName;
    @XmlAttribute
    private String uriDelimiter;

    public RoutingSlipDefinition() {
        this(null, DEFAULT_DELIMITER);
    }

    public RoutingSlipDefinition(String headerName) {
        this(headerName, DEFAULT_DELIMITER);
    }

    public RoutingSlipDefinition(String headerName, String uriDelimiter) {
        setHeaderName(headerName);
        setUriDelimiter(uriDelimiter);
    }

    @Override
    public String toString() {
        return "RoutingSlip[headerName=" + getHeaderName() + ", uriDelimiter=" + getUriDelimiter() + "]";
    }

    @Override
    public String getShortName() {
        return "routingSlip";
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        ObjectHelper.notEmpty(getHeaderName(), "headerName", this);
        ObjectHelper.notEmpty(getUriDelimiter(), "uriDelimiter", this);
        return new RoutingSlip(getHeaderName(), getUriDelimiter());
    }

    @Override
    public List<ProcessorDefinition<?>> getOutputs() {
        return Collections.emptyList();
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public void setUriDelimiter(String uriDelimiter) {
        this.uriDelimiter = uriDelimiter;
    }

    public String getUriDelimiter() {
        return uriDelimiter;
    }
}
