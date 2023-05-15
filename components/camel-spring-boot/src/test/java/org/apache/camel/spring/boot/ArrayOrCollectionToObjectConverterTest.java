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
package org.apache.camel.spring.boot;

import com.example.Dummy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.util.ExchangeHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CamelAutoConfiguration.class},
        properties = {"camel.springboot.main-run-controller = true"})
public class ArrayOrCollectionToObjectConverterTest extends Assert {

    /*
    ENTESB-21311: SpringFramework caches a missed TypeConverter and user can not clean it
    */
    
    @Autowired
    private CamelContext context;

    @Test
    public void collectionToObjectTest() {
        Exchange exchange = new DefaultExchange(context);
        List<Dummy> value = new ArrayList<>();
        Dummy dummy = new Dummy();
        value.add(dummy);
        Dummy converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertEquals(dummy, converted);

        value = new ArrayList<>();
        converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertNull(converted);

        value.add(dummy);
        converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertEquals(dummy, converted);
    }

    @Test
    public void arrayToObjectTest() {
        Exchange exchange = new DefaultExchange(context);
        Dummy[] value = new Dummy[1];
        Dummy dummy = new Dummy();
        value[0] = dummy;
        Dummy converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertEquals(dummy, converted);

        value =  new Dummy[0];
        converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertNull(converted);

        value = new Dummy[1];
        value[0] = dummy;
        converted = ExchangeHelper.convertToType(exchange, Dummy.class, value);
        assertEquals(dummy, converted);
    }

}
