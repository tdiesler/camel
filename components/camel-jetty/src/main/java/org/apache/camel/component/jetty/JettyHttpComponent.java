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
package org.apache.camel.component.jetty;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.http.CamelServlet;
import org.apache.camel.component.http.HttpBinding;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpConsumer;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.spi.ManagementAgent;
import org.apache.camel.spi.ManagementStrategy;
import org.apache.camel.util.CastUtils;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.URISupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.MultiPartFilter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * An HttpComponent which starts an embedded Jetty for to handle consuming from
 * the http endpoints.
 *
 * @version $Revision$
 */
public class JettyHttpComponent extends HttpComponent {
    public static final String TMP_DIR = "CamelJettyTempDir";
    
    protected static final HashMap<String, ConnectorRef> CONNECTORS = new HashMap<String, ConnectorRef>();
   
    private static final transient Log LOG = LogFactory.getLog(JettyHttpComponent.class);
    private static final String JETTY_SSL_KEYSTORE = "org.eclipse.jetty.ssl.keystore";
    
    protected String sslKeyPassword;
    protected String sslPassword;
    protected String sslKeystore;
    protected Map<Integer, SslSocketConnector> sslSocketConnectors;
    protected HttpClient httpClient;
    protected ThreadPool httpClientThreadPool;
    protected Integer httpClientMinThreads;
    protected Integer httpClientMaxThreads;
    protected MBeanContainer mbContainer;
    protected boolean enableJmx;

    class ConnectorRef {
        Server server;
        Connector connector;
        CamelServlet servlet;
        int refCount;

        public ConnectorRef(Server server, Connector connector, CamelServlet servlet) {
            this.server = server;
            this.connector = connector;
            this.servlet = servlet;
            increment();
        }

        public int increment() {
            return ++refCount;
        }

        public int decrement() {
            return --refCount;
        }
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        uri = uri.startsWith("jetty:") ? remaining : uri;

        // must extract well known parameters before we create the endpoint
        List<Handler> handlerList = resolveAndRemoveReferenceListParameter(parameters, "handlers", Handler.class);
        HttpBinding binding = resolveAndRemoveReferenceParameter(parameters, "httpBindingRef", HttpBinding.class);
        Boolean throwExceptionOnFailure = getAndRemoveParameter(parameters, "throwExceptionOnFailure", Boolean.class);
        Boolean bridgeEndpoint = getAndRemoveParameter(parameters, "bridgeEndpoint", Boolean.class);
        Boolean matchOnUriPrefix = getAndRemoveParameter(parameters, "matchOnUriPrefix", Boolean.class);
        Boolean enableJmx = getAndRemoveParameter(parameters, "enableJmx", Boolean.class);

        // configure http client if we have url configuration for it
        // http client is only used for jetty http producer (hence not very commonly used)
        HttpClient client = null;
        if (IntrospectionSupport.hasProperties(parameters, "httpClient.")) {
            // set additional parameters on http client
            // only create client when needed
            client = getHttpClient();
            IntrospectionSupport.setProperties(client, parameters, "httpClient.");
            // validate that we could resolve all httpClient. parameters as this component is lenient
            validateParameters(uri, parameters, "httpClient.");
        }

        // restructure uri to be based on the parameters left as we dont want to include the Camel internal options
        URI httpUri = URISupport.createRemainingURI(new URI(uri), CastUtils.cast(parameters));
        uri = httpUri.toString();

        // create endpoint after all known parameters have been extracted from parameters
        JettyHttpEndpoint endpoint = new JettyHttpEndpoint(this, uri, httpUri);
        setEndpointHeaderFilterStrategy(endpoint);

        if (client != null) {
            endpoint.setClient(client);
        }
        if (handlerList.size() > 0) {
            endpoint.setHandlers(handlerList);
        }
        // prefer to use endpoint configured over component configured
        if (binding == null) {
            // fallback to component configured
            binding = getHttpBinding();
        }
        if (binding != null) {
            endpoint.setBinding(binding);
        }
        // should we use an exception for failed error codes?
        if (throwExceptionOnFailure != null) {
            endpoint.setThrowExceptionOnFailure(throwExceptionOnFailure);
        }
        if (bridgeEndpoint != null) {
            endpoint.setBridgeEndpoint(bridgeEndpoint);
        }
        if (matchOnUriPrefix != null) {
            endpoint.setMatchOnUriPrefix(matchOnUriPrefix);
        }
        
