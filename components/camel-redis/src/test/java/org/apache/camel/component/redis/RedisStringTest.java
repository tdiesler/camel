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
package org.apache.camel.component.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.impl.JndiRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedisStringTest extends RedisTestSupport {
    private ValueOperations valueOperations;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        JndiRegistry registry = super.createRegistry();
        registry.bind("redisTemplate", redisTemplate);
        return registry;
    }

    @Before
    public void setUp() throws Exception {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        super.setUp();
    }

    @Test
    public void shouldExecuteSET() throws Exception {
        Object result = sendHeaders(
                RedisConstants.COMMAND, "SET",
                RedisConstants.KEY, "key",
                RedisConstants.VALUE, "value");

        verify(valueOperations).set("key", "value");
    }

    @Test
    public void shouldExecuteSETNX() throws Exception {
        Object result = sendHeaders(
                RedisConstants.COMMAND, "SETNX",
                RedisConstants.KEY, "key",
                RedisConstants.VALUE, "value");

        verify(valueOperations).setIfAbsent("key", "value");
    }


    @Test
    public void shouldExecuteSETEX() throws Exception {
        Object result = sendHeaders(
                RedisConstants.COMMAND, "SETEX",
                RedisConstants.KEY, "key",
                RedisConstants.TIMEOUT, "10",
                RedisConstants.VALUE, "value");

        verify(valueOperations).set("key", "value", 10, TimeUnit.SECONDS);
    }


    @Test
    public void shouldExecuteSETRANGE() throws Exception {
        Object result = sendHeaders(
                RedisConstants.COMMAND, "SETRANGE",
                RedisConstants.KEY, "key",
                RedisConstants.OFFSET, "10",
                RedisConstants.VALUE, "value");

        verify(valueOperations).set("key", "value", 10);
    }


    @Test
    public void shouldExecuteGETRANGE() throws Exception {
        when(valueOperations.get(anyString(), anyLong(), anyLong())).thenReturn("test");

        Object result = sendHeaders(
                RedisConstants.COMMAND, "GETRANGE",
                RedisConstants.KEY, "key",
                RedisConstants.START, "2",
                RedisConstants.END, "4");

        verify(valueOperations).get("key", 2, 4);
        assertEquals("test", result);
    }


    @Test
    public void shouldExecuteSETBIT() throws Exception {
        Object result = sendHeaders(
                RedisConstants.COMMAND, "SETBIT",
                RedisConstants.KEY, "key",
                RedisConstants.OFFSET, "10",
                RedisConstants.VALUE, "0");

        verify(redisTemplate).execute(any(RedisCallback.class));
    }


    @Test
    public void shouldExecuteGETBIT() throws Exception {
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(true);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "GETBIT",
                RedisConstants.KEY, "key",
                RedisConstants.OFFSET, "2");

        verify(redisTemplate).execute(any(RedisCallback.class));
        assertEquals(true, result);
    }

    @Test
    public void shouldExecuteGET() throws Exception {
        when(valueOperations.get("key")).thenReturn("value");

        Object result = sendHeaders(
                RedisConstants.COMMAND, "GET",
                RedisConstants.KEY, "key");

        verify(valueOperations).get("key");
        assertEquals("value", result);
    }

    @Test
    public void shouldExecuteAPPEND() throws Exception {
        when(valueOperations.append(anyString(), anyString())).thenReturn(5);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "APPEND",
                RedisConstants.KEY, "key",
                RedisConstants.VALUE, "value");

        verify(valueOperations).append("key", "value");
        assertEquals(5, result);
    }

    @Test
    public void shouldExecuteDECR() throws Exception {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(2L);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "DECR",
                RedisConstants.KEY, "key");

        verify(valueOperations).increment("key", -1);
        assertEquals(2L, result);
    }

    @Test
    public void shouldExecuteDECRBY() throws Exception {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "DECRBY",
                RedisConstants.VALUE, "2",
                RedisConstants.KEY, "key");

        verify(valueOperations).increment("key", -2);
        assertEquals(1L, result);
    }

    @Test
    public void shouldExecuteINCR() throws Exception {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(2L);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "INCR",
                RedisConstants.KEY, "key");

        verify(valueOperations).increment("key", 1);
        assertEquals(2L, result);
    }

    @Test
    public void shouldExecuteINCRBY() throws Exception {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "INCRBY",
                RedisConstants.VALUE, "2",
                RedisConstants.KEY, "key");

        verify(valueOperations).increment("key", 2);
        assertEquals(1L, result);
    }


    @Test
    public void shouldExecuteSTRLEN() throws Exception {
        when(valueOperations.size(anyString())).thenReturn(5L);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "STRLEN",
                RedisConstants.KEY, "key");

        verify(valueOperations).size("key");
        assertEquals(5L, result);
    }


    @Test
    public void shouldExecuteMGET() throws Exception {
        List<String> fields = new ArrayList<String>();
        fields.add("field1");

        List<String> values = new ArrayList<String>();
        values.add("value1");

        when(valueOperations.multiGet(fields)).thenReturn(values);

        Object result = sendHeaders(
                RedisConstants.COMMAND, "MGET",
                RedisConstants.FIELDS, fields);

        verify(valueOperations).multiGet(fields);
        assertEquals(values, result);
    }


    @Test
    public void shouldExecuteMSET() throws Exception {
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("field1", "valu1");

        Object result = sendHeaders(
                RedisConstants.COMMAND, "MSET",
                RedisConstants.VALUES, values);

        verify(valueOperations).multiSet(values);
    }


    @Test
    public void shouldExecuteMSETNX() throws Exception {
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("field1", "valu1");

        Object result = sendHeaders(
                RedisConstants.COMMAND, "MSETNX",
                RedisConstants.VALUES, values);

        verify(valueOperations).multiSetIfAbsent(values);
    }

    @Test
    public void shouldExecuteGETSET() throws Exception {
        when(valueOperations.getAndSet(anyString(), anyString())).thenReturn("old value");
        String value = "new value";

        Object result = sendHeaders(
                RedisConstants.COMMAND, "GETSET",
                RedisConstants.KEY, "key",
                RedisConstants.VALUE, value);

        verify(valueOperations).getAndSet("key", value);
        assertEquals("old value", result);

    }


//
//    @Test
//        public void shouldExecuteMULTI() throws Exception {
//            //when(valueOperations.multi()).thenReturn(any(Transaction.class));
//
//            Object result = sendHeaders(RedisConstants.COMMAND, "MULTI");
//
//            verify(jedis).multi();
//            assertEquals("OK", result);
//        }
//
//        @Test
//        public void shouldExecuteInTransaction() throws Exception {
//            Transaction transaction = mock(Transaction.class);
//            when(jedis.multi()).thenReturn(transaction);
//
//            sendHeaders(RedisConstants.COMMAND, "MULTI");
//
//
//            Object result = sendHeaders(
//                    RedisConstants.KEY, "key",
//                    RedisConstants.VALUE, "value");
//
//            verify(transaction).set("key", "value");
//            assertEquals("OK", result);
//
//
//        }
}
