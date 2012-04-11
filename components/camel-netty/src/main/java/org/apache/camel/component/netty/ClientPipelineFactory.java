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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * Factory to create {@link ChannelPipeline} for clients, eg {@link NettyProducer}.
 * <p/>
 * Implementators should use implement the {@link #getPipeline(NettyProducer)} method.
 *
 * @see ChannelPipelineFactory
 */
public abstract class ClientPipelineFactory implements ChannelPipelineFactory {

    public ClientPipelineFactory() {
    }

    /**
     * Returns a newly created {@link ChannelPipeline}.
     *
     * @param producer the netty producer
     */
    public abstract ChannelPipeline getPipeline(NettyProducer producer) throws Exception;

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        throw new UnsupportedOperationException("use getPipeline(NettyProducer) instead");
    }
}
