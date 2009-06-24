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
package org.apache.camel.itest.mail;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * Unit testing Mail configured using spring bean
 */
@ContextConfiguration
public class SpringMailEndpointTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected ProducerTemplate template;
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Test
    public void testMailEndpointAsSpringBean() throws Exception {
        Mailbox.clearAll();

        String body = "Hello Claus.\nYes it does.\n\nRegards James.";

        result.expectedBodiesReceived(body);
        result.expectedHeaderReceived("subject", "Hello Camel");

        template.sendBodyAndHeader("smtp://james2@localhost", body, "subject", "Hello Camel");

        result.assertIsSatisfied();
    }

}