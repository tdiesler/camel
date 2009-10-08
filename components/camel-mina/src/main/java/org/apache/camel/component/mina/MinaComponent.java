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
package org.apache.camel.component.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.DatagramAcceptorConfig;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAddress;
import org.apache.mina.transport.vmpipe.VmPipeConnector;

/**
 * Component for Apache MINA.
 *
 * @version $Revision$
 */
public class MinaComponent extends DefaultComponent<MinaExchange> {
    private static final transient Log LOG = LogFactory.getLog(MinaComponent.class);
    private MinaConfiguration configuration;

    public MinaComponent() {
    }

    public MinaComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint<MinaExchange> createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating MinaEndpoint from uri: " + uri);
        }

        // Using the configuration which set by the component as a default one
        // Since the configuration's properties will be set by the URI
        // we need to copy or create a new MinaConfiguration here
        MinaConfiguration config;
        if (configuration != null) {        
            config = configuration.copy();
        } else {
            config = new MinaConfiguration();
        }

        URI u = new URI(remaining);
        config.setHost(u.getHost());
        config.setPort(u.getPort());
        config.setProtocol(u.getScheme());
        setProperties(config, parameters);

        return createEndpoint(uri, config);
    }

    public Endpoint createEndpoint(MinaConfiguration config) throws Exception {
        return createEndpoint(null, config);
    }

    private Endpoint createEndpoint(String uri, MinaConfiguration config) throws Exception {
        String protocol = config.getProtocol();
        // if mistyped uri then protocol can be null
        if (protocol != null) {
            if (protocol.equals("tcp")) {
                return createSocketEndpoint(uri, config);
            } else if (protocol.equals("udp") || protocol.equals("mcast") || protocol.equals("multicast")) {
                return createDatagramEndpoint(uri, config);
            } else if (protocol.equals("vm")) {
                return createVmEndpoint(uri, config);
            }
        }
        // protocol not resolved so error
        throw new IllegalArgumentException("Unrecognised MINA protocol: " + protocol + " for uri: " + uri);

    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected MinaEndpoint createVmEndpoint(String uri, MinaConfiguration configuration) {
        boolean minaLogger = configuration.isMinaLogger();
        boolean sync = configuration.isSync();

        IoAcceptor acceptor = new VmPipeAcceptor();
        SocketAddress address = new VmPipeAddress(configuration.getPort());
        IoConnector connector = new VmPipeConnector();

        // connector config
        configureCodecFactory("MinaProducer", connector.getDefaultConfig(), configuration);
        if (minaLogger) {
            connector.getFilterChain().addLast("logger", new LoggingFilter());
        }

        // acceptor connectorConfig
        configureCodecFactory("MinaConsumer", acceptor.getDefaultConfig(), configuration);
        if (minaLogger) {
            acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        }

        MinaEndpoint endpoint = new MinaEndpoint(uri, this);
        endpoint.setAddress(address);
        endpoint.setAcceptor(acceptor);
        endpoint.setConnector(connector);
        endpoint.setConfiguration(configuration);

        // set sync or async mode after endpoint is created
        if (sync) {
            endpoint.setExchangePattern(ExchangePattern.InOut);
        } else {
            endpoint.setExchangePattern(ExchangePattern.InOnly);
        }

        return endpoint;
    }

    protected MinaEndpoint createSocketEndpoint(String uri, MinaConfiguration configuration) {
        boolean minaLogger = configuration.isMinaLogger();
        long timeout = configuration.getTimeout();
        boolean sync = configuration.isSync();
        final int processorCount = Runtime.getRuntime().availableProcessors() + 1;

        IoAcceptor acceptor = new SocketAcceptor(processorCount, Executors.newCachedThreadPool());
        IoConnector connector = new SocketConnector(processorCount, Executors.newCachedThreadPool());
        SocketAddress address = new InetSocketAddress(configuration.getHost(), configuration.getPort());

        // connector config
        SocketConnectorConfig connectorConfig = new SocketConnectorConfig();
        // must use manual thread model according to Mina documentation
        connectorConfig.setThreadModel(ThreadModel.MANUAL);
        configureCodecFactory("MinaProducer", connectorConfig, configuration);
        connectorConfig.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        if (minaLogger) {
            connectorConfig.getFilterChain().addLast("logger", new LoggingFilter());
        }
        // set connect timeout to mina in seconds
        connectorConfig.setConnectTimeout((int) (timeout / 1000));

        // acceptor connectorConfig
        SocketAcceptorConfig acceptorConfig = new SocketAcceptorConfig();
        configureCodecFactory("MinaConsumer", acceptorConfig, configuration);
        acceptorConfig.setReuseAddress(true);
        acceptorConfig.setDisconnectOnUnbind(true);
        acceptorConfig.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        if (minaLogger) {
            acceptorConfig.getFilterChain().addLast("logger", new LoggingFilter());
        }

        MinaEndpoint endpoint = new MinaEndpoint(uri, this);
        endpoint.setAddress(address);
        endpoint.setAcceptor(acceptor);
        endpoint.setAcceptorConfig(acceptorConfig);
        endpoint.setConnector(connector);
        endpoint.setConnectorConfig(connectorConfig);
        endpoint.setConfiguration(configuration);

        // set sync or async mode after endpoint is created
        if (sync) {
            endpoint.setExchangePattern(ExchangePattern.InOut);
        } else {
            endpoint.setExchangePattern(ExchangePattern.InOnly);
        }

        return endpoint;
    }

    protected void configureCodecFactory(String type, IoServiceConfig config, MinaConfiguration configuration) {
        ProtocolCodecFactory codecFactory = getCodecFactory(type, configuration.getCodec());

        if (codecFactory == null) {
            if (configuration.isTextline()) {
                Charset charset = getEncodingParameter(type, configuration);
                LineDelimiter delimiter = getLineDelimiterParameter(configuration.getTextlineDelimiter());
                codecFactory = new TextLineCodecFactory(charset, delimiter);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(type + ": Using TextLineCodecFactory: " + codecFactory + " using encoding: "
                            + charset + " and line delimiter: " + configuration.getTextlineDelimiter()
                            + "(" + delimiter + ")");
                }
            } else {
                codecFactory = new ObjectSerializationCodecFactory();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(type + ": Using ObjectSerializationCodecFactory: " + codecFactory);
                }
            }
        }

        addCodecFactory(config, codecFactory);
    }

    protected MinaEndpoint createDatagramEndpoint(String uri, MinaConfiguration configuration) {
        boolean minaLogger = configuration.isMinaLogger();
        long timeout = configuration.getTimeout();
        boolean transferExchange = configuration.isTransferExchange();
        boolean sync = configuration.isSync();

        IoAcceptor acceptor = new DatagramAcceptor(Executors.newCachedThreadPool());
        IoConnector connector = new DatagramConnector(Executors.newCachedThreadPool());
        SocketAddress address = new InetSocketAddress(configuration.getHost(), configuration.getPort());

        if (transferExchange) {
            throw new IllegalArgumentException("transferExchange=true is not supported for datagram protocol");
        }

        DatagramConnectorConfig connectorConfig = new DatagramConnectorConfig();
        configureDataGramCodecFactory("MinaProducer", connectorConfig, configuration);
        connectorConfig.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        if (minaLogger) {
            connectorConfig.getFilterChain().addLast("logger", new LoggingFilter());
        }
        // set connect timeout to mina in seconds
        connectorConfig.setConnectTimeout((int) (timeout / 1000));

        DatagramAcceptorConfig acceptorConfig = new DatagramAcceptorConfig();
        configureDataGramCodecFactory("MinaConsumer", acceptorConfig, configuration);
        acceptorConfig.setDisconnectOnUnbind(true);
        // reuse address is default true for datagram
        if (minaLogger) {
            acceptorConfig.getFilterChain().addLast("logger", new LoggingFilter());
        }

        MinaEndpoint endpoint = new MinaEndpoint(uri, this);
        endpoint.setAddress(address);
        endpoint.setAcceptor(acceptor);
        endpoint.setAcceptorConfig(acceptorConfig);
        endpoint.setConnector(connector);
        endpoint.setConnectorConfig(connectorConfig);
        endpoint.setConfiguration(configuration);
        // set sync or async mode after endpoint is created
        if (sync) {
            endpoint.setExchangePattern(ExchangePattern.InOut);
        } else {
            endpoint.setExchangePattern(ExchangePattern.InOnly);
        }

        return endpoint;
    }

    /**
     * For datagrams the entire message is available as a single ByteBuffer so lets just pass those around by default
     * and try converting whatever they payload is into ByteBuffers unless some custom converter is specified
     */
    protected void configureDataGramCodecFactory(final String type, final IoServiceConfig config, final MinaConfiguration configuration) {
        ProtocolCodecFactory codecFactory = getCodecFactory(type, configuration.getCodec());
        if (codecFactory == null) {
            final Charset charset = getEncodingParameter(type, configuration);
            
            codecFactory = new MinaUdpProtocolCodecFactory(getCamelContext(), charset);

            if (LOG.isDebugEnabled()) {
                LOG.debug(type + ": Using CodecFactory: " + codecFactory + " using encoding: " + charset);
            }
        }

        addCodecFactory(config, codecFactory);
    }

    private ProtocolCodecFactory getCodecFactory(String type, String codec) {
        ProtocolCodecFactory codecFactory = null;
        if (codec != null) {
            codecFactory = getCamelContext().getRegistry().lookup(codec, ProtocolCodecFactory.class);
            if (codecFactory == null) {
                throw new IllegalArgumentException("Codec " + codec + " not found in registry.");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(type + ": Using custom CodecFactory: " + codecFactory);
            }
        }
        return codecFactory;
    }

    private void addCodecFactory(IoServiceConfig config, ProtocolCodecFactory codecFactory) {
        config.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
    }

    private static LineDelimiter getLineDelimiterParameter(TextLineDelimiter delimiter) {
        if (delimiter == null) {
            return LineDelimiter.DEFAULT;
        }

        switch (delimiter) {
        case DEFAULT:
            return LineDelimiter.DEFAULT;
        case AUTO:
            return LineDelimiter.AUTO;
        case UNIX:
            return LineDelimiter.UNIX;
        case WINDOWS:
            return LineDelimiter.WINDOWS;
        case MAC:
            return LineDelimiter.MAC;
        default:
            throw new IllegalArgumentException("Unknown textline delimiter: " + delimiter);
        }
    }

    private static Charset getEncodingParameter(String type, MinaConfiguration configuration) {
        String encoding = configuration.getEncoding();
        if (encoding == null) {
            encoding = Charset.defaultCharset().name();
            // set in on configuration so its updated
            configuration.setEncoding(encoding);
            if (LOG.isDebugEnabled()) {
                LOG.debug(type + ": No encoding parameter using default charset: " + encoding);
            }
        }
        if (!Charset.isSupported(encoding)) {
            throw new IllegalArgumentException("The encoding: " + encoding + " is not supported");
        }

        return Charset.forName(encoding);
    }

    // Properties
    //-------------------------------------------------------------------------

    public MinaConfiguration getConfiguration() {        
        return configuration;
    }

    public void setConfiguration(MinaConfiguration configuration) {
        this.configuration = configuration;
    }
}
