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

import javax.net.ssl.SSLEngine;

import org.apache.camel.component.netty.handlers.ServerChannelHandler;
import org.apache.camel.component.netty.ssl.SSLEngineFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslHandler;

public class ServerPipelineFactory implements ChannelPipelineFactory {
    private static final transient Log LOG = LogFactory.getLog(ServerPipelineFactory.class);
    private NettyConsumer consumer;
        
    public ServerPipelineFactory(NettyConsumer consumer) {
        this.consumer = consumer; 
    }    

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline channelPipeline = Channels.pipeline();

        SslHandler sslHandler = configureServerSSLOnDemand();
        if (sslHandler != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Server SSL handler configured and added as an interceptor against the ChannelPipeline");
            }
            channelPipeline.addLast("ssl", sslHandler);            
        }
        channelPipeline.addLast("decoder", consumer.getConfiguration().getDecoder());
        channelPipeline.addLast("encoder", consumer.getConfiguration().getEncoder());
        if (consumer.getConfiguration().getHandler() != null) {
            channelPipeline.addLast("handler", consumer.getConfiguration().getHandler());
        } else {
            channelPipeline.addLast("handler", new ServerChannelHandler(consumer));
        }
         
        return channelPipeline;
    }
    
    private SslHandler configureServerSSLOnDemand() throws Exception {
        if (!consumer.getConfiguration().isSsl()) {
            return null;
        }

        if (consumer.getConfiguration().getSslHandler() != null) {
            return consumer.getConfiguration().getSslHandler();
        } else {
            SSLEngineFactory sslEngineFactory = 
                new SSLEngineFactory(consumer.getConfiguration().getKeyStoreFile(), 
                        consumer.getConfiguration().getTrustStoreFile(), 
                        consumer.getConfiguration().getPassphrase().toCharArray());
            SSLEngine sslEngine = sslEngineFactory.createServerSSLEngine();
            return new SslHandler(sslEngine);
        }
    }   

}
