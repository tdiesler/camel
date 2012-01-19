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
package org.apache.camel.component.jclouds;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.CastUtils;
import org.apache.camel.util.ObjectHelper;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcloudsBlobStoreConsumer extends ScheduledBatchPollingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JcloudsBlobStoreConsumer.class);
    private final JcloudsBlobStoreEndpoint endpoint;
    private final String container;
    private final BlobStore blobStore;
    private int maxMessagesPerPoll = 10;

    public JcloudsBlobStoreConsumer(JcloudsBlobStoreEndpoint endpoint, Processor processor, BlobStore blobStore) {
        super(endpoint, processor);
        this.blobStore = blobStore;
        this.endpoint = endpoint;
        this.container = endpoint.getContainer();
    }

    @Override
    protected int poll() throws Exception {
        shutdownRunningTask = null;
        pendingExchanges = 0;

        Queue<Exchange> queue = new LinkedList<Exchange>();

        ListContainerOptions opt = new ListContainerOptions();

        for (StorageMetadata md : blobStore.list(container, opt.maxResults(maxMessagesPerPoll))) {
            String blobName = md.getName();
            Object body = JcloudsBlobStoreHelper.readBlob(blobStore, container, blobName, Thread.currentThread().getContextClassLoader());
            Exchange exchange = endpoint.createExchange();
            exchange.getIn().setBody(body);
            exchange.setProperty(JcloudsConstants.BLOB_NAME, blobName);
            queue.add(exchange);
        }
        return queue.isEmpty() ? 0 : processBatch(CastUtils.cast(queue));
    }

    public int processBatch(Queue<Object> exchanges) throws Exception {
        int total = exchanges.size();

        for (int index = 0; index < total && isBatchAllowed(); index++) {
            // only loop if we are started (allowed to run)
            Exchange exchange = ObjectHelper.cast(Exchange.class, exchanges.poll());
            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // update pending number of exchanges
            pendingExchanges = total - index - 1;

            // add on completion to handle after work when the exchange is done
            exchange.addOnCompletion(new Synchronization() {
                public void onComplete(Exchange exchange) {
                    String blobName = (String) exchange.getProperty(JcloudsConstants.BLOB_NAME);
                    blobStore.removeBlob(container, blobName);
                }

                public void onFailure(Exchange exchange) {
                 //empty method
                }
            });

            LOG.trace("Processing exchange [{}]...", exchange);
            getProcessor().process(exchange);
        }

        return total;
    }
}
