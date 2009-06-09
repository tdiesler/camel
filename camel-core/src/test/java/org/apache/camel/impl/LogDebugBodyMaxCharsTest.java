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
package org.apache.camel.impl;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version $Revision$
 */
public class LogDebugBodyMaxCharsTest extends ContextTestSupport {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context.getProperties().put(Exchange.LOG_DEBUG_BODY_MAX_CHARS, "20");
    }

    public void testLogBodyMaxLengthTest() throws Exception {
        // create a big body
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 1000; i++) {
            int value = i % 10;
            sb.append(value);
        }
        String body = sb.toString();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", body);

        assertMockEndpointsSatisfied();

        // should be clipped after 20 chars
        String msg = mock.getReceivedExchanges().get(0).getIn().toString();
        assertEquals("Message: 01234567890123456789... [Body clipped after 20 chars, total length is 1000]", msg);

        // but body and clipped should not be the same
        assertNotSame("clipped log and real body should not be the same", msg, mock.getReceivedExchanges().get(0).getIn().getBody(String.class));
    }

    public void tesNotClipped() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", "1234567890");

        assertMockEndpointsSatisfied();

        // should be clipped after 20 chars
        String msg = mock.getReceivedExchanges().get(0).getIn().toString();
        assertEquals("Message: 01234567890", msg);

        // but body and clipped should not be the same
        assertNotSame("clipped log and real body should not be the same", msg, mock.getReceivedExchanges().get(0).getIn().getBody(String.class));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("log:foo").to("mock:result");
            }
        };
    }
}
