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
package org.apache.camel.fix;

import java.util.List;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.fix42.NewOrderSingleElement;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.ArtixDSContentType;
import quickfix.Message;

/**
 * @version $Revision$
 */
public class ConvertToQuickMessageWithExplicitUmarshalTest extends ConvertToQuickMessageTest {
   

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("file:src/test/data?noop=true").
                        unmarshal().artixDS(NewOrderSingleElement.class, ArtixDSContentType.Text).

                        // debugging
                        process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                ComplexDataObject body = exchange.getIn().getBody(ComplexDataObject.class);

                                System.out.println("Found DataObject: " + body);
                            }
                        }).
                        convertBodyTo(Message.class).
                        to("mock:results");
            }
        };
    }
}