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
import org.apache.camel.processor.RollbackProcessor;
import org.apache.camel.spi.RouteContext;

/**
 * Represents an XML &lt;rollback/&gt; element
 */
@XmlRootElement(name = "rollback")
@XmlAccessorType(XmlAccessType.FIELD)
public class RollbackDefinition extends ProcessorDefinition<RollbackDefinition> {

    @XmlAttribute
    private Boolean markRollbackOnly;
    @XmlAttribute
    private Boolean markRollbackOnlyLast;
    @XmlAttribute
    private String message;

    public RollbackDefinition() {
    }

    public RollbackDefinition(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String getShortName() {
        return "rollback";
    }

    @Override
    public String toString() {
        if (message != null) {
            return "Rollback[" + message + "]";
        } else {
            return "Rollback";
        }
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) {
        // validate that only either mark rollbacks is chosen and not both
        if (markRollbackOnly != null && markRollbackOnly.booleanValue()
            && markRollbackOnlyLast != null && markRollbackOnlyLast.booleanValue()) {
            throw new IllegalArgumentException("Only either one of markRollbackOnly and markRollbackOnlyLast is possible to select as true");
        }

        RollbackProcessor answer = new RollbackProcessor(message);
        answer.setMarkRollbackOnly(markRollbackOnly != null ? markRollbackOnly : false);
        answer.setMarkRollbackOnlyLast(markRollbackOnlyLast != null ? markRollbackOnlyLast : false);
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessorDefinition> getOutputs() {
        return Collections.EMPTY_LIST;
    }

    public Boolean isMarkRollbackOnly() {
        return markRollbackOnly;
    }

    public void setMarkRollbackOnly(Boolean markRollbackOnly) {
        this.markRollbackOnly = markRollbackOnly;
    }

    public Boolean isMarkRollbackOnlyLast() {
        return markRollbackOnlyLast;
    }

    public void setMarkRollbackOnlyLast(Boolean markRollbackOnlyLast) {
        this.markRollbackOnlyLast = markRollbackOnlyLast;
    }
}