        if (enableJmx != null) {
            endpoint.setEnableJmx(enableJmx);
        } else { 
            // set this option based on setting of JettyHttpComponent
            endpoint.setEnableJmx(isEnableJmx());
        }

        setProperties(endpoint, parameters);
        return endpoint;
    }

    /**
     * Connects the URL specified on the endpoint to the specified processor.
     */
    @Override
    public void connect(HttpConsumer consumer) throws Exception {
        // Make sure that there is a connector for the requested endpoint.
        JettyHttpEndpoint endpoint = (JettyHttpEndpoint)consumer.getEndpoint();
        String connectorKey = getConnectorKey(endpoint);

        synchronized (CONNECTORS) {
            ConnectorRef connectorRef = CONNECTORS.get(connectorKey);
            if (connectorRef == null) {
                Connector connector;
                if ("https".equals(endpoint.getProtocol())) {
                    connector = getSslSocketConnector(endpoint.getPort());
                } else {
                    connector = new SelectChannelConnector();
                }
                connector.setPort(endpoint.getPort());
                connector.setHost(endpoint.getHttpUri().getHost());
                if ("localhost".equalsIgnoreCase(endpoint.getHttpUri().getHost())) {
                    LOG.warn("You use localhost interface! It means that no external connections will be available. Don't you want to use 0.0.0.0 instead (all network interfaces)?");
                }
                Server server = createServer();
                if (endpoint.isEnableJmx()) {
                    enableJmx(server);
                }
                server.addConnector(connector);

                connectorRef = new ConnectorRef(server, connector, createServletForConnector(server, connector, endpoint.getHandlers()));
                connectorRef.server.start();
                
                CONNECTORS.put(connectorKey, connectorRef);
                
            } else {
                // ref track the connector
                connectorRef.increment();
            }
            // check the session support
            if (endpoint.isSessionSupport()) {                
                enableSessionSupport(connectorRef.server);
            }
            connectorRef.servlet.connect(consumer);
        }
    }
    
    private void enableJmx(Server server) {
        MBeanContainer containerToRegister = getMbContainer();
        if (containerToRegister != null) {
            server.getContainer().addEventListener(containerToRegister);
            // Since we may have many Servers running, don't tie the MBeanContainer
            // to a Server lifecycle or we end up closing it while it is still in use.
            //server.addBean(mbContainer);
        }
    }

    private void enableSessionSupport(Server server) throws Exception {
        ServletContextHandler context = (ServletContextHandler)server.getChildHandlerByClass(ServletContextHandler.class);
        if (context.getSessionHandler() == null) {
            SessionHandler sessionHandler = new SessionHandler();
            if (context.isStarted()) {
                // restart the context
                context.stop();
                context.setSessionHandler(sessionHandler);
                context.start();
            } else {
                context.setSessionHandler(sessionHandler);
            }
        }
    }

    /**
     * Disconnects the URL specified on the endpoint from the specified processor.
     */
    @Override
    public void disconnect(HttpConsumer consumer) throws Exception {
        // If the connector is not needed anymore then stop it
        HttpEndpoint endpoint = consumer.getEndpoint();
        String connectorKey = getConnectorKey(endpoint);
        
        synchronized (CONNECTORS) {
            ConnectorRef connectorRef = CONNECTORS.get(connectorKey);
            if (connectorRef != null) {
                connectorRef.servlet.disconnect(consumer);
                if (connectorRef.decrement() == 0) {
                    connectorRef.server.removeConnector(connectorRef.connector);
                    connectorRef.connector.stop();
                    connectorRef.server.stop();
                    CONNECTORS.remove(connectorKey);
                    // Camel controls the lifecycle of these entities so remove the
                    // registered MBeans when Camel is done with the managed objects.
                    MBeanContainer containerToClean = getMbContainer(); 
                    if (containerToClean != null) {
                        containerToClean.removeBean(connectorRef.server);
                        containerToClean.removeBean(connectorRef.connector);
                    }
                }
            }
        }
    }
    
