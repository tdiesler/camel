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

package org.apache.camel.web.groovy;

import org.junit.Test;

/**
 *
 */
public class BeanDSLTest extends GroovyRendererTestSupport {

    @Test
    public void testBeanMethodHeartbeat() throws Exception {
        String dsl = "from(\"bean:beanService?method=status\").to(\"mock:result\")";
        assertEquals(dsl, render(dsl));
    }

    @Test
    public void testBeanRef() throws Exception {
        String dsl = "from(\"direct:start\").beanRef(\"myBean\").to(\"mock:result\")";
        assertEquals(dsl, render(dsl));
    }

    @Test
    public void testBeanRecipient() throws Exception {
        String dsl = "from(\"direct:start\").beanRef(\"beanRecipient\", \"recipientList\")";
        assertEquals(dsl, render(dsl));
    }

    @Test
    public void testBeanWithException() throws Exception {
        String dsl = "errorHandler(deadLetterChannel(\"mock://error\"));onException(Exception.class).to(\"mock:invalid\");from(\"direct:start\").beanRef(\"myBean\").to(\"mock:valid\")";
        String expect = "errorHandler(deadLetterChannel(\"mock://error\").logStackTrace(true));onException(Exception.class).to(\"mock:invalid\");"
            + "from(\"direct:start\").beanRef(\"myBean\").to(\"mock:valid\")";
        assertEquals(expect, render(dsl));
    }
}
