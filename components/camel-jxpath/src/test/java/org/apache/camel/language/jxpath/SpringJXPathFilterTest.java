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
package org.apache.camel.language.jxpath;

import org.apache.camel.CamelTemplate;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * @version $Revision$
 */
@ContextConfiguration
public class SpringJXPathFilterTest extends AbstractJUnit38SpringContextTests {
    @Autowired
    protected CamelTemplate template;
    @EndpointInject(uri = "mock:results")
    protected MockEndpoint endpoint;

    public void testFilterWithJXPath() throws Exception {
        endpoint.expectedMessageCount(1);

        template.sendBody("direct:start", new PersonBean("James", "London"));

        endpoint.assertIsSatisfied();
    }
}