    private String getConnectorKey(HttpEndpoint endpoint) {
        return endpoint.getProtocol() + ":" + endpoint.getHttpUri().getHost() + ":" + endpoint.getPort();
    }

    // Properties
    // -------------------------------------------------------------------------
       
    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public void setSslKeyPassword(String sslKeyPassword) {
        this.sslKeyPassword = sslKeyPassword;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }

    public void setKeystore(String sslKeystore) {
        this.sslKeystore = sslKeystore;
    }

    public String getKeystore() {
        return sslKeystore;
    }

    public SslSocketConnector getSslSocketConnector(int port) {
        SslSocketConnector answer = null;
        if (sslSocketConnectors != null) {
            answer = sslSocketConnectors.get(port);
        }
        if (answer == null) {
            answer = createSslSocketConnector();
        } else {
            // try the keystore system property as a backup, jetty doesn't seem
            // to read this property anymore
            String keystoreProperty = System.getProperty(JETTY_SSL_KEYSTORE);
            if (keystoreProperty != null) {
                answer.setKeystore(keystoreProperty);
            }

        }
        return answer;
    }
    
    public SslSocketConnector createSslSocketConnector() {
        SslSocketConnector answer = new SslSocketConnector();
        // with default null values, jetty ssl system properties
        // and console will be read by jetty implementation
        answer.setPassword(sslPassword);
        answer.setKeyPassword(sslKeyPassword);
        if (sslKeystore != null) {
            answer.setKeystore(sslKeystore);
        } else {
            // try the keystore system property as a backup, jetty doesn't seem
            // to read this property anymore
            String keystoreProperty = System.getProperty(JETTY_SSL_KEYSTORE);
            if (keystoreProperty != null) {
                answer.setKeystore(keystoreProperty);
            }
        }
        
        return answer;
    }

    public void setSslSocketConnectors(Map <Integer, SslSocketConnector> connectors) {
        sslSocketConnectors = connectors;
    }

