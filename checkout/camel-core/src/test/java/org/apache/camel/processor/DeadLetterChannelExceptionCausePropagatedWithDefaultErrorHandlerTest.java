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
package org.apache.camel.processor;

import org.apache.camel.builder.ProcessorBuilder;
import org.apache.camel.builder.RouteBuilder;

public class DeadLetterChannelExceptionCausePropagatedWithDefaultErrorHandlerTest extends DeadLetterChannelExceptionCausePropagatedTest {

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                onException(RuntimeException.class).handled(true).to("mock:failed");

                from("direct:start")
                    .process(ProcessorBuilder.throwException(RUNTIME_EXCEPTION))
                    .to("mock:success");
            }
        };
    }

}