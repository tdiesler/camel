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
package org.apache.camel.component.restlet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class RestletProducerTest extends CamelTestSupport {

    @Test
    public void testRestletProducer() throws Exception {
        String out = template.requestBodyAndHeader("direct:start", "Hello World", "id", 123, String.class);
        assertEquals("123;Donald Duck;Hello World", out);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // TODO: CAMEL-3021: Should support the {id} uri
                // from("direct:start").to("restlet:http://localhost:9080/users/{id}/basic?restletMethod=post");
                from("direct:start").to("restlet:http://localhost:9080/users/123/basic?restletMethod=post");

                from("restlet:http://localhost:9080/users/{id}/basic?restletMethod=post")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            String id = exchange.getIn().getHeader("id", String.class);
                            String body = exchange.getIn().getBody(String.class);
                            exchange.getOut().setBody(id + ";Donald Duck;" + body);
                        }
                    });
            }
        };
    }
}
