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
package org.apache.camel.component.elasticsearch;

import java.net.URI;
import java.util.Map;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an Elasticsearch endpoint.
 */
public class ElasticsearchEndpoint extends DefaultEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchEndpoint.class);

    private Node node;
    private Client client;
    private ElasticsearchConfiguration config;

    public ElasticsearchEndpoint(String uri, ElasticsearchComponent component, Map<String, Object> parameters) throws Exception {
        super(uri, component);
        this.config = new ElasticsearchConfiguration(new URI(uri), parameters);
    }

    public Producer createProducer() throws Exception {
        return new ElasticsearchProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Cannot consume to a ElasticsearchEndpoint: " + getEndpointUri());
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (config.isLocal()) {
            LOG.info("Starting local ElasticSearch server");
        } else {
            LOG.info("Joining ElasticSearch cluster " + config.getClusterName());
        }

        if (config.getIp() != null && !config.isLocal()) {
            Settings settings = ImmutableSettings.settingsBuilder()
                    // setting the classloader here will allow the underlying elasticsearch-java
                    // class to find its names.txt in an OSGi environment (otherwise the thread
                    // classloader is used, which won't be able to see the file causing a startup
                    // exception).
                    .classLoader(Settings.class.getClassLoader())
                    .put("cluster.name", config.getClusterName()).put("node.client", true).build();
            Client client = new TransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(config.getIp(), config.getPort()));
            this.client = client;
        } else {
            NodeBuilder builder = nodeBuilder().local(config.isLocal()).data(config.isData());
            if (!config.isLocal() && config.getClusterName() != null) {
                builder.clusterName(config.getClusterName());
            }
            builder.getSettings().classLoader(Settings.class.getClassLoader());
            node = builder.node();
            client = node.client();
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (config.isLocal()) {
            LOG.info("Stopping local ElasticSearch server");
        } else {
            LOG.info("Leaving ElasticSearch cluster " + config.getClusterName());
        }
        client.close();
        node.close();
        super.doStop();
    }

    public Client getClient() {
        return client;
    }

    public ElasticsearchConfiguration getConfig() {
        return config;
    }

    public void setOperation(String operation) {
        config.setOperation(operation);
    }

}
