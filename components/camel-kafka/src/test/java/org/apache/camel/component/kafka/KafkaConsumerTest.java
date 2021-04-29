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
package org.apache.camel.component.kafka;

import java.util.concurrent.ExecutorService;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.spi.ExecutorServiceManager;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KafkaConsumerTest {

    private KafkaConfiguration configuration = mock(KafkaConfiguration.class);
    private KafkaComponent component = mock(KafkaComponent.class);
    private KafkaEndpoint endpoint = mock(KafkaEndpoint.class);
    private Processor processor = mock(Processor.class);
    private CamelContext context = mock(CamelContext.class);
    private ExecutorService executorService = mock(ExecutorService.class);
    private ExecutorServiceManager executorServiceManager = mock(ExecutorServiceManager.class);

    @Before
    public void setUp() {
        when(endpoint.getComponent()).thenReturn(component);
        when(endpoint.getConfiguration()).thenReturn(configuration);
        when(endpoint.getCamelContext()).thenReturn(context);
        when(context.getExecutorServiceManager()).thenReturn(executorServiceManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumerRequiresBootstrapServers() throws Exception {
        when(endpoint.getConfiguration().getGroupId()).thenReturn("groupOne");
        new KafkaConsumer(endpoint, processor);
    }

    @Test
    public void consumerOnlyRequiresBootstrapServers() throws Exception {
        when(endpoint.getConfiguration().getBrokers()).thenReturn("localhost:2181");
        new KafkaConsumer(endpoint, processor);
    }

    @Test
    public void shutdownTimeout() throws Exception {
        int timeout = 1000;
        when(endpoint.getConfiguration().getBrokers()).thenReturn("localhost:2181");
        when(endpoint.getConfiguration().getShutdownTimeout()).thenReturn(timeout);

        KafkaConsumer consumer = new KafkaConsumer(endpoint, processor);
        consumer.executor = executorService;
        consumer.doStop();
        verify(executorServiceManager).shutdownGraceful(executorService, timeout);
    }

}
