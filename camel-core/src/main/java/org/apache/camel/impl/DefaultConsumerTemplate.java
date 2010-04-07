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

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.ServiceHelper;

import static org.apache.camel.util.ObjectHelper.wrapRuntimeCamelException;

/**
 * @version $Revision$
 */
public class DefaultConsumerTemplate extends ServiceSupport implements ConsumerTemplate {

    private final CamelContext context;
    private ConsumerCache consumerCache;
    private int maximumCacheSize;

    public DefaultConsumerTemplate(CamelContext context) {
        this.context = context;
    }

    public int getMaximumCacheSize() {
        return maximumCacheSize;
    }

    public void setMaximumCacheSize(int maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
    }

    public int getCurrentCacheSize() {
        if (consumerCache == null) {
            return 0;
        }
        return consumerCache.size();
    }

    public CamelContext getCamelContext() {
        return context;
    }

    public Exchange receive(String endpointUri) {
        Endpoint endpoint = resolveMandatoryEndpoint(endpointUri);
        return getConsumerCache().receive(endpoint);
    }

    public Exchange receive(Endpoint endpoint) {
        return receive(endpoint.getEndpointUri());
    }

    public Exchange receive(String endpointUri, long timeout) {
        Endpoint endpoint = resolveMandatoryEndpoint(endpointUri);
        return getConsumerCache().receive(endpoint, timeout);
    }

    public Exchange receive(Endpoint endpoint, long timeout) {
        return receive(endpoint.getEndpointUri(), timeout);
    }

    public Exchange receiveNoWait(String endpointUri) {
        Endpoint endpoint = resolveMandatoryEndpoint(endpointUri);
        return getConsumerCache().receiveNoWait(endpoint);
    }

    public Exchange receiveNoWait(Endpoint endpoint) {
        return receiveNoWait(endpoint.getEndpointUri());
    }

    public Object receiveBody(String endpointUri) {
        Exchange exchange = receive(endpointUri);
        return extractResultBody(exchange);
    }

    public Object receiveBody(Endpoint endpoint) {
        return receiveBody(endpoint.getEndpointUri());
    }

    public Object receiveBody(String endpointUri, long timeout) {
        Exchange exchange = receive(endpointUri, timeout);
        return extractResultBody(exchange);
    }

    public Object receiveBody(Endpoint endpoint, long timeout) {
        return receiveBody(endpoint.getEndpointUri(), timeout);
    }

    public Object receiveBodyNoWait(String endpointUri) {
        Exchange exchange = receiveNoWait(endpointUri);
        return extractResultBody(exchange);
    }

    public Object receiveBodyNoWait(Endpoint endpoint) {
        return receiveBodyNoWait(endpoint.getEndpointUri());
    }

    public <T> T receiveBody(String endpointUri, Class<T> type) {
        Object body = receiveBody(endpointUri);
        return context.getTypeConverter().convertTo(type, body);
    }

    public <T> T receiveBody(Endpoint endpoint, Class<T> type) {
        return receiveBody(endpoint.getEndpointUri(), type);
    }

    public <T> T receiveBody(String endpointUri, long timeout, Class<T> type) {
        Object body = receiveBody(endpointUri, timeout);
        return context.getTypeConverter().convertTo(type, body);
    }

    public <T> T receiveBody(Endpoint endpoint, long timeout, Class<T> type) {
        return receiveBody(endpoint.getEndpointUri(), timeout, type);
    }

    public <T> T receiveBodyNoWait(String endpointUri, Class<T> type) {
        Object body = receiveBodyNoWait(endpointUri);
        return context.getTypeConverter().convertTo(type, body);
    }

    public <T> T receiveBodyNoWait(Endpoint endpoint, Class<T> type) {
        return receiveBodyNoWait(endpoint.getEndpointUri(), type);
    }

    protected Endpoint resolveMandatoryEndpoint(String endpointUri) {
        return CamelContextHelper.getMandatoryEndpoint(context, endpointUri);
    }

    /**
     * Extracts the body from the given result.
     * <p/>
     * If the exchange pattern is provided it will try to honor it and retrieve the body
     * from either IN or OUT according to the pattern.
     *
     * @param result   the result
     * @return  the result, can be <tt>null</tt>.
     */
    protected Object extractResultBody(Exchange result) {
        Object answer = null;
        if (result != null) {
            // rethrow if there was an exception
            if (result.getException() != null) {
                throw wrapRuntimeCamelException(result.getException());
            }

            // okay no fault then return the response
            if (result.hasOut()) {
                // use OUT as the response
                answer = result.getOut().getBody();
            } else {
                // use IN as the response
                answer = result.getIn().getBody();
            }
        }
        return answer;
    }

    private ConsumerCache getConsumerCache() {
        if (!isStarted()) {
            throw new IllegalStateException("ConsumerTemplate has not been started");
        }
        return consumerCache;
    }

    protected void doStart() throws Exception {
        if (consumerCache == null) {
            if (maximumCacheSize > 0) {
                consumerCache = new ConsumerCache(context, maximumCacheSize);
            } else {
                consumerCache = new ConsumerCache(context);
            }
        }
        ServiceHelper.startService(consumerCache);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopService(consumerCache);
        consumerCache = null;
    }

}
