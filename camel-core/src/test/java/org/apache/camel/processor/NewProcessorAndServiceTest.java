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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Service;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class NewProcessorAndServiceTest extends ContextTestSupport {

    private MyProcessor myProcessor;

    public void testNewProcessorAndService() throws Exception {
        String out = template.requestBody("direct:start", "Hello World", String.class);
        assertEquals("Bye World", out);

        assertTrue("MyProcessor should be started", myProcessor.isStarted());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").process(myProcessor = new MyProcessor());
            }
        };
    }

    private static class MyProcessor implements Processor, Service {

        private boolean started;

        public void process(Exchange exchange) throws Exception {
            exchange.getOut().setBody("Bye World");
        }

        public void start() throws Exception {
            started = true;
        }

        public void stop() throws Exception {
            started = false;
        }

        public boolean isStarted() {
            return started;
        }
    }
}
