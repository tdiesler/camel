/*
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
package org.apache.camel.component.netty4;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Regression test for CAMEL-16227
 */
public class NettyReuseChannelCallbackTest extends BaseNettyTest {

    private static final Logger LOG = LoggerFactory.getLogger(NettyReuseChannelCallbackTest.class);

    private final List<Channel> channels = new ArrayList<>();

    @Test
    public void testReuse() throws Exception {
        final List<Endpoint> eventEndpoints = new ArrayList<>(4);
        final List<Long> times = new ArrayList<>(2);

        final EventNotifierSupport nettyEventRecorder = new EventNotifierSupport() {
            @Override
            public void notify(EventObject event) throws Exception {
                if (event instanceof ExchangeSendingEvent) {
                    LOG.info("Got event {}", event);
                    add(((ExchangeSendingEvent) event).getEndpoint());
                }
                if (event instanceof ExchangeSentEvent) {
                    LOG.info("Got event {}", event);
                    add(((ExchangeSentEvent) event).getEndpoint());
                    if (((ExchangeSentEvent) event).getEndpoint() instanceof NettyEndpoint) {
                        times.add(((ExchangeSentEvent) event).getTimeTaken());
                    }
                }
            }

            @Override
            public boolean isEnabled(EventObject event) {
                return true;
            }

            private void add(Endpoint endpoint) {
                if (endpoint instanceof NettyEndpoint) {
                    eventEndpoints.add(endpoint);
                }
            }
        };
        nettyEventRecorder.start();
        context.getManagementStrategy().addEventNotifier(nettyEventRecorder);

        NotifyBuilder notify = new NotifyBuilder(context).whenDone(1).create();

        getMockEndpoint("mock:a").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:b").expectedBodiesReceived("Hello Hello World");
        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World", "Hello Hello World");

        template.sendBody("direct:start", "World\n");

        assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);

        assertTrue(notify.matchesWaitTime());

        assertEquals(2, channels.size());
        assertSame("Should reuse channel", channels.get(0), channels.get(1));
        assertFalse("And closed when routing done", channels.get(0).isOpen());
        assertFalse("And closed when routing done", channels.get(1).isOpen());

        assertEquals("Should get 4 events for netty endpoints", 4, eventEndpoints.size());
        assertSame("Sending and sent event should contain the same endpoint", eventEndpoints.get(0), eventEndpoints.get(1));
        assertSame("Sending and sent event should contain the same endpoint", eventEndpoints.get(2), eventEndpoints.get(3));
        assertEquals("Should get 2 ExchangeSent events", 2, times.size());
        // one side effect of mixing callbacks in wrong time taken reported in event
        times.forEach(time -> assertTrue(time < 900));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Netty URIs are slightly different (different requestTimeout)
                // so there is different NettyEndpoint instance for each of them.
                // This makes distinguishing events easier in test.
                // If they URIs would be the same there would be one NettyEndpoint instance
                // but still 2 separate NettyProducer instances.
                from("direct:start")
                        .to("netty4:tcp://localhost:{{port}}?textline=true&sync=true&reuseChannel=true&disconnect=true&requestTimeout=1000")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Channel channel = exchange.getProperty(NettyConstants.NETTY_CHANNEL, Channel.class);
                                channels.add(channel);
                                assertTrue("Should be active", channel.isActive());
                            }
                        })
                        .to("mock:a")
                        .to("netty4:tcp://localhost:{{port}}?textline=true&sync=true&reuseChannel=true&disconnect=true&requestTimeout=2000")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Channel channel = exchange.getProperty(NettyConstants.NETTY_CHANNEL, Channel.class);
                                channels.add(channel);
                                assertTrue("Should be active", channel.isActive());
                            }
                        })
                        .to("mock:b");

                from("netty4:tcp://localhost:{{port}}?textline=true&sync=true")
                        .transform(body().prepend("Hello "))
                        .delay(500)
                        .to("mock:result");
            }
        };
    }
}
