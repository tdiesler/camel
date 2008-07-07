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

import java.util.Map;

import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorType;
import org.apache.camel.spi.ErrorHandlerWrappingStrategy;
import org.apache.camel.spi.RouteContext;

/**
 * @version $Revision$
 */
public class InstrumentationErrorHandlerWrappingStrategy implements
        ErrorHandlerWrappingStrategy {

    private Map<ProcessorType, PerformanceCounter> counterMap;

    public InstrumentationErrorHandlerWrappingStrategy(
            Map<ProcessorType, PerformanceCounter> counterMap) {
        this.counterMap = counterMap;
    }

    public Processor wrapProcessorInErrorHandler(RouteContext routeContext, ProcessorType processorType,
                                                 Processor target) throws Exception {

        // don't wrap our instrumentation interceptors
        if (counterMap.containsKey(processorType)) {
            return processorType.getErrorHandlerBuilder().createErrorHandler(routeContext, target);
        }

        return target;
    }

}
