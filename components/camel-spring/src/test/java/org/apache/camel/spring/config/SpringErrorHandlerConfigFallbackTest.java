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
package org.apache.camel.spring.config;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class SpringErrorHandlerConfigFallbackTest extends SpringErrorHandlerConfigTest {

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/config/SpringErrorHandlerConfigFallbackTest.xml");
    }

    public void testDefaultEH() throws Exception {
        // TODO: delete me when working

        getMockEndpoint("mock:result").expectedMessageCount(0);
        getMockEndpoint("mock:dlc").expectedMessageCount(0);

        Exchange exchange = template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Damn");
            }
        });

        assertMockEndpointsSatisfied();

        assertTrue(exchange.isFailed());
        assertEquals("Damn cannot do this", exchange.getException(IllegalArgumentException.class).getMessage());
        assertEquals(true, exchange.getIn().getHeader(Exchange.REDELIVERED));
        assertEquals(2, exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER));
    }


}
