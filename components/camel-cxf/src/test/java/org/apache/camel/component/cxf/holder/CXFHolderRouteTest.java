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
package org.apache.camel.component.cxf.holder;

import java.util.List;

import javax.xml.ws.Holder;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class CXFHolderRouteTest extends CxfHolderConsumerTest {
    
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(CXF_ENDPOINT_URI).wireTap("seda:tap").process(new Processor() {
                    @SuppressWarnings("unchecked")
                    public void process(Exchange exchange) throws Exception {
                        Message in = exchange.getIn();
                        List parameters = in.getBody(List.class);
                        int amount = (Integer) parameters.remove(1);
                        Holder<String> customer = (Holder<String>)parameters.get(1);
                        if (customer.value.length() == 0) {
                            customer.value = "newCustomer";
                        }
                        parameters.add(0, "Ordered ammount " + amount);
                        exchange.getOut().setBody(parameters);
                    }
                });
                from("seda:tap").to("log:myEndpoint");
            }
        };
    }

}
