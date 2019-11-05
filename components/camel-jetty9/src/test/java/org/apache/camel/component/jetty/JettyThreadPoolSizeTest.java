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
package org.apache.camel.component.jetty;

import java.util.Collection;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.ThreadUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JettyThreadPoolSizeTest extends BaseJettyTest {

    private static final Logger LOG =  LoggerFactory.getLogger(JettyThreadPoolSizeTest.class);

    @Test
    public void threadPoolTest() throws  Exception {

        long initialJettyThreadNumber = countJettyThread();

        LOG.info("initial Jetty thread number (expected 5): " + initialJettyThreadNumber);

        context.stop();

        long jettyThreadNumberAfterStop =  countJettyThread();

        LOG.info("Jetty thread number after stopping Camel Context: (expected 0): " + jettyThreadNumberAfterStop);


        JettyHttpComponent jettyComponent = (JettyHttpComponent)context.getComponent("jetty");
        jettyComponent.setMinThreads(5);
        jettyComponent.setMaxThreads(5);

        context.start();

        long jettyThreadNumberAfterRestart = countJettyThread();
        LOG.info("Jetty thread number after starting Camel Context: (expected 5): " + jettyThreadNumberAfterRestart);


        assertEquals(5L, initialJettyThreadNumber);

        assertEquals(0L, jettyThreadNumberAfterStop);

        assertEquals(5L, jettyThreadNumberAfterRestart);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // setup the jetty component with the custom minThreads
                JettyHttpComponent jettyComponent = (JettyHttpComponent)context.getComponent("jetty");
                jettyComponent.setMinThreads(5);
                jettyComponent.setMaxThreads(5);

                from("jetty://http://localhost:{{port}}/myserverWithCustomPoolSize").to("mock:result");
            }
        };
    }

    private long countJettyThread() {
        Collection<Thread> threadSet = ThreadUtils.getAllThreads();
        return threadSet.stream().filter(thread -> thread.getName().contains("CamelJettyServer")).count();
    }

}