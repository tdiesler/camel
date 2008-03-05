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
package org.apache.camel.processor.juel;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.language.juel.JuelExpression.el;

/**
 * @version $Revision$
 */
public class SimulatorTest extends ContextTestSupport {

    public void testReceivesFooResponse() throws Exception {
        assertRespondsWith("foo", "fooResponse");
    }

    public void testReceivesBarResponse() throws Exception {
        assertRespondsWith("bar", "barResponse");
    }

    protected void assertRespondsWith(final String value, String containedText) throws InvalidPayloadException {
        Exchange response = template.request("direct:a", new Processor() {
            public void process(Exchange exchange) throws Exception {
                Message in = exchange.getIn();
                in.setBody("answer");
                in.setHeader("cheese", value);
            }
        });

        assertNotNull("Should receive a response!", response);

        String text = ExchangeHelper.getMandatoryOutBody(response, String.class);
        log.info("Received: " + text);
        assertStringContains(text, containedText);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // START SNIPPET: example
                from("direct:a").
                    recipientList(el("file:src/test/data/${in.headers.cheese}.xml"));
                // END SNIPPET: example
            }
        };
    }
}