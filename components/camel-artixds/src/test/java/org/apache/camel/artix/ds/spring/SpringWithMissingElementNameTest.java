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
package org.apache.camel.artix.ds.spring;
import java.util.List;

import iso.std.iso.x20022.tech.xsd.pacs.x008.x001.x01.Document;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import static org.apache.camel.hamcrest.Assertions.assertInstanceOf;

/**
 * @version $Revision$
 */
@ContextConfiguration
public class SpringWithMissingElementNameTest extends AbstractJUnit38SpringContextTests {
    @EndpointInject(uri = "mock:result")
    MockEndpoint resultEndpoint;

    public void testBadElementName() throws Exception {
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.assertIsSatisfied();
        List<Exchange> list = resultEndpoint.getExchanges();
        assertEquals("list size", 1, list.size());
        Exchange exchange = list.get(0);
        Object body = exchange.getIn().getBody();
        Document document = assertInstanceOf("body", body, Document.class);
        System.out.println("Found: " + document);
    }
}
