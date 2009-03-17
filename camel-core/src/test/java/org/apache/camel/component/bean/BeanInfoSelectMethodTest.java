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
package org.apache.camel.component.bean;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;

/**
 * @version $Revision$
 */
public class BeanInfoSelectMethodTest extends ContextTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("foo", new MyFooBean());
        return jndi;
    }

    public void testOrder() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Order");
        template.sendBody("direct:a", "Hello");
        assertMockEndpointsSatisfied();
    }

    public void testFailure() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Failure");
        template.send("direct:b", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Hello");
                exchange.setException(new IllegalArgumentException("Forced by unit test"));
            }
        });
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel().logStackTrace(false).disableRedelivery());

                onException(Exception.class).handled(true).beanRef("foo").to("mock:result");

                from("direct:a").beanRef("foo").to("mock:result");

                from("direct:b").to("mock:foo");
            }
        };
    }



    public static class MyFooBean {

        public String handleException(Exception e) {
            fail("Should not call this method as it is not intended for Camel");
            return "Exception";
        }

        public String handleFailure(String order, Exception e) {
            return "Failure";
        }

        public String handleOrder(String order) {
            return "Order";
        }
    }
}
