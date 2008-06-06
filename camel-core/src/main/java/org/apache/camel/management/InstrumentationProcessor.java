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
package org.apache.camel.management;

import org.apache.camel.Exchange;
import org.apache.camel.processor.DelegateProcessor;

/**
 * JMX enabled processor that uses the {@link Counter} for instrumenting
 * processing of exchanges.
 */
public class InstrumentationProcessor extends DelegateProcessor {

    private PerformanceCounter counter;

    public InstrumentationProcessor(PerformanceCounter counter) {
        this.counter = counter;
    }

    public InstrumentationProcessor() {
    }

    public void setCounter(PerformanceCounter counter) {
        this.counter = counter;
    }
    
    public void process(Exchange exchange) throws Exception {
        long startTime = System.currentTimeMillis();
        super.process(exchange);
        if (counter != null) {
            if (!exchange.isFailed()) {
                counter.completedExchange(System.currentTimeMillis() - startTime);
            } else {
                counter.completedExchange();
            }
        }
    }
}