    public synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient();
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);

            if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
                String host = System.getProperty("http.proxyHost");
                int port = Integer.parseInt(System.getProperty("http.proxyPort"));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Java System Property http.proxyHost and http.proxyPort detected. Using http proxy host: "
                            + host + " port: " + port);
                }
                httpClient.setProxy(new Address(host, port));
            }

            // use QueueThreadPool as the default bounded is deprecated (see SMXCOMP-157)
            if (getHttpClientThreadPool() == null) {
                QueuedThreadPool qtp = new QueuedThreadPool();
                if (httpClientMinThreads != null) {
                    qtp.setMinThreads(httpClientMinThreads.intValue());
                }
                if (httpClientMaxThreads != null) {
                    qtp.setMaxThreads(httpClientMaxThreads.intValue());
                }
                try {
                    qtp.start();
                } catch (Exception e) {
                    throw new RuntimeCamelException("Error starting JettyHttpClient thread pool: " + qtp, e);
                }
                setHttpClientThreadPool(qtp);
            }
            httpClient.setThreadPool(getHttpClientThreadPool());
        }
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ThreadPool getHttpClientThreadPool() {
        return httpClientThreadPool;
    }

    public void setHttpClientThreadPool(ThreadPool httpClientThreadPool) {
        this.httpClientThreadPool = httpClientThreadPool;
    }

    public Integer getHttpClientMinThreads() {
        return httpClientMinThreads;
    }

    public void setHttpClientMinThreads(Integer httpClientMinThreads) {
        this.httpClientMinThreads = httpClientMinThreads;
    }

    public Integer getHttpClientMaxThreads() {
        return httpClientMaxThreads;
    }

    public void setHttpClientMaxThreads(Integer httpClientMaxThreads) {
        this.httpClientMaxThreads = httpClientMaxThreads;
    }

    public void setEnableJmx(boolean enableJmx) {
        this.enableJmx = enableJmx;
    }

    public boolean isEnableJmx() {
        return enableJmx;
    }

    public synchronized MBeanContainer getMbContainer() {
        // If null, provide the default implementation.
        if (mbContainer == null) {
            MBeanServer mbs = null;
            
            final ManagementStrategy mStrategy = 
                this.getCamelContext().getManagementStrategy();
            final ManagementAgent mAgent = mStrategy.getManagementAgent();
            if (mAgent != null) {
                mbs = mAgent.getMBeanServer();
            }
            
            if (mbs != null) {
                mbContainer = new MBeanContainer(mbs);
                startMbContainer();
            } else {
                LOG.warn("JMX disabled in Camel Context.  The Camel Context takes precedent and JMX will not be enabled in Jetty.");
            }
        }
        
        return this.mbContainer;
    }

    public void setMbContainer(MBeanContainer mbContainer) {
        this.mbContainer = mbContainer;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected CamelServlet createServletForConnector(Server server, Connector connector, List<Handler> handlers) throws Exception {
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);
        context.setConnectorNames(new String[] {connector.getName()});

        if (handlers != null && !handlers.isEmpty()) {
            for (Handler handler : handlers) {
                if (handler instanceof HandlerWrapper) {
                    ((HandlerWrapper) handler).setHandler(server.getHandler());
                    server.setHandler(handler);
                } else {
                    HandlerCollection handlerCollection = new HandlerCollection();
                    handlerCollection.addHandler(server.getHandler());
                    handlerCollection.addHandler(handler);
                    server.setHandler(handlerCollection);
                }
            }
        }

        CamelServlet camelServlet = new CamelServlet();
        ServletHolder holder = new ServletHolder();
        holder.setServlet(camelServlet);
        CamelContext camelContext = this.getCamelContext();
        FilterHolder filterHolder = new FilterHolder();
        filterHolder.setInitParameter("deleteFiles", "true");
        if (camelContext.getProperties().get(TMP_DIR) != null) {
            context.setAttribute("javax.servlet.context.tempdir", camelContext.getProperties().get(TMP_DIR));
        }
        filterHolder.setFilter(new MultiPartFilter());
        //add the default MultiPartFilter filter for it
        context.addFilter(filterHolder, "/*", 0);
        context.addServlet(holder, "/*");

        return camelServlet;
    }
    
    protected Server createServer() throws Exception {
        Server server = new Server();
        ContextHandlerCollection collection = new ContextHandlerCollection();
        server.setHandler(collection);
        return server;
    }
    
    /**
     * Starts {@link #mbContainer} and registers the
     * container with itself as a managed bean logging an error
     * if there is a problem starting the container.  Does nothing
     * if {@link #mbContainer} is {@code null}.
     */
    protected void startMbContainer() {
        if (mbContainer != null && !mbContainer.isStarted()) {
            try {
                mbContainer.start();
                // Publish the container itself for consistency with
                // traditional embedded Jetty configurations.
                mbContainer.addBean(mbContainer);
            } catch (Exception e) {
                LOG.warn("Could not start Jetty MBeanContainer.  Jetty JMX extensions will remain disabled.", e);
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (httpClientThreadPool != null && httpClientThreadPool instanceof LifeCycle) {
            LifeCycle lc = (LifeCycle) httpClientThreadPool;
            lc.start();
        }
        if (httpClient != null && !httpClient.isStarted()) {
            httpClient.start();
        }
        
        startMbContainer();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (CONNECTORS.size() > 0) {
            for (ConnectorRef connectorRef : CONNECTORS.values()) {
                connectorRef.server.removeConnector(connectorRef.connector);
                connectorRef.connector.stop();
                connectorRef.server.stop();
                // Camel controls the lifecycle of these entities so remove the
                // registered MBeans when Camel is done with the managed objects.
                if (mbContainer != null) {
                    mbContainer.removeBean(connectorRef.server);
                    mbContainer.removeBean(connectorRef.connector);
                }
            }
            CONNECTORS.clear();
        }
        if (httpClient != null) {
            httpClient.stop();
        }
        if (httpClientThreadPool != null && httpClientThreadPool instanceof LifeCycle) {
            LifeCycle lc = (LifeCycle) httpClientThreadPool;
            lc.stop();
        }
        if (mbContainer != null) {
            mbContainer.stop();
        }
    }
}