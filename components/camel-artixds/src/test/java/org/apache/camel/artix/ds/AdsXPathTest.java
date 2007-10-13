/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.artix.ds;

import java.util.List;

import biz.c24.io.api.data.ComplexDataObject;
import iso.std.iso.x20022.tech.xsd.pacs.x008.x001.x01.DocumentElement;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.builder.saxon.XQueryBuilder.xquery;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.ArtixDSContentType;

/**
 * @version $Revision: 1.1 $
 */
public class AdsXPathTest extends ContextTestSupport {
    public void testParsingMessage() throws Exception {
        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(1);

        resultEndpoint.assertIsSatisfied();

        List<Exchange> list = resultEndpoint.getReceivedExchanges();
        Exchange exchange = list.get(0);
        Message in = exchange.getIn();
        log.info("Headers: " + in.getHeaders());
        ComplexDataObject object = assertIsInstanceOf(ComplexDataObject.class, in.getBody());
        log.info("Received: " + object);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {

                Namespaces ns = new Namespaces("foo", "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.01");

                from("file:src/test/data?noop=true").
                        unmarshal().artixDS(DocumentElement.class, ArtixDSContentType.Xml).
                        setHeader("foo", ns.xquery("//foo:MsgId", String.class)).
                        filter(ns.xquery("//foo:MsgId = 'PFSM1234'")).
                        to("mock:result");
            }
        };
    }
}