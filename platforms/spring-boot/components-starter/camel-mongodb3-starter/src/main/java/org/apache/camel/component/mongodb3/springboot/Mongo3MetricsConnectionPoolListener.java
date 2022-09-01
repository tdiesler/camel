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
package org.apache.camel.component.mongodb3.springboot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mongodb.connection.ServerId;
import com.mongodb.event.ConnectionAddedEvent;
import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionPoolClosedEvent;
import com.mongodb.event.ConnectionPoolListener;
import com.mongodb.event.ConnectionPoolOpenedEvent;
import com.mongodb.event.ConnectionPoolWaitQueueEnteredEvent;
import com.mongodb.event.ConnectionPoolWaitQueueExitedEvent;
import com.mongodb.event.ConnectionRemovedEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;



@NonNullApi
@NonNullFields
public class Mongo3MetricsConnectionPoolListener  implements ConnectionPoolListener {

    private static final String METRIC_PREFIX = "mongodb.driver.pool.";

    private final Map<ServerId, AtomicInteger> poolSizes = new ConcurrentHashMap<>();
    private final Map<ServerId, AtomicInteger> checkedOutCounts = new ConcurrentHashMap<>();
    private final Map<ServerId, AtomicInteger> waitQueueSizes = new ConcurrentHashMap<>();
    private final Map<ServerId, List<Meter>> meters = new ConcurrentHashMap<>();

    private final MeterRegistry registry;
    private final Mongo3ConnectionPoolTagsProvider tagsProvider;



    /**
     * Create a new {@code MongoMetricsConnectionPoolListener}.
     *
     * @param registry registry to use
     */
    public Mongo3MetricsConnectionPoolListener(MeterRegistry registry) {
        this(registry, new DefaultMongo3ConnectionPoolTagsProvider());
    }

    /**
     * Create a new {@code MongoMetricsConnectionPoolListener}.
     *
     * @param registry registry to use
     * @param tagsProvider tags provider to use
     * @since 1.7.0
     */
    public Mongo3MetricsConnectionPoolListener(MeterRegistry registry, Mongo3ConnectionPoolTagsProvider tagsProvider) {
        this.registry = registry;
        this.tagsProvider = tagsProvider;
    }

    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent event) {
        List<Meter> connectionMeters = new ArrayList<>();
        connectionMeters.add(registerGauge(event, METRIC_PREFIX + "size",
                "the current size of the connection pool, including idle and and in-use members", poolSizes));
        connectionMeters.add(registerGauge(event, METRIC_PREFIX + "checkedout",
                "the count of connections that are currently in use", checkedOutCounts));
        connectionMeters.add(registerGauge(event, METRIC_PREFIX + "waitqueuesize",
                "the current size of the wait queue for a connection from the pool", waitQueueSizes));
        meters.put(event.getServerId(), connectionMeters);
    }


    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent event) {
        ServerId serverId = event.getServerId();
        for (Meter meter : meters.get(serverId)) {
            registry.remove(meter);
        }
        meters.remove(serverId);
        poolSizes.remove(serverId);
        checkedOutCounts.remove(serverId);
        waitQueueSizes.remove(serverId);
    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        AtomicInteger checkedOutCount = checkedOutCounts.get(event.getConnectionId().getServerId());
        if (checkedOutCount != null) {
            checkedOutCount.incrementAndGet();
        }

    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        AtomicInteger checkedOutCount = checkedOutCounts.get(event.getConnectionId().getServerId());
        if (checkedOutCount != null) {
            checkedOutCount.decrementAndGet();
        }
    }

    @Override
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent event) {
        AtomicInteger waitQueueSize = waitQueueSizes.get(event.getServerId());
        if (waitQueueSize != null) {
            waitQueueSize.incrementAndGet();
        }
    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent event) {
        AtomicInteger waitQueueSize = waitQueueSizes.get(event.getServerId());
        if (waitQueueSize != null) {
            waitQueueSize.decrementAndGet();
        }
    }

    @Override
    public void connectionAdded(ConnectionAddedEvent event) {
        AtomicInteger poolSize = poolSizes.get(event.getConnectionId().getServerId());
        if (poolSize != null) {
            poolSize.incrementAndGet();
        }
    }

    @Override
    public void connectionRemoved(ConnectionRemovedEvent event) {
        AtomicInteger poolSize = poolSizes.get(event.getConnectionId().getServerId());
        if (poolSize != null) {
            poolSize.decrementAndGet();
        }
    }

    private Gauge registerGauge(ConnectionPoolOpenedEvent event, String metricName, String description, Map<ServerId, AtomicInteger> metrics) {
        AtomicInteger value = new AtomicInteger();
        metrics.put(event.getServerId(), value);
        return Gauge.builder(metricName, value, AtomicInteger::doubleValue)
                .description(description)
                .tags(tagsProvider.connectionPoolTags(event))
                .register(registry);
    }

}
