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
package org.apache.camel.processor.aggregate.cassandra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultExchangeHolder;

/**
 * Marshall/unmarshall Exchange to/from a ByteBuffer.
 * Inspired from JdbcCamelCodec.
 */
public class CassandraCamelCodec {

    // JDK8
    private static Method staticSetObjectInputFilter;
    // JDK9+
    private static Method setObjectInputFilter;
    // common
    private static Method createFilter;

    static {
        Method[] methods = ObjectInputStream.class.getMethods();
        Method setMethod = null;
        Method getMethod = null;
        for (final Method method : methods) {
            if (method.getName().equals("setObjectInputFilter")) {
                // JDK9+
                setObjectInputFilter = method;
            }
        }
        try {
            Class<?> jdk8OIFConfigClass = CassandraCamelCodec.class.getClassLoader().loadClass("sun.misc.ObjectInputFilter$Config");
            for (Method method : jdk8OIFConfigClass.getMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("setObjectInputFilter")) {
                    staticSetObjectInputFilter = method;
                    staticSetObjectInputFilter.setAccessible(true);
                } else if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("createFilter")) {
                    createFilter = method;
                    createFilter.setAccessible(true);
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> jdk9PlusOIFConfigClass = CassandraCamelCodec.class.getClassLoader().loadClass("java.io.ObjectInputFilter$Config");
            for (Method method : jdk9PlusOIFConfigClass.getMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("createFilter")) {
                    createFilter = method;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public ByteBuffer marshallExchange(CamelContext camelContext, Exchange exchange, boolean allowSerializedHeaders) throws IOException {
        // use DefaultExchangeHolder to marshal to a serialized object
        DefaultExchangeHolder pe = DefaultExchangeHolder.marshal(exchange, false, allowSerializedHeaders);
        // add the aggregated size and timeout property as the only properties we want to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_SIZE, exchange.getProperty(Exchange.AGGREGATED_SIZE, Integer.class));
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_TIMEOUT, exchange.getProperty(Exchange.AGGREGATED_TIMEOUT, Long.class));
        // add the aggregated completed by property to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_COMPLETED_BY, exchange.getProperty(Exchange.AGGREGATED_COMPLETED_BY, String.class));
        // add the aggregated correlation key property to retain
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_CORRELATION_KEY, exchange.getProperty(Exchange.AGGREGATED_CORRELATION_KEY, String.class));
        // and a guard property if using the flexible toolbox aggregator
        DefaultExchangeHolder.addProperty(pe, Exchange.AGGREGATED_COLLECTION_GUARD, exchange.getProperty(Exchange.AGGREGATED_COLLECTION_GUARD, String.class));
        // persist the from endpoint as well
        if (exchange.getFromEndpoint() != null) {
            DefaultExchangeHolder.addProperty(pe, "CamelAggregatedFromEndpoint", exchange.getFromEndpoint().getEndpointUri());
        }
        return ByteBuffer.wrap(serialize(pe));
    }

    public Exchange unmarshallExchange(CamelContext camelContext, ByteBuffer buffer, String deserializationFilter) throws IOException, ClassNotFoundException {
        DefaultExchangeHolder pe = (DefaultExchangeHolder) deserialize(new ByteBufferInputStream(buffer), deserializationFilter);
        Exchange answer = new DefaultExchange(camelContext);
        DefaultExchangeHolder.unmarshal(answer, pe);
        // restore the from endpoint
        String fromEndpointUri = (String) answer.removeProperty("CamelAggregatedFromEndpoint");
        if (fromEndpointUri != null) {
            Endpoint fromEndpoint = camelContext.hasEndpoint(fromEndpointUri);
            if (fromEndpoint != null) {
                answer.setFromEndpoint(fromEndpoint);
            }
        }
        return answer;
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
        objectOut.writeObject(object);
        objectOut.close();
        return bytesOut.toByteArray();
    }

    private Object deserialize(InputStream bytes, String deserializationFilter) throws IOException, ClassNotFoundException {
        ObjectInputStream objectIn = new ObjectInputStream(bytes);

        try {
            String version = System.getProperty("java.specification.version");
            if (version == null || version.contains(".")) {
                // assume it's JDK 8
                if (createFilter != null) {
                    Object filter = createFilter.invoke(null, deserializationFilter);
                    if (staticSetObjectInputFilter != null) {
                        staticSetObjectInputFilter.invoke(null, objectIn, filter);
                    }
                }
            } else {
                // JDK9+
                if (createFilter != null) {
                    Object filter = createFilter.invoke(null, deserializationFilter);
                    if (setObjectInputFilter != null) {
                        setObjectInputFilter.invoke(objectIn, filter);
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Object object = objectIn.readObject();
        objectIn.close();
        return object;
    }

    private static class ByteBufferInputStream extends InputStream {

        private final ByteBuffer buffer;

        ByteBufferInputStream(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int read() throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }
            return buffer.get();
        }

        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }
            len = Math.min(len, buffer.remaining());
            buffer.get(bytes, off, len);
            return len;
        }
    }
}
