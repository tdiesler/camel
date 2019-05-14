/*
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
package org.apache.camel.component.netty4.http;

import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.junit.Test;


public class NettyHttpBindingUseAbsolutePath extends BaseNettyTest {

    @Test
    public void testSendToNettyWithPath() throws Exception {
        Exchange exchange = template.request("netty4-http:http://localhost:{{port}}/mypath?useRelativePath=false", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
            }
        });

        // convert the response to a String
        String body = exchange.getOut().getBody(String.class);
        assertEquals("Request message is OK", body);
    }

    @Test
    public void testSendToNettyWithoutPath() throws Exception {
        Exchange exchange = template.request("netty4-http:http://localhost:{{port}}?useRelativePath=false", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
            }
        });

        // convert the response to a String
        String body = exchange.getOut().getBody(String.class);
        assertEquals("Request message is OK", body);
    }

    @Test
    public void testSendToNettyWithoutPath2() throws Exception {
        Exchange exchange = template.request("netty4-http:http://localhost:{{port}}/?useRelativePath=false", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
            }
        });

        // convert the response to a String
        String body = exchange.getOut().getBody(String.class);
        assertEquals("Request message is OK", body);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("netty4-http:http://localhost:{{port}}/mypath").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        assertEquals("Get a wrong form parameter from the message header", "localhost:" + getPort(), exchange.getIn().getHeader("host"));

                        NettyHttpMessage in = (NettyHttpMessage) exchange.getIn();
                        FullHttpRequest request = in.getHttpRequest();
                        assertEquals("Relative path not used in POST", "http://localhost:" + getPort() + "/mypath", request.uri());

                        // send a response
                        exchange.getOut().getHeaders().clear();
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
                        exchange.getOut().setBody("Request message is OK");
                    }
                });

                from("netty4-http:http://localhost:{{port}}").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        assertEquals("Get a wrong form parameter from the message header", "localhost:" + getPort(), exchange.getIn().getHeader("host"));

                        NettyHttpMessage in = (NettyHttpMessage) exchange.getIn();
                        FullHttpRequest request = in.getHttpRequest();
                        assertEquals("Relative path not used in POST", "http://localhost:" + getPort() + "/", request.uri());

                        // send a response
                        exchange.getOut().getHeaders().clear();
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
                        exchange.getOut().setBody("Request message is OK");
                    }
                });
            }
        };
    }

}
