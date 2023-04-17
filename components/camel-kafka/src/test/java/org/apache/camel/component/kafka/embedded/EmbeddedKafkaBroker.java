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
package org.apache.camel.component.kafka.embedded;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import kafka.metrics.KafkaMetricsReporter;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.zk.KafkaZkClient;

import org.apache.camel.test.AvailablePortFinder;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.mutable.Buffer;

import static org.apache.camel.component.kafka.embedded.TestUtils.constructTempDir;
import static org.apache.camel.component.kafka.embedded.TestUtils.perTest;

public class EmbeddedKafkaBroker extends ExternalResource {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Integer brokerId;
    private final Integer port;
    private final String zkConnection;
    private final Properties baseProperties;

    private final String brokerList;

    private KafkaServer kafkaServer;
    private File logDir;

    public EmbeddedKafkaBroker(int brokerId, String zkConnection) {
        this(brokerId, AvailablePortFinder.getNextAvailable(), zkConnection, new Properties());
    }

    public EmbeddedKafkaBroker(int brokerId, int port, String zkConnection, Properties baseProperties) {
        this.brokerId = brokerId;
        this.port = port;
        this.zkConnection = zkConnection;
        this.baseProperties = baseProperties;

        log.info("Starting broker[{}] on port {}", brokerId, port);
        this.brokerList = "localhost:" + this.port;
    }

    @Override
    public void before() {
        logDir = constructTempDir(perTest("kafka-log"));

        Properties properties = new Properties();
        properties.putAll(baseProperties);
        properties.setProperty("advertised.listeners","PLAINTEXT://localhost:"+port);
        properties.setProperty("listeners","PLAINTEXT://0.0.0.0:"+port);
        properties.setProperty("zookeeper.connect", zkConnection);
        properties.setProperty("broker.id", brokerId.toString());
        properties.setProperty("host.name", "localhost");
        properties.setProperty("port", Integer.toString(port));
        properties.setProperty("log.dir", logDir.getAbsolutePath());
        properties.setProperty("num.partitions", String.valueOf(1));
        properties.setProperty("auto.create.topics.enable", String.valueOf(Boolean.TRUE));
        log.info("log directory: " + logDir.getAbsolutePath());
        properties.setProperty("log.flush.interval.messages", String.valueOf(1));
        properties.setProperty("offsets.topic.replication.factor", String.valueOf(1));

        kafkaServer = startBroker(properties);
    }

    private KafkaServer startBroker(Properties props) {
        List<KafkaMetricsReporter> kmrList = new ArrayList<>();
        KafkaServer server = new KafkaServer(new KafkaConfig(props), new SystemTime(), Option.<String> empty(), false);
        server.startup();
        return server;
    }

    public String getBrokerList() {
        return brokerList;
    }

    public Integer getPort() {
        return port;
    }

    public void after() {
        kafkaServer.shutdown();
        try {
            TestUtils.deleteFile(logDir);
        } catch (FileNotFoundException e) {
            log.info("Could not delete {} - not found", logDir.getAbsolutePath());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmbeddedKafkaBroker{");
        sb.append("brokerList='").append(brokerList).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
