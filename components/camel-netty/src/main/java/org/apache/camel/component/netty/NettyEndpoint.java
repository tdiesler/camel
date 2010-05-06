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

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class NettyEndpoint extends DefaultEndpoint {
    private NettyConfiguration configuration;

    public NettyEndpoint(String endpointUri, Component component, NettyConfiguration configuration) {
        super(endpointUri, component);
        this.configuration = configuration;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new NettyConsumer(this, processor, configuration);
    }

    public Producer createProducer() throws Exception {
        return new NettyProducer(this, configuration);
    }

    public Exchange createExchange(ChannelHandlerContext ctx, MessageEvent messageEvent) {
        Exchange exchange = createExchange();
        exchange.getIn().setHeader(NettyConstants.NETTY_CHANNEL_HANDLER_CONTEXT, ctx);
        exchange.getIn().setHeader(NettyConstants.NETTY_MESSAGE_EVENT, messageEvent);
        return exchange;        
    }
    
    public boolean isSingleton() {
        return true;
    }

    public NettyConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(NettyConfiguration configuration) {
        this.configuration = configuration;
    }

    
}