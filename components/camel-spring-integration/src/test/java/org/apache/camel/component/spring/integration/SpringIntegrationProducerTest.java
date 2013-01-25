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
package org.apache.camel.component.spring.integration;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringIntegrationProducerTest extends CamelSpringTestSupport {

    @Test
    public void testSendingTwoWayMessage() throws Exception {
        String result = template.requestBody("direct:twowayMessage", "Willem", String.class);

        assertEquals("Can't get the right response", result, "Hello Willem");
    }

    @Test
    public void testSendingOneWayMessage() throws Exception {
        template.sendBody("direct:onewayMessage", "Greet");

        HelloWorldService service = getMandatoryBean(HelloWorldService.class, "helloService");
        assertEquals("We should call the service", service.getGreetName(), "Greet");        
    }

    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/component/spring/integration/producer.xml");
    }
}
