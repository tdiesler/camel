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
package org.apache.camel.component.bean;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @version 
 */
@ContextConfiguration
public class BeanWithRecipientListTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    protected ProducerTemplate template;
    @EndpointInject(uri = "mock:a")
    protected MockEndpoint a;
    @EndpointInject(uri = "mock:b")
    protected MockEndpoint b;

    protected String body = "James";

    @Test
    public void testSendBody() throws Exception {
        a.expectedBodiesReceived(body);
        b.expectedBodiesReceived(body);

        template.sendBody("direct:start", body);

        MockEndpoint.assertIsSatisfied(a, b);
    }
}