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

import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.api.management.mbean.ManagedCounterMBean;
import org.apache.camel.spi.ManagementStrategy;

@ManagedResource(description = "Managed Counter")
public abstract class ManagedCounter implements ManagedCounterMBean {
    protected Statistic exchangesTotal;

    public void init(ManagementStrategy strategy) {
        this.exchangesTotal = new Statistic("org.apache.camel.exchangesTotal", this, Statistic.UpdateMode.COUNTER);
    }

    public synchronized void reset() {
        exchangesTotal.reset();
    }

    public long getExchangesTotal() throws Exception {
        return exchangesTotal.getValue();
    }

    public synchronized void increment() {
        exchangesTotal.increment();
    }
}
