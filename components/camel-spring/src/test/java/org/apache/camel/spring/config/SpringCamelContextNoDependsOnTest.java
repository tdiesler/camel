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
package org.apache.camel.spring.config;

import org.apache.camel.spring.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class SpringCamelContextNoDependsOnTest extends SpringTestSupport {

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/config/SpringCamelContextNoDependsOnTest.xml");
    }

    public void testNoDependsOn() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        // in this example CamelContext is created first, then route builder, and then the depends on bean is last
        long time1 = context.getRegistry().lookup("myDependsOnBean", MyDependsOnBean.class).getTime();
        long time2 = context.getRegistry().lookup("myRouteBuilder", MyDependsOnRouteBuilder.class).getTime();

        assertTrue("myDependsOnBean should NOT be created before myRouteBuilder", time1 > time2);
    }

}