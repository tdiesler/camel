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
package org.apache.camel.component.jetty;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.FallbackConverter;
import org.apache.camel.StreamCache;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
@Converter
public final class JettyConverter {

    private static final Log LOG = LogFactory.getLog(JettyConverter.class);

    private JettyConverter() {
    }

    @FallbackConverter
    @SuppressWarnings("unchecked")
    public static Object convertTo(Class<?> type, Exchange exchange, Object value, TypeConverterRegistry registry) throws Exception {
        // do not convert to stream cache
        if (StreamCache.class.isAssignableFrom(value.getClass())) {
            return null;
        }

        if (JettyHttpMessage.class.isAssignableFrom(value.getClass())) {
            JettyHttpMessage message = (JettyHttpMessage) value;

            Object body = message.getBody();
            if (body == null) {
                return null;
            }

            if (body instanceof JettyFutureGetBody) {
                JettyFutureGetBody future = (JettyFutureGetBody) body;

                if (future.isCancelled()) {
                    // return void to indicate its not possible to convert at this time
                    return Void.TYPE;
                }

                // do some trace logging as the get is blocking until the response is ready
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Getting future response");
                }

                Object reply = future.get();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Got future response");
                }

                if (reply == null) {
                    // return void to indicate its not possible to convert at this time
                    return Void.TYPE;
                }

                // maybe from is already the type we want
                if (type.isAssignableFrom(reply.getClass())) {
                    return type.cast(reply);
                }

                // no then try to lookup a type converter
                TypeConverter tc = registry.lookup(type, reply.getClass());
                if (tc != null) {
                    return tc.convertTo(type, exchange, reply);
                }
            } else {
                // no then try to lookup a type converter
                TypeConverter tc = registry.lookup(type, body.getClass());
                if (tc != null) {
                    return tc.convertTo(type, exchange, body);
                }
            }
        }

        return null;
    }

}
