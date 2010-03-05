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
package org.apache.camel.component.jetty.jettyproducer;

import java.io.IOException;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class JettyHttpProducerAsyncTest extends CamelTestSupport {

    protected LocalTestServer localServer;

    @Before
    @Override
    public void setUp() throws Exception {
        localServer = new LocalTestServer(null, null);
        localServer.register("/", new HttpRequestHandler() {
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
                if (!"GET".equals(request.getRequestLine().getMethod())) {
                    response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
                    return;
                }

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity("google", HTTP.ISO_8859_1));
            }
        });
        localServer.start();

        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (localServer != null) {
            localServer.stop();
        }
    }

    @Test
    public void testGoogleFrontPageAsync() throws Exception {
        // these tests does not run well on Windows
        if (isPlatform("windows")) {
            return;
        }

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).body(String.class).contains("google");

        template.sendBody("direct:start", null);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        // to prevent redirect being thrown as an exception
                        .toAsync("jetty://http://" + localServer.getServiceHostName() + ":" + localServer.getServicePort() + "?throwExceptionOnFailure=false")
                        .to("mock:result");
            }
        };
    }
}