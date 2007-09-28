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
package org.apache.camel.component.artixds;

import java.util.List;

import iso.std.iso.x20022.tech.xsd.pacs.x008.x001.x01.DocumentElement;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.component.artixds.ArtixSink.artixSink;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision: 1.1 $
 */
public class ArtixReformatTest extends ContextTestSupport {

    public void testArtix() throws Exception {
        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(1);

        resultEndpoint.assertIsSatisfied();

        List<Exchange> list = resultEndpoint.getReceivedExchanges();
        Exchange exchange = list.get(0);
        Message in = exchange.getIn();

        String text = in.getBody(String.class);
        System.out.println("Received: " + text);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("file:src/test/data?noop=true").

                    // process as a source
                    process(ArtixSource.artixSource(DocumentElement.class).xmlSource()).

                    // now reformat with a custom sink
                    process(artixSink().tagValuePair()).


                to("mock:result");
            }
        };
    }

}