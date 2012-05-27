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
package org.apache.camel.spring.example;

import org.apache.camel.spring.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version 
 */
public class SpringExpressionTrimTest extends SpringTestSupport {

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/example/expressionTrim.xml");
    }

    public void testDefault() throws Exception {
        String out = template.requestBody("direct:a", "World", String.class);
        assertEquals("Hello World", out);
    }

    public void testTrim() throws Exception {
        String out = template.requestBody("direct:b", "World", String.class);
        assertEquals("Hello World", out);
    }

    public void testNoTrim() throws Exception {
        String out = template.requestBody("direct:c", "World", String.class);
        // the xml has whitespace noise which is not trimmed
        assertEquals("\n          Hello World\n        ", out);
    }

}
