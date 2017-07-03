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
package org.apache.camel.component.consul.ha;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.orbitz.consul.Consul;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.ha.ClusteredRoutePolicyFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulClusteredRoutePolicyIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulClusteredRoutePolicyIT.class);
    private static final List<String> CLIENTS = IntStream.range(0, 3).mapToObj(Integer::toString).collect(Collectors.toList());
    private static final List<String> RESULTS = new ArrayList<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(CLIENTS.size() * 2);
    private static final CountDownLatch LATCH = new CountDownLatch(CLIENTS.size());
    private static final String CONSUL_HOST = System.getProperty("camel.consul.host", Consul.DEFAULT_HTTP_HOST);
    private static final int CONSUL_PORT = Integer.getInteger("camel.consul.port", Consul.DEFAULT_HTTP_PORT);

    // ************************************
    // Test
    // ************************************

    @Test
    public void test() throws Exception {
        for (String id : CLIENTS) {
            SCHEDULER.submit(() -> run(id));
        }

        LATCH.await(1, TimeUnit.MINUTES);
        SCHEDULER.shutdownNow();

        Assert.assertEquals(CLIENTS.size(), RESULTS.size());
        Assert.assertTrue(RESULTS.containsAll(CLIENTS));
    }

    // ************************************
    // Run a Camel node
    // ************************************

    private static void run(String id) {
        try {
            CountDownLatch contextLatch = new CountDownLatch(1);

            ConsulClusterService service = new ConsulClusterService();
            service.setId("node-" + id);
            service.setUrl(String.format("http://%s:%d", CONSUL_HOST, CONSUL_PORT));

            LOGGER.info("Consul URL {}", service.getUrl());

            DefaultCamelContext context = new DefaultCamelContext();
            context.disableJMX();
            context.setName("context-" + id);
            context.addService(service);
            context.addRoutePolicyFactory(ClusteredRoutePolicyFactory.forNamespace("my-ns"));
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("timer:consul?delay=1s&period=1s&repeatCount=1")
                        .routeId("route-" + id)
                        .process(e -> {
                            LOGGER.debug("Node {} done", id);
                            RESULTS.add(id);
                            // Shutdown the context later on to give a chance to
                            // other members to catch-up
                            SCHEDULER.schedule(contextLatch::countDown, 2 + ThreadLocalRandom.current().nextInt(3), TimeUnit.SECONDS);
                        });
                }
            });

            // Start the context after some random time so the startup order
            // changes for each test.
            Thread.sleep(ThreadLocalRandom.current().nextInt(500));
            context.start();

            contextLatch.await();
            context.stop();

            LATCH.countDown();
        } catch (Exception e) {
            LOGGER.warn("", e);
        }
    }
}
