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
package org.apache.camel.fix;

import biz.c24.io.fix42.NewOrderSingleElement;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.ArtixDSContentType;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class FixUsingExplicitUnmarshalTest extends FixTest {

    @Test
    public void testInvokeMethod() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedHeaderReceived("name", "Tony the Tiger");
        mock.expectedHeaderReceived("dangerous", true);

        Animal animal = new Animal("Tony the Tiger", 12);
        template.sendBody("direct:start", animal);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                // lets run a fix server
                from("fixserver:banzai-to-camel.cfg").to("log:quickfix");

                // now lets run a fix client
                from("file:src/test/data?noop=true").

                        // TODO should we auto-include this inside the FIX component?
                                unmarshal().artixDS(NewOrderSingleElement.class, ArtixDSContentType.Text).

                        to("fix:camel-to-banzai.cfg").
                        to("mock:results");
            }
        };
    }
}
