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
package org.apache.camel.artix.ds.old;

import java.util.List;

import iso.std.iso.x20022.tech.xsd.pacs.x008.x001.x01.DocumentElement;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.artix.ds.ArtixDSSink;
import org.apache.camel.artix.ds.ArtixDSSource;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
<<<<<<< HEAD
=======
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;
>>>>>>> 9371055... CAMEL-3953, CAMEL-3954, CAMEL-3260: camel-core now loads type converter without classpath scanning. This speeup starting CamelContext and unit testing. CAMEL-3032: Do not use camel-test JAR in pomx.xml.

/**
 * @version $Revision$
 */
<<<<<<< HEAD
public class AdsReformatViaProcessorsTest extends ContextTestSupport {
    public void testArtix() throws Exception {
        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(1);

        resultEndpoint.assertIsSatisfied();
=======
public class FilterTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void testSendMatchingMessage() throws Exception {
        String expectedBody = "<matched/>";

        resultEndpoint.expectedBodiesReceived(expectedBody);

        template.sendBodyAndHeader(expectedBody, "foo", "bar");

        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSendNotMatchingMessage() throws Exception {
        resultEndpoint.expectedMessageCount(0);
>>>>>>> 9371055... CAMEL-3953, CAMEL-3954, CAMEL-3260: camel-core now loads type converter without classpath scanning. This speeup starting CamelContext and unit testing. CAMEL-3032: Do not use camel-test JAR in pomx.xml.

        List<Exchange> list = resultEndpoint.getReceivedExchanges();
        Exchange exchange = list.get(0);
        Message in = exchange.getIn();

        String text = in.getBody(String.class);
        log.info("Received: " + text);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("file:src/test/data?noop=true").

                        process(ArtixDSSource.adsSource(DocumentElement.class).xmlSource()).

                        process(ArtixDSSink.adsSink().tagValuePair()).

                        to("mock:result");
            }
        };
    }
}
