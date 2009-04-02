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
package org.apache.camel.component.jetty;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import static org.apache.camel.component.mock.MockEndpoint.expectsMessageCount;

public class JettyWithXPathChoiceTest extends ContextTestSupport {
    protected MockEndpoint x;
    protected MockEndpoint y;
    protected MockEndpoint z;

    public void testSendToFirstWhen() throws Exception {
        String body = "<one/>";
        expectsMessageCount(0, y, z);

        sendBody(body);

        assertMockEndpointsSatisfied();

        x.reset();
        y.reset();
        z.reset();        
        
        body = "<two/>";
        expectsMessageCount(0, x, z);
        
        sendBody(body);

        assertMockEndpointsSatisfied();    
    }

    private void sendBody(String body) {
        template.sendBody("http://localhost:9080/myworld", body);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        x = getMockEndpoint("mock:x");
        y = getMockEndpoint("mock:y");
        z = getMockEndpoint("mock:z");
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                errorHandler(deadLetterChannel("mock:error").delay(0));

                // Need a convertBodyTo processor here or we may get an error
                // that we are at the end of the stream
                from("jetty:http://localhost:9080/myworld").choice()
                  .when().xpath("/one").to("mock:x")
                  .when().xpath("/two").to("mock:y")
                  .otherwise().to("mock:z");
            }
        };
    }

}
