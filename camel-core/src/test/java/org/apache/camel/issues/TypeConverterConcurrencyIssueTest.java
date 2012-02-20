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
package org.apache.camel.issues;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.impl.converter.StaticMethodTypeConverter;
import org.apache.camel.util.StopWatch;

/**
 * Testing for CAMEL-5002
 */
public class TypeConverterConcurrencyIssueTest extends ContextTestSupport {

    private int size = 100 * 1000;
    
    public void testTypeConverter() throws Exception {
        // add as type converter
        Method method = TypeConverterConcurrencyIssueTest.class.getMethod("toMyCamelBean", String.class);
        assertNotNull(method);
        context.getTypeConverterRegistry().addTypeConverter(MyCamelBean.class, String.class, new StaticMethodTypeConverter(method));

        ExecutorService pool = context.getExecutorServiceManager().newThreadPool(this, "test", 50, 50);
        final CountDownLatch latch = new CountDownLatch(size);

        StopWatch watch = new StopWatch();
        for (int i = 0; i < size; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    context.getTypeConverter().convertTo(MyCamelBean.class, "1;MyCamel");
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        log.info("Took " + watch.stop() + " millis to convert " + size + " objects");
    }
    
    public static MyCamelBean toMyCamelBean(String body) {
        MyCamelBean bean = new MyCamelBean();
        String[] data = body.split(";");
        bean.setId(Integer.parseInt(data[0]));
        bean.setName(data[1]);
        return bean;
    }

}
