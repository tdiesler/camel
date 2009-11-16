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
package org.apache.camel.component.jetty.jettyproducer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Unit test with a simple route test.
 */
public class JettyHttpProducerContentBasedRouteTest extends CamelTestSupport {

    private String serverUri = "jetty://http://localhost:9080/myservice";

    @Test
    public void testSendOne() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:one");

        mock.expectedHeaderReceived("one", "true");

        template.requestBody(serverUri + "?one=true", null, Object.class);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSendOther() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:other");

        mock.expectedHeaderReceived("two", "true");

        template.requestBody(serverUri + "?two=true", null, Object.class);

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(serverUri)
                    .choice()
                    .when().simple("in.header.one").to("mock:one")
                    .otherwise()
                    .to("mock:other");
            }
        };
    }

}