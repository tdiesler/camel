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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Predicate;
import org.apache.camel.processor.validation.PredicateValidatingProcessor;
import org.apache.camel.spi.RouteContext;

/**
 * Represents an XML &lt;validate/&gt; element
 *
 * @version 
 */
@XmlRootElement(name = "validate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidateDefinition extends NoOutputExpressionNode {

    public ValidateDefinition() {
        super();
    }
    
    @Override
    public String toString() {
        return "Validate[" + getExpression() + " -> " + getOutputs() + "]";
    }
    
    @Override
    public String getShortName() {
        return "validate";
    }

    @Override
    public PredicateValidatingProcessor createProcessor(RouteContext routeContext) throws Exception {
        Predicate pred = getExpression().createPredicate(routeContext);
        return new PredicateValidatingProcessor(pred);
    }

}