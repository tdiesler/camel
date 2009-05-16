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
package org.apache.camel.model.loadbalancer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.processor.loadbalancer.FailOverLoadBalancer;
import org.apache.camel.processor.loadbalancer.LoadBalancer;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

@XmlRootElement(name = "failOver")
@XmlAccessorType(XmlAccessType.FIELD)
public class FailOverLoadBalanceStrategy extends LoadBalancerDefinition {    

    @XmlAttribute (name = "exception", required = false)
    private String failedException;
    
    @Override
    protected LoadBalancer createLoadBalancer(RouteContext routeContext) {
        if (ObjectHelper.isNotEmpty(failedException)) {
            Class type = routeContext.getCamelContext().getClassResolver().resolveClass(failedException);
            if (type == null) {
                throw new IllegalArgumentException("Cannot find class: " + failedException + " in the classpath");
            }
            return new FailOverLoadBalancer(type);
        } else {
            return new FailOverLoadBalancer();
        }
    }
    
    public void setFailedException(String exceptionName) {
        failedException = exceptionName;
    }
    
    public String getFailedException() {
        return failedException;
    }

    @Override
    public String toString() {
        return "FailOverLoadBalancer";
    }
}
