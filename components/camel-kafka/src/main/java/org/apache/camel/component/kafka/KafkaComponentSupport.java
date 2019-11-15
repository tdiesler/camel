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

package org.apache.camel.component.kafka;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class KafkaComponentSupport {


    static final Long KAFKA_NO_TIMESTAMP = null;

    static final Integer KAFKA_NO_PARTITION = null;

    static final Object KAFKA_NO_KEY = null;

    /**
     * The number of bytes used to represent a {@code double} value.
     */
    private static final int DOUBLE_BYTES = Double.SIZE / Byte.SIZE;

    /**
     * The number of bytes used to represent a {@code int} value in two's
     * complement binary form.
     */
    private static final int  INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    /**
     * The number of bytes used to represent a {@code long} value in two's
     * complement binary form.
     */
    private static final int  LONG_BYTES = Long.SIZE / Byte.SIZE;

    private static final Logger LOG = LoggerFactory.getLogger(KafkaComponentSupport.class);



    private KafkaComponentSupport() {
    }

    /**
     * Propagates headers from Kafka record to Camel Exchange
     *
     * @param record  the Kafka record
     * @param headerFilterStrategy the filter to be applied
     * @param exchange the context to perform propagation
     *
     */
    static void propagateHeaders(ConsumerRecord<Object, Object> record, Exchange exchange, HeaderFilterStrategy headerFilterStrategy) {
        for (Header header : record.headers()) {
            if (shouldBeFilteredOut(header, exchange, headerFilterStrategy)) {
                continue;
            }
            exchange.getIn().setHeader(header.key(), header.value());
        }
    }

    /**
     * Get Camel headers that have to be propagated to Kafka Record
     *
     * @param exchange  the Camel exchange
     * @param headerFilterStrategy the filter to be applied
     * @param exchange the context to perform propagation
     *
     */
    static List<Header> getPropagatedHeaders(Exchange exchange, HeaderFilterStrategy headerFilterStrategy) {

        Set<Map.Entry<String, Object>> entries = exchange.getIn().getHeaders().entrySet();
        List<Header> kafkaHeadersList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : entries) {
            if (shouldBeFilteredOut(entry, exchange, headerFilterStrategy)) {
                continue;
            }
            RecordHeader kafkaHeader = getRecordHeader(entry);

            if (kafkaHeader != null) {
                kafkaHeadersList.add(kafkaHeader);
            }
        }
        return kafkaHeadersList;
    }


    /**
     * Applies filtering logic to Kafka Message header that is
     * going to be copied to Camel Exchange.
     * <p/>
     * It returns <tt>true</tt> if the filtering logic return a match.
     * Otherwise, it returns <tt>false</tt>.
     * A match means the header should be excluded.
     *
     * @param header  the Kafka header
     * @param headerFilterStrategy the filter to be applied
     * @param exchange    the context to perform filtering
     * @return <tt>true</tt> if this header should be filtered out.
     */
    private static boolean shouldBeFilteredOut(Header header, Exchange exchange, HeaderFilterStrategy headerFilterStrategy) {
        return headerFilterStrategy.applyFilterToCamelHeaders(header.key(), header.value(), exchange);
    }

    /**
     * Applies filtering logic to Kafka Message header that is
     * going to be copied to Camel Exchange.
     * <p/>
     * It returns <tt>true</tt> if the filtering logic return a match.
     * Otherwise, it returns <tt>false</tt>.
     * A match means the header should be excluded.
     *
     * @param entry a key value tuple representing a single camel header
     * @param headerFilterStrategy the filter to be applied
     * @param exchange    the context to perform filtering
     * @return <tt>true</tt> if this header should be filtered out.
     */
    private static  boolean shouldBeFilteredOut(Map.Entry<String, Object> entry, Exchange exchange, HeaderFilterStrategy headerFilterStrategy) {
        return headerFilterStrategy.applyFilterToExternalHeaders(entry.getKey(), entry.getValue(), exchange);
    }

    private static RecordHeader getRecordHeader(Map.Entry<String, Object> entry) {
        byte[] headerValue = getHeaderValue(entry.getValue());
        if (headerValue == null) {
            return null;
        }
        return new RecordHeader(entry.getKey(), headerValue);
    }

    private static byte[] getHeaderValue(Object value) {
        if (value instanceof String) {
            return ((String) value).getBytes();
        } else if (value instanceof Long) {
            ByteBuffer buffer = ByteBuffer.allocate(LONG_BYTES);
            buffer.putLong((Long) value);
            return buffer.array();
        } else if (value instanceof Integer) {
            ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTES);
            buffer.putInt((Integer) value);
            return buffer.array();
        } else if (value instanceof Double) {
            ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_BYTES);
            buffer.putDouble((Double) value);
            return buffer.array();
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        }
        LOG.debug("Cannot propagate header value of type[{}], skipping... "
                + "Supported types: String, Integer, Long, Double, byte[].", value != null ? value.getClass() : "null");
        return null;
    }
}
