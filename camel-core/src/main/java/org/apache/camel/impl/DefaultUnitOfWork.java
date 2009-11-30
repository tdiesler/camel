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
package org.apache.camel.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Service;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.spi.UnitOfWork;
import org.apache.camel.util.EventHelper;
import org.apache.camel.util.UuidGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of {@link org.apache.camel.spi.UnitOfWork}
 *
 * @version $Revision$
 */
public class DefaultUnitOfWork implements UnitOfWork, Service {
    private static final transient Log LOG = LogFactory.getLog(DefaultUnitOfWork.class);

    private String id;
    private List<Synchronization> synchronizations;
    private Message originalInMessage;
    private final TracedRouteNodes tracedRouteNodes;

    public DefaultUnitOfWork(Exchange exchange) {
        tracedRouteNodes = new DefaultTracedRouteNodes();

        // TODO: optimize to only copy original message if enabled to do so in the route
        // special for JmsMessage as it can cause it to loose headers later.
        if (exchange.getIn().getClass().getSimpleName().equals("JmsMessage")) {
            this.originalInMessage = new DefaultMessage();
            this.originalInMessage.setBody(exchange.getIn().getBody());
            // cannot copy headers with a JmsMessage as the underlying javax.jms.Message object goes nuts 
        } else {
            this.originalInMessage = exchange.getIn().copy();
        }

        // fire event
        EventHelper.notifyExchangeCreated(exchange.getContext(), exchange);

        // register to inflight registry
        if (exchange.getContext() != null) {
            exchange.getContext().getInflightRepository().add(exchange);
        }
    }

    public void start() throws Exception {
        id = null;
    }

    public void stop() throws Exception {
        // need to clean up when we are stopping to not leak memory
        if (synchronizations != null) {
            synchronizations.clear();
        }
        if (tracedRouteNodes != null) {
            tracedRouteNodes.clear();
        }
        originalInMessage = null;
    }

    public synchronized void addSynchronization(Synchronization synchronization) {
        if (synchronizations == null) {
            synchronizations = new ArrayList<Synchronization>();
        }
        synchronizations.add(synchronization);
    }

    public synchronized void removeSynchronization(Synchronization synchronization) {
        if (synchronizations != null) {
            synchronizations.remove(synchronization);
        }
    }

    public void handoverSynchronization(Exchange target) {
        if (synchronizations == null || synchronizations.isEmpty()) {
            return;
        }

        for (Synchronization synchronization : synchronizations) {
            target.addOnCompletion(synchronization);
        }

        // clear this list as its handed over to the other exchange
        this.synchronizations.clear();
    }

    public void done(Exchange exchange) {
        boolean failed = exchange.isFailed();

        // fire event to signal the exchange is done
        try {
            if (failed) {
                EventHelper.notifyExchangeFailed(exchange.getContext(), exchange);
            } else {
                EventHelper.notifyExchangeDone(exchange.getContext(), exchange);
            }
        } catch (Exception e) {
            // must catch exceptions to ensure synchronizations is also invoked
            LOG.warn("Exception occurred during event notification. This exception will be ignored.", e);
        }

        if (synchronizations != null && !synchronizations.isEmpty()) {
            // invoke synchronization callbacks
            for (Synchronization synchronization : synchronizations) {
                try {
                    if (failed) {
                        synchronization.onFailure(exchange);
                    } else {
                        synchronization.onComplete(exchange);
                    }
                } catch (Exception e) {
                    // must catch exceptions to ensure all synchronizations have a chance to run
                    LOG.warn("Exception occurred during onCompletion. This exception will be ignored.", e);
                }
            }
        }

        // unregister from inflight registry
        if (exchange.getContext() != null) {
            exchange.getContext().getInflightRepository().remove(exchange);
        }
    }

    public String getId() {
        if (id == null) {
            id = UuidGenerator.get().generateUuid();
        }
        return id;
    }

    public Message getOriginalInMessage() {
        return originalInMessage;
    }

    public TracedRouteNodes getTracedRouteNodes() {
        return tracedRouteNodes;
    }

}
