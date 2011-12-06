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
package org.apache.camel.api.management.mbean;

import org.apache.camel.api.management.ManagedAttribute;

public interface ManagedThrottlingInflightRoutePolicyMBean {

    @ManagedAttribute(description = "Maximum inflight exchanges")
    int getMaxInflightExchanges();

    @ManagedAttribute(description = "Maximum inflight exchanges")
    void setMaxInflightExchanges(int maxInflightExchanges);

    @ManagedAttribute(description = "Resume percentage of maximum inflight exchanges")
    int getResumePercentOfMax();

    @ManagedAttribute(description = "Resume percentage of maximum inflight exchanges")
    void setResumePercentOfMax(int resumePercentOfMax);

    @ManagedAttribute(description = "Scope")
    String getScope();

    @ManagedAttribute(description = "Scope")
    void setScope(String scope);

    @ManagedAttribute(description = "Logging Level")
    String getLoggingLevel();

    @ManagedAttribute(description = "Logging Level")
    void setLoggingLevel(String loggingLevel);

}