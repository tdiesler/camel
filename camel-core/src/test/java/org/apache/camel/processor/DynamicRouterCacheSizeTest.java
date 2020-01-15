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

import java.util.Map;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.ServicePoolAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.junit.Test;

public class DynamicRouterCacheSizeTest extends ContextTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        camelContext.addComponent("service-pool-aware", new ServicePoolAwareComponent());
        return camelContext;
    }

    @Test
    public void testCache() {
        template.sendBodyAndHeader("direct:start", "Hello World", "whereto", "foo");
        template.sendBodyAndHeader("direct:start", "Bye World", "whereto", "foo");
        template.sendBodyAndHeader("direct:start", "Hi World", "whereto", "bar");


        SendDynamicProcessor proc = (SendDynamicProcessor) context.getProcessor("myCacheSize");
        assertEquals(0, proc.producerCache.getCapacity());
        assertEquals(0, proc.producerCache.size());
    }

    @Test
    public void testServicePoolAwareCache() {
        template.sendBodyAndHeader("direct:pool", "Hello World", "whereto", "foo");
        template.sendBodyAndHeader("direct:pool", "Bye World", "whereto", "foo");
        template.sendBodyAndHeader("direct:pool", "Hi World", "whereto", "bar");


        SendDynamicProcessor proc = (SendDynamicProcessor) context.getProcessor("myServicePoolAwareCacheSize");
        assertEquals(0, proc.producerCache.getCapacity());
        assertEquals(0, proc.producerCache.size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .toD("mock:${header.whereto}", -1).id("myCacheSize");

                from("direct:pool")
                        .toD("service-pool-aware:${header.whereto}", -1).id("myServicePoolAwareCacheSize");
            }
        };
    }

    private class ServicePoolAwareComponent extends MockComponent {
        @Override
        protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) {
            SericePoolAwareEndpoint endpoint = new SericePoolAwareEndpoint(uri, this);
            endpoint.setName(remaining);
            return endpoint;
        }
    }

    private class SericePoolAwareEndpoint extends MockEndpoint {
        public SericePoolAwareEndpoint(String endpointUri, Component component) {
            super(endpointUri, component);
        }
        @Override
        public Producer createProducer() {
            return new ServicePoolAwareProducer(this) {
                public boolean process(Exchange exchange, AsyncCallback callback) {
                    onExchange(exchange);
                    callback.done(true);
                    return true;
                }
            };
        }
    }

    private class ServicePoolAwareProducer extends DefaultAsyncProducer implements ServicePoolAware {
        public ServicePoolAwareProducer(Endpoint endpoint) {
            super(endpoint);
        }
        public boolean process(Exchange exchange, AsyncCallback callback) {
            return false;
        }
    }
}
