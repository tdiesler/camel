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
package org.apache.camel.component.netty.http.handlers;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.camel.Exchange;
import org.apache.camel.component.netty.http.ContextPathMatcher;
import org.apache.camel.component.netty.http.DefaultContextPathMatcher;
import org.apache.camel.component.netty.http.HttpServerConsumerChannelFactory;
import org.apache.camel.component.netty.http.NettyHttpConsumer;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A multiplex {@link org.apache.camel.component.netty.http.HttpServerPipelineFactory} which keeps a list of handlers, and delegates to the
 * target handler based on the http context path in the incoming request. This is used to allow to reuse
 * the same Netty consumer, allowing to have multiple routes on the same netty {@link org.jboss.netty.bootstrap.ServerBootstrap}
 */
public class HttpServerMultiplexChannelHandler extends SimpleChannelUpstreamHandler implements HttpServerConsumerChannelFactory {

    // use NettyHttpConsumer as logger to make it easier to read the logs as this is part of the consumer
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpConsumer.class);
    private final ConcurrentMap<ContextPathMatcher, HttpServerChannelHandler> consumers = new ConcurrentHashMap<ContextPathMatcher, HttpServerChannelHandler>();
    private int port;
    private String token;
    private int len;

    public HttpServerMultiplexChannelHandler() {
        // must have default no-arg constructor to allow IoC containers to manage it
    }

    public void init(int port) {
        this.port = port;
        this.token = ":" + port;
        this.len = token.length();
    }

    public void addConsumer(NettyHttpConsumer consumer) {
        String path = pathAsKey(consumer.getConfiguration().getPath());
        ContextPathMatcher matcher = new DefaultContextPathMatcher(path, consumer.getConfiguration().isMatchOnUriPrefix());
        consumers.put(matcher, new HttpServerChannelHandler(consumer));
    }

    public void removeConsumer(NettyHttpConsumer consumer) {
        String path = pathAsKey(consumer.getConfiguration().getPath());
        ContextPathMatcher matcher = new DefaultContextPathMatcher(path, consumer.getConfiguration().isMatchOnUriPrefix());
        consumers.remove(matcher);
    }

    public int consumers() {
        return consumers.size();
    }

    public int getPort() {
        return port;
    }

    public ChannelHandler getChannelHandler() {
        return this;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        // store request, as this channel handler is created per pipeline
        HttpRequest request = (HttpRequest) messageEvent.getMessage();

        LOG.debug("Message received: {}", request);

        HttpServerChannelHandler handler = getHandler(request);
        if (handler != null) {
            // store handler as attachment
            ctx.setAttachment(handler);
            handler.messageReceived(ctx, messageEvent);
        } else {
            // this resource is not found, so send empty response back
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
            response.setHeader(Exchange.CONTENT_TYPE, "text/plain");
            response.setHeader(Exchange.CONTENT_LENGTH, 0);
            response.setContent(ChannelBuffers.copiedBuffer(new byte[]{}));
            messageEvent.getChannel().write(response).syncUninterruptibly();
            // close the channel after send error message
            messageEvent.getChannel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        HttpServerChannelHandler handler = (HttpServerChannelHandler) ctx.getAttachment();
        if (handler != null) {
            handler.exceptionCaught(ctx, e);
        } else {
            if (e.getCause() instanceof ClosedChannelException) {
                // The channel is closed so we do nothing here
                LOG.debug("Channel already closed. Ignoring this exception.");
                return;
            } else {
                if ("Broken pipe".equals(e.getCause().getMessage())) {
                    // Can't recover channel at this point. Only valid thing to do is close. A TCP RST is a possible cause for this.
                    // Note that trying to write to channel in this state will cause infinite recursion in netty 3.x
                    LOG.debug("Channel pipe is broken. Closing channel now.", e);                    
                    ctx.getChannel().close();
                } else {
                    // we cannot throw the exception here
                    LOG.warn("HttpServerChannelHandler is not found as attachment to handle exception, send 404 back to the client.", e.getCause());
                    // Now we just send 404 back to the client
                    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
                    response.headers().set(Exchange.CONTENT_TYPE, "text/plain");
                    response.headers().set(Exchange.CONTENT_LENGTH, 0);
                    // Here we don't want to expose the exception detail to the client
                    response.setContent(ChannelBuffers.copiedBuffer(new byte[]{}));                
                    
                    ctx.getChannel().write(response).syncUninterruptibly();
                    // close the channel after send error message
                    ctx.getChannel().close();
                }
            }
        }
    }

    private HttpServerChannelHandler getHandler(HttpRequest request) {
        // need to strip out host and port etc, as we only need the context-path for matching
        String path = request.getUri();
        int idx = path.indexOf(token);
        if (idx > -1) {
            path = path.substring(idx + len);
        }

        // use the path as key to find the consumer handler to use
        path = pathAsKey(path);

        // find the one that matches
        for (Map.Entry<ContextPathMatcher, HttpServerChannelHandler> entry : consumers.entrySet()) {
            if (entry.getKey().matches(path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String pathAsKey(String path) {
        // cater for default path
        if (path == null || path.equals("/")) {
            path = "";
        }

        // strip out query parameters
        int idx = path.indexOf('?');
        if (idx > -1) {
            path = path.substring(0, idx);
        }

        // strip of ending /
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return UnsafeUriCharactersEncoder.encodeHttpURI(path);
    }

}
