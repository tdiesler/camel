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
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Test;

public class HttpProducerContentTypeTest extends BaseHttpTest {

    private static final String CONTENT_TYPE = "multipart/form-data;boundary=---------------------------j2radvtrk";
    @Test
    public void testContentTypeWithBoundary() throws Exception {
        Exchange out = template.request("http4://" + getHostName() + ":" + getPort() + "/content", new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE);
                exchange.getIn().setBody("This is content");
            }
            
        });

        assertNotNull(out);
        assertFalse("Should not fail", out.isFailed());
        assertEquals(CONTENT_TYPE, out.getOut().getBody(String.class));
        
    }

    @Override
    protected void registerHandler(LocalTestServer server) {
        server.register("/content", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                String contentType = request.getFirstHeader(Exchange.CONTENT_TYPE).getValue();
                
                assertEquals(CONTENT_TYPE, contentType);

                response.setEntity(new StringEntity(contentType, "ASCII"));
                response.setStatusCode(HttpStatus.SC_OK);
            }
        });
    }
}