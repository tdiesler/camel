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
package org.apache.camel.component.http4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http4.handler.HeaderValidationHandler;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AUTH;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseContent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @version 
 */
public class HttpProxyServerWithSystemPropertiesTest extends BaseHttpTest {
    
    private HttpServer proxy;
    
    @Before
    @Override
    public void setUp() throws Exception {
        Map<String, String> expectedHeaders = new HashMap<String, String>();
        expectedHeaders.put("Proxy-Connection", "Keep-Alive");
        proxy = ServerBootstrap.bootstrap().
                setHttpProcessor(getBasicHttpProcessor()).
                setConnectionReuseStrategy(getConnectionReuseStrategy()).
                setResponseFactory(getHttpResponseFactory()).
                setExpectationVerifier(getHttpExpectationVerifier()).
                setSslContext(getSSLContext()).
                registerHandler("*", new HeaderValidationHandler("GET", null, null, getExpectedContent(), expectedHeaders)).create();
        proxy.start();

        // see https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6737819
        System.setProperty("http.nonProxyHosts", "");

        System.getProperties().setProperty("http.proxyHost", getProxyHost());
        System.getProperties().setProperty("http.proxyPort", String.valueOf(getProxyPort()));

        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (proxy != null) {
            proxy.stop();
        }
    }
    
    @Override
    protected HttpProcessor getBasicHttpProcessor() {
        List<HttpRequestInterceptor> requestInterceptors = new ArrayList<HttpRequestInterceptor>();
        requestInterceptors.add(new RequestProxyBasicAuth());
        List<HttpResponseInterceptor> responseInterceptors = new ArrayList<HttpResponseInterceptor>();
        responseInterceptors.add(new ResponseContent());
        responseInterceptors.add(new ResponseProxyBasicUnauthorized());
        ImmutableHttpProcessor httpproc = new ImmutableHttpProcessor(requestInterceptors, responseInterceptors);
        return httpproc;
    }
    

    @Test
    public void httpGetWithProxyFromSystemProperties() throws Exception {

        Exchange exchange = template.request("http4://" + getProxyHost() + ":" + getProxyPort() + "?useSystemProperties=true", new Processor() {
            public void process(Exchange exchange) throws Exception {
            }
        });

        assertExchange(exchange);
    }

    private String getProxyHost() {
        return proxy.getInetAddress().getHostName();
    }

    private int getProxyPort() {
        return proxy.getLocalPort();
    }
    
    class RequestProxyBasicAuth implements HttpRequestInterceptor {
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            String auth = null;
            
            String requestLine = request.getRequestLine().toString();
            // assert we set a write GET URI
            if (requestLine.contains("http4://localhost")) {
                throw new HttpException("Get a wrong proxy GET url");
            }
            Header h = request.getFirstHeader(AUTH.PROXY_AUTH_RESP);
            if (h != null) {
                String s = h.getValue();
                if (s != null) {
                    auth = s.trim();
                }
            }

            if (auth != null) {
                int i = auth.indexOf(' ');
                if (i == -1) {
                    throw new ProtocolException("Invalid Authorization header: " + auth);
                }
                String authscheme = auth.substring(0, i);
                if (authscheme.equalsIgnoreCase("basic")) {
                    String s = auth.substring(i + 1).trim();
                    byte[] credsRaw = s.getBytes("ASCII");
                    BinaryDecoder codec = new Base64();
                    try {
                        String creds = new String(codec.decode(credsRaw), "ASCII");
                        context.setAttribute("proxy-creds", creds);
                    } catch (DecoderException ex) {
                        throw new ProtocolException("Malformed BASIC credentials");
                    }
                }
            }
        }
    }

    class ResponseProxyBasicUnauthorized implements HttpResponseInterceptor {
        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
                response.addHeader(AUTH.PROXY_AUTH, "Basic realm=\"test realm\"");
            }
        }
    }
}