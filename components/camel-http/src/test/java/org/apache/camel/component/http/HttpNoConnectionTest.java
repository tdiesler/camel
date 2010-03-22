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
package org.apache.camel.component.http;

import java.net.ConnectException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.http.handler.BasicValidationHandler;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.localserver.LocalTestServer;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class HttpNoConnectionTest extends BaseHttpTest {

    @Test
    public void httpConnectionOk() throws Exception {
        Exchange exchange = template.request("http://" + getHostName() + ":" + getPort() + "/search", new Processor() {
            public void process(Exchange exchange) throws Exception {
            }
        });

        assertExchange(exchange);
    }

    @Test
    public void httpConnectionNotOk() throws Exception {
        String url = "http://" + getHostName() + ":" + getPort() + "/search";
        // stop server so there are no connection
        localServer.stop();

        try {
            template.request(url, null);
            fail("Should have thrown an exception");
        } catch (RuntimeCamelException e) {
            HttpHostConnectException cause = assertIsInstanceOf(HttpHostConnectException.class, e.getCause());
            assertIsInstanceOf(ConnectException.class, cause.getCause());
        }
    }

    @Override
    protected void registerHandler(LocalTestServer server) {
        server.register("/search", new BasicValidationHandler("GET", null, null, getExpectedContent()));
    }

}
