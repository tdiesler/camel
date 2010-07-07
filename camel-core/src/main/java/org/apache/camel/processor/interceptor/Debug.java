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
package org.apache.camel.processor.interceptor;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.Debugger;
import org.apache.camel.spi.InterceptStrategy;

/**
 * @version $Revision$
 */
public class Debug implements InterceptStrategy {

    private final Debugger debugger;

    public Debug(Debugger debugger) {
        this.debugger = debugger;
    }

    public Processor wrapProcessorInInterceptors(final CamelContext context, final ProcessorDefinition<?> definition,
                                                 final Processor target, final Processor nextTarget) throws Exception {
        return new DelegateAsyncProcessor(target) {
            @Override
            public boolean process(Exchange exchange, AsyncCallback callback) {
                debugger.onExchange(exchange, target, definition);
                return super.process(exchange, callback);
            }

            @Override
            public String toString() {
                return "Debug[" + target + "]";
            }
        };
    }
}
