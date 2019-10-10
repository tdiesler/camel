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
package org.apache.camel.component.netty4.http;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.Exchange;

public final class NettyHttpStreamHelper {
    private NettyHttpStreamHelper() {

    }

    public static void processStream(Exchange exchange) throws Exception {
        InputStream is = exchange.getIn().getBody(InputStream.class);

        byte[] buffer = new byte[1024];
        long read = 0;
        long total = 0;
        while ((read = is.read(buffer, 0, buffer.length)) != -1) {
            total += read;
        }

        exchange.getIn().setBody(new Long(total));
    }

    public static CompletableFuture<Void> asyncProcessStream(Exchange exchange) {
        return CompletableFuture.runAsync(() -> {
            try {
                processStream(exchange);
            } catch (Exception e) {
                exchange.setException(e);
            }
        });
    }

    public static void prepareStream(Exchange exchange) throws Exception {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        exchange.getIn().setBody(pis);

        StreamWriter sw = new StreamWriter(pos, NettyHttpStreamTest.SIZE);
        sw.start();
    }
}
