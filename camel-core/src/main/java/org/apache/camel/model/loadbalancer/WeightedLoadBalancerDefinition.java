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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.model.LoadBalancerDefinition;
import org.apache.camel.processor.loadbalancer.LoadBalancer;
import org.apache.camel.processor.loadbalancer.WeightedLoadBalancer;
import org.apache.camel.processor.loadbalancer.WeightedRandomLoadBalancer;
import org.apache.camel.processor.loadbalancer.WeightedRoundRobinLoadBalancer;
import org.apache.camel.spi.RouteContext;

/**
 * Represents an XML &lt;sticky/&gt; element
 */
@XmlRootElement(name = "weighted")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeightedLoadBalancerDefinition extends LoadBalancerDefinition {
    
    @XmlElement(name = "roundRobin", required = true)
    private boolean roundRobin;
    
    @XmlElement(name = "distributionRatios", required = true)
    private ArrayList<Integer> distributionRatioList;
    
    @Override
    protected LoadBalancer createLoadBalancer(RouteContext routeContext) {
        WeightedLoadBalancer loadBalancer = null;
        
        try {
            if (!roundRobin) {
                loadBalancer = new WeightedRandomLoadBalancer(distributionRatioList);
            } else {
                loadBalancer = new WeightedRoundRobinLoadBalancer(distributionRatioList);
            }
        } catch (Exception e) {
            
        }
        return loadBalancer;
    }

    public boolean isRoundRobin() {
        return roundRobin;
    }

    public void setRoundRobin(boolean roundRobin) {
        this.roundRobin = roundRobin;
    }

    public ArrayList<Integer> getDistributionRatioList() {
        return distributionRatioList;
    }

    public void setDistributionRatioList(ArrayList<Integer> distributionRatioList) {
        this.distributionRatioList = distributionRatioList;
    }

    @Override
    public String toString() {
        if (!roundRobin) { 
            return "WeightedRandomLoadBalancer[" + distributionRatioList + "]";
        } else {
            return "WeightedRoundRobinLoadBalancer[" + distributionRatioList + "]";
        }
    }
}
