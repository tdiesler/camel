/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.web.resources;

import com.sun.jersey.api.view.ImplicitProduces;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.spi.UnitOfWork;

import java.util.Map;

/**
 * A Camel <a href="http://camel.apache.org/exchange.html">message exchange</a> 
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Constants.HTML_MIME_TYPES)
public class ExchangeResource {
    private final EndpointResource endpointResource;
    private final Exchange exchange;

    public ExchangeResource(EndpointResource endpointResource, Exchange exchange) {
        this.endpointResource = endpointResource;
        this.exchange = exchange;
    }

    public Exchange getExchange() {
        return exchange;
    }

    // Helper methods for the view
    public CamelContext getCamelContext() {
        return exchange.getContext();
    }

    public String getExchangeId() {
        return exchange.getExchangeId();
    }

    public Throwable getException() {
        return exchange.getException();
    }

    public Message getFault() {
        return exchange.getFault();
    }

    public Endpoint getFromEndpoint() {
        return exchange.getFromEndpoint();
    }

    public Message getIn() {
        return exchange.getIn();
    }

    public Message getOut() {
        return exchange.getOut(false);
    }

    public Map<String, Object> getProperties() {
        return exchange.getProperties();
    }

    public ExchangePattern getPattern() {
        return exchange.getPattern();
    }

    public UnitOfWork getUnitOfWork() {
        return exchange.getUnitOfWork();
    }

    public boolean isFailed() {
        return exchange.isFailed();
    }

    public boolean isTransacted() {
        return exchange.isTransacted();
    }
}
