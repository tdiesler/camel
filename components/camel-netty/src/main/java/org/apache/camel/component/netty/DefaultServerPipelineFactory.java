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
package org.apache.camel.component.netty;

import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.camel.CamelContext;
import org.apache.camel.component.netty.handlers.ServerChannelHandler;
import org.apache.camel.component.netty.ssl.SSLEngineFactory;
import org.apache.camel.util.ObjectHelper;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultServerPipelineFactory extends ServerPipelineFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultServerPipelineFactory.class);

    private NettyConsumer consumer;
    private SSLContext sslContext;

    @Deprecated
    public DefaultServerPipelineFactory(NettyServerBootstrapConfiguration configuration) {
        this.consumer = null;
        try {
            this.sslContext = createSSLContext(null, configuration);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        if (sslContext != null) {
            LOG.info("Created SslContext {}", sslContext);
        }
    }

    public DefaultServerPipelineFactory(NettyConsumer consumer) {
        this.consumer = consumer;
        try {
            this.sslContext = createSSLContext(consumer.getContext(), consumer.getConfiguration());
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        if (sslContext != null) {
            LOG.info("Created SslContext {}", sslContext);
        }
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline channelPipeline = Channels.pipeline();

        SslHandler sslHandler = configureServerSSLOnDemand();
        if (sslHandler != null) {
            // must close on SSL exception
            sslHandler.setCloseOnSSLException(true);
            LOG.debug("Server SSL handler configured and added as an interceptor against the ChannelPipeline: {}", sslHandler);
            addToPipeline("ssl", channelPipeline, sslHandler);
        }

        List<ChannelHandler> encoders = consumer.getConfiguration().getEncoders();
        for (int x = 0; x < encoders.size(); x++) {
            ChannelHandler encoder = encoders.get(x);
            if (encoder instanceof ChannelHandlerFactory) {
                // use the factory to create a new instance of the channel as it may not be shareable
                encoder = ((ChannelHandlerFactory) encoder).newChannelHandler();
            }
            addToPipeline("encoder-" + x, channelPipeline, encoder);
        }

        List<ChannelHandler> decoders = consumer.getConfiguration().getDecoders();
        for (int x = 0; x < decoders.size(); x++) {
            ChannelHandler decoder = decoders.get(x);
            if (decoder instanceof ChannelHandlerFactory) {
                // use the factory to create a new instance of the channel as it may not be shareable
                decoder = ((ChannelHandlerFactory) decoder).newChannelHandler();
            }
            addToPipeline("decoder-" + x, channelPipeline, decoder);
        }

        if (consumer.getConfiguration().isOrderedThreadPoolExecutor()) {
            // this must be added just before the ServerChannelHandler
            // use ordered thread pool, to ensure we process the events in order, and can send back
            // replies in the expected order. eg this is required by TCP.
            // and use a Camel thread factory so we have consistent thread namings
            ExecutionHandler executionHandler = new ExecutionHandler(consumer.getEndpoint().getComponent().getExecutorService());
            addToPipeline("executionHandler", channelPipeline, executionHandler);
            LOG.debug("Using OrderedMemoryAwareThreadPoolExecutor with core pool size: {}", consumer.getConfiguration().getMaximumPoolSize());
        }

        // our handler must be added last
        addToPipeline("handler", channelPipeline, new ServerChannelHandler(consumer));

        LOG.trace("Created ChannelPipeline: {}", channelPipeline);
        return channelPipeline;
    }

    private void addToPipeline(String name, ChannelPipeline pipeline, ChannelHandler handler) {
        pipeline.addLast(name, handler);
    }

    private SSLContext createSSLContext(CamelContext camelContext, NettyServerBootstrapConfiguration configuration) throws Exception {
        if (!configuration.isSsl()) {
            return null;
        }

        SSLContext answer;

        // create ssl context once
        if (configuration.getSslContextParameters() != null) {
            answer = configuration.getSslContextParameters().createSSLContext();
        } else {
            if (configuration.getKeyStoreFile() == null && configuration.getKeyStoreResource() == null) {
                LOG.debug("keystorefile is null");
            }
            if (configuration.getTrustStoreFile() == null && configuration.getTrustStoreResource() == null) {
                LOG.debug("truststorefile is null");
            }
            if (configuration.getPassphrase().toCharArray() == null) {
                LOG.debug("passphrase is null");
            }

            SSLEngineFactory sslEngineFactory;
            if (configuration.getKeyStoreFile() != null || configuration.getTrustStoreFile() != null) {
                sslEngineFactory = new SSLEngineFactory();
                answer = sslEngineFactory.createSSLContext(camelContext.getClassResolver(),
                        configuration.getKeyStoreFormat(),
                        configuration.getSecurityProvider(),
                        "file:" + configuration.getKeyStoreFile().getPath(),
                        "file:" + configuration.getTrustStoreFile().getPath(),
                        configuration.getPassphrase().toCharArray());
            } else {
                sslEngineFactory = new SSLEngineFactory();
                answer = sslEngineFactory.createSSLContext(camelContext.getClassResolver(),
                        configuration.getKeyStoreFormat(),
                        configuration.getSecurityProvider(),
                        configuration.getKeyStoreResource(),
                        configuration.getTrustStoreResource(),
                        configuration.getPassphrase().toCharArray());
            }
        }

        return answer;
    }

    private SslHandler configureServerSSLOnDemand() throws Exception {
        if (!consumer.getConfiguration().isSsl()) {
            return null;
        }

        if (consumer.getConfiguration().getSslHandler() != null) {
            return consumer.getConfiguration().getSslHandler();
        } else if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(consumer.getConfiguration().isNeedClientAuth());
            return new SslHandler(engine);
        }

        return null;
    }

    @Override
    public ServerPipelineFactory createPipelineFactory(NettyConsumer consumer) {
        return new DefaultServerPipelineFactory(consumer);
    }
}
