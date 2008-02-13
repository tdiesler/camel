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
package org.apache.camel.fix;

import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Service;
import org.apache.camel.Exchange;
import org.apache.camel.processor.loadbalancer.LoadBalancer;
import org.apache.camel.processor.loadbalancer.RoundRobinLoadBalancer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import quickfix.*;

/**
 * @version $Revision: 1.1 $
 */
public class FixEndpoint extends DefaultEndpoint implements Service {
    private final String resourceUri;
    private Session session;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private Resource resource;
    private SocketAcceptor acceptor;
    private SessionID sessionID;
    private LoadBalancer loadBalancer;

    public FixEndpoint(String uri, CamelContext camelContext, String resourceUri) {
        super(uri, camelContext);
        this.resourceUri = resourceUri;
    }

    public FixEndpoint(String uri, Component component, String resourceUri) {
        super(uri, component);
        this.resourceUri = resourceUri;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new LoadBalancerConsumer();
    }

    public Producer createProducer() throws Exception {
        return new FixProducer(this);
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Called when a message is sent to the application
     */
    public void onMessage(Message message) throws Exception {
        Exchange exchange = createExchange(message);
        getLoadBalancer().process(exchange);
    }

    public Exchange createExchange(Message message) {
        Exchange answer = createExchange();
        answer.getIn().setBody(message);
        return answer;
    }

    public void start() throws Exception {
        Resource configResource = getResource();
        InputStream inputStream = configResource.getInputStream();
        ObjectHelper.notNull(inputStream, "Could not load " + resourceUri);

        SessionSettings settings = new SessionSettings(inputStream);

        Application application = new CamelApplication(this);
        FileStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(settings);
        acceptor = new SocketAcceptor(application, storeFactory, settings,
                logFactory, new DefaultMessageFactory());

        acceptor.start();
    }

    public void stop() throws Exception {
        if (session != null) {
            session.disconnect();
            session = null;
        }
        if (acceptor != null) {
            acceptor.stop();
            acceptor = null;
        }
    }

    
    // Properties
    //-------------------------------------------------------------------------

    public Session getSession() throws Exception {
        if (session == null) {
            session = createSession();
            ObjectHelper.notNull(session, "FIX Session");
        }
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public LoadBalancer getLoadBalancer() {
        if (loadBalancer == null){
            loadBalancer = new RoundRobinLoadBalancer();
        }
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Resource getResource() {
        if (resource == null) {
            resource = getResourceLoader().getResource(resourceUri);
            if (resource == null) {
                throw new IllegalArgumentException("Could not find resource for URI: " + resourceUri + " using: " + getResourceLoader());
            }
        }
        return resource;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Session createSession() throws Exception {
        return Session.lookupSession(sessionID);
    }
}
