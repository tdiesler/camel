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
package org.apache.camel.component.restlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.CamelContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

/**
 * A Camel component embedded Restlet that produces and consumes exchanges.
 *
 * @version $Revision$
 */
public class RestletComponent extends DefaultComponent {
    private static final Log LOG = LogFactory.getLog(RestletComponent.class);

    private Map<String, Server> servers = new HashMap<String, Server>();
    private Map<String, MethodBasedRouter> routers = new HashMap<String, MethodBasedRouter>();

    private Component component = new Component();

    @Override
    protected Endpoint createEndpoint(String uri, String remaining,
            Map parameters) throws Exception {
        
        RestletBinding restletBinding = null;
        // lookup binding in registry if provided
        String ref = getAndRemoveParameter(parameters, "restletBindingRef", String.class);
        if (ref != null) {
            restletBinding = CamelContextHelper.mandatoryLookup(getCamelContext(), 
                    ref, RestletBinding.class);
        }
        
        if (restletBinding == null) {
            restletBinding = new DefaultRestletBinding();
        }
        
        Method method = getAndRemoveParameter(parameters, "restletMethod", Method.class);
        RestletEndpoint result = new RestletEndpoint(this, remaining, parameters, restletBinding);
        if (method != null) {
            result.setRestletMethod(method);
        }
        
        return result;
    }
    
    @Override
    protected void doStart() throws Exception {
        try {
            super.doStart();
        } catch (Exception e) {
            LOG.warn("Failed to stop", e);
        }
        component.start();
    }
    
    @Override
    protected void doStop() throws Exception {
        try {
            component.stop();
        } catch (Exception e) {
            LOG.warn("Failed to stop", e);
        }
        super.doStop();
    }
    
    public void connect(RestletConsumer consumer) throws Exception {
        RestletEndpoint endpoint = (RestletEndpoint)consumer.getEndpoint();
        addServerIfNeccessary(endpoint);
        MethodBasedRouter router = getMethodRouter(endpoint.getUriPattern());
        router.addRoute(endpoint.getRestletMethod(), consumer.getRestlet());
        
        if (!router.hasBeenAttached()) {
            component.getDefaultHost().attach(endpoint.getUriPattern(), router);
            if (LOG.isDebugEnabled()) {
                LOG.debug("attached methodRouter uriPattern: " + endpoint.getUriPattern());
            }
        }
        
        LOG.info("Attached restlet uriPattern: " + endpoint.getUriPattern() + " method: " 
                + endpoint.getRestletMethod());

    }

    public void disconnect(RestletConsumer consumer) throws Exception {
        RestletEndpoint endpoint = (RestletEndpoint)consumer.getEndpoint();
        MethodBasedRouter router = getMethodRouter(endpoint.getUriPattern());
        router.removeRoute(endpoint.getRestletMethod());
        LOG.info("Detached restlet uriPattern: " + endpoint.getUriPattern() + " method: " 
                + endpoint.getRestletMethod());
    }    
    
    private MethodBasedRouter getMethodRouter(String uriPattern) {
        synchronized (routers) {
            MethodBasedRouter result = routers.get(uriPattern);
            if (result == null) {
                result = new MethodBasedRouter(uriPattern);
                routers.put(uriPattern, result);
            }
            return result;
        }    
    }
    
    private void addServerIfNeccessary(RestletEndpoint endpoint) throws Exception {
        String key = buildKey(endpoint);
        Server server = null;
        synchronized (servers) {
            server = servers.get(key);
            if (server == null) {
                server = component.getServers().add(Protocol.valueOf(endpoint.getProtocol()), 
                        endpoint.getPort());
                servers.put(key, server);
                LOG.info("Add server: " + key);
                server.start();
            }
        }
    }
    
    private String buildKey(RestletEndpoint endpoint) {
        return endpoint.getHost() + ":" + endpoint.getPort();
    }
    
}

