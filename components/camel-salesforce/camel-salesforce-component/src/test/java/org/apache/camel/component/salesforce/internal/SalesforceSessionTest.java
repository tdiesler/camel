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
package org.apache.camel.component.salesforce.internal;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.camel.component.salesforce.SalesforceLoginConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.eclipse.jetty.client.CachedExchange;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SalesforceSessionTest {

    @Test
    public void shouldUseTheOverridenInstanceUrl() throws Exception {
        final SalesforceLoginConfig config = new SalesforceLoginConfig("https://login.salesforce.com", "clientId",
            "clientSecret", "username", "password", true);
        config.setInstanceUrl("https://custom.salesforce.com:8443");

        final SalesforceSession session = login(config);

        assertEquals("https://custom.salesforce.com:8443", session.getInstanceUrl());
    }

    @Test
    public void shouldUseTheSalesforceSuppliedInstanceUrl() throws Exception {
        final SalesforceLoginConfig config = new SalesforceLoginConfig("https://login.salesforce.com", "clientId",
            "clientSecret", "username", "password", true);

        final SalesforceSession session = login(config);

        assertEquals("https://eu11.salesforce.com", session.getInstanceUrl());
    }

    static SalesforceSession login(final SalesforceLoginConfig config) throws Exception {
        final HttpClient client = mock(HttpClient.class);

        final ArgumentCaptor<ContentExchange> exchangeArgument = ArgumentCaptor.forClass(ContentExchange.class);

        final Future<SalesforceSession> session = Executors.newFixedThreadPool(1)
            .submit(new Callable<SalesforceSession>() {
                public SalesforceSession call() throws SalesforceException {
                    final SalesforceSession session = new SalesforceSession(client, config);
                    session.login(null);

                    return session;
                }
            });

        // this is necessary so that client.send(exchange) is invoked on
        // separate thread no way of knowing when that happens, 250msec ought
        // to be enough
        Thread.sleep(250);

        verify(client).send(exchangeArgument.capture());

        final ContentExchange contentExchange = exchangeArgument.getValue();
        final Method setStatus = HttpExchange.class.getDeclaredMethod("setStatus", int.class);
        setStatus.setAccessible(true);
        // we need to pass through all states in order for the exchange to be
        // 'done'
        setStatus.invoke(contentExchange, HttpExchange.STATUS_WAITING_FOR_COMMIT);
        setStatus.invoke(contentExchange, HttpExchange.STATUS_SENDING_REQUEST);
        setStatus.invoke(contentExchange, HttpExchange.STATUS_WAITING_FOR_RESPONSE);
        setStatus.invoke(contentExchange, HttpExchange.STATUS_PARSING_HEADERS);
        setStatus.invoke(contentExchange, HttpExchange.STATUS_PARSING_CONTENT);
        setStatus.invoke(contentExchange, HttpExchange.STATUS_COMPLETED);

        final Method onResponseStatus = CachedExchange.class.getDeclaredMethod("onResponseStatus", Buffer.class,
            int.class, Buffer.class);
        onResponseStatus.setAccessible(true);
        onResponseStatus.invoke(contentExchange, null, 200, new ByteArrayBuffer("Success"));

        final Method onResponseContent = ContentExchange.class.getDeclaredMethod("onResponseContent", Buffer.class);
        onResponseContent.setAccessible(true);
        onResponseContent.invoke(contentExchange,
            new ByteArrayBuffer("{\"instance_url\": \"https://eu11.salesforce.com\"}"));

        final Method done = HttpExchange.class.getDeclaredMethod("done");
        done.setAccessible(true);
        done.invoke(contentExchange);

        return session.get();
    }
}
