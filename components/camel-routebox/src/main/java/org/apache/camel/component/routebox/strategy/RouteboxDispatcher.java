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
package org.apache.camel.component.routebox.strategy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.routebox.RouteboxEndpoint;
import org.apache.camel.impl.SynchronizationAdapter;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RouteboxDispatcher {
    private static final transient Log LOG = LogFactory.getLog(RouteboxDispatcher.class);
    private ProducerTemplate producer;
    
    public RouteboxDispatcher(ProducerTemplate producer) {
        super();
        this.producer = producer;
    }

    public Exchange dispatchSync(RouteboxEndpoint endpoint, Exchange exchange) throws Exception {
        URI dispatchUri = null;
        Exchange reply = null;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching exchange" + exchange + "to endpoint " + endpoint.getEndpointUri());
        }
        
        dispatchUri = selectDispatchUri(endpoint, exchange);
        
        if (exchange.getPattern() == ExchangePattern.InOnly) {
            reply = producer.send(dispatchUri.toASCIIString(), exchange);
        } else {
            reply = (Exchange) issueRequest(endpoint, ExchangePattern.InOut, exchange.getIn().getBody(), exchange.getIn().getHeaders());        
        }

        return reply;
    }
    
    public Exchange dispatchAsync(RouteboxEndpoint endpoint, Exchange exchange) throws Exception {
        URI dispatchUri = null;
        Exchange reply = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching exchange" + exchange + "to endpoint " + endpoint.getEndpointUri());
        }
        
        dispatchUri = selectDispatchUri(endpoint, exchange);
        
        if (exchange.getPattern() == ExchangePattern.InOnly) {
            producer.asyncSend(dispatchUri.toASCIIString(), exchange);
            reply = exchange;
        } else {
            Future<Exchange> future = producer.asyncCallback(dispatchUri.toASCIIString(), exchange, new SynchronizationAdapter());
            reply = future.get(endpoint.getConfig().getConnectionTimeout(), TimeUnit.MILLISECONDS);
        }
        
        return reply;
    }
    
    protected URI selectDispatchUri(RouteboxEndpoint endpoint, Exchange exchange) throws Exception {
        URI dispatchUri = null;
        
        List<URI> consumerUris = getInnerContextConsumerList(endpoint.getConfig().getInnerContext());
        if (consumerUris.isEmpty()) {
            throw new CamelException("No routes found for dispatch in Routebox");
        } else if (consumerUris.size() == 1) {
            dispatchUri = consumerUris.get(0);
        } else {
            if (!endpoint.getConfig().getDispatchMap().isEmpty()) {
                //apply URI string found in dispatch Map
                if (endpoint.getConfig().getDispatchMap().containsKey(exchange.getIn().getHeader("ROUTE_DISPATCH_KEY"))) {             
                    dispatchUri = new URI(endpoint.getConfig().getDispatchMap().get(exchange.getIn().getHeader("ROUTE_DISPATCH_KEY")));
                } else {
                    throw new CamelException("No matching entry found in Dispatch Map for ROUTE_DISPATCH_KEY: " + exchange.getIn().getHeader("ROUTE_DISPATCH_KEY"));
                }
            } else {
                //apply dispatch strategy
                dispatchUri = endpoint.getConfig().getDispatchStrategy().selectDestinationUri(consumerUris, exchange);
                if (dispatchUri == null) {
                    throw new CamelException("No matching inner routes found for Operation");
                }
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatch URI set to: " + dispatchUri.toASCIIString());
        }
        
        return dispatchUri;
    }
    
    protected List<URI> getInnerContextConsumerList(CamelContext context) throws URISyntaxException {
        List<URI> consumerList = new ArrayList<URI>();
        List<RouteDefinition> routeDefinitions = context.getRouteDefinitions();
        for (RouteDefinition routeDefinition : routeDefinitions) {
            List<FromDefinition> inputs = routeDefinition.getInputs();
            for (FromDefinition input : inputs) {
                consumerList.add(new URI(input.getUri()));
            }
        }
        return consumerList;
    }
    
    public Exchange issueRequest(Endpoint endpoint, ExchangePattern pattern, final Object body, final Map<String, Object> headers) throws CamelExecutionException {
        Exchange exchange = producer.send(endpoint, pattern, new Processor() {
            public void process(Exchange exchange) throws Exception {
                Message in = exchange.getIn();
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    in.setHeader(header.getKey(), header.getValue());
                }
                in.setBody(body);
            }
        });
        
        return exchange;
    }
    
}