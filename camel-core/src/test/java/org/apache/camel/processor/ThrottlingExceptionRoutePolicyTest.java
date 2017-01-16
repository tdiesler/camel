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
package org.apache.camel.processor;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.ThrottingExceptionHalfOpenHandler;
import org.apache.camel.impl.ThrottlingExceptionRoutePolicy;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottlingExceptionRoutePolicyTest extends ContextTestSupport {
    private static Logger log = LoggerFactory.getLogger(ThrottlingExceptionRoutePolicyTest.class);
    
    private String url = "seda:foo?concurrentConsumers=20";
    private MockEndpoint result;
    private int size = 100;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.setUseRouteBuilder(true);
        result = getMockEndpoint("mock:result");
        
        context.getShutdownStrategy().setTimeout(1);
    }

    
    @Test
    public void testThrottlingRoutePolicyClosed() throws Exception {
        result.expectedMinimumMessageCount(size);

        for (int i = 0; i < size; i++) {
            template.sendBody(url, "Message " + i);
            Thread.sleep(3);
        }

        assertMockEndpointsSatisfied();
    }

    
    @Test
    public void testOpenCircuitToPreventMessageThree() throws Exception {
        result.reset();
        result.expectedMessageCount(2);
        List<String> bodies = Arrays.asList(new String[]{"Message One", "Message Two"}); 
        result.expectedBodiesReceivedInAnyOrder(bodies);
        
        result.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String msg = exchange.getIn().getBody(String.class);
                exchange.setException(new ThrottleException(msg));
            }
        });
        
        // send two messages which will fail
        template.sendBody(url, "Message One");
        template.sendBody(url, "Message Two");
        
        // wait long enough to 
        // have the route shutdown
        Thread.sleep(3000);
        
        // send more messages 
        // but never should get there
        // due to open circuit
        log.debug("sending message three");
        template.sendBody(url, "Message Three");
        
        Thread.sleep(2000);
        
        assertMockEndpointsSatisfied();
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                int threshold = 2;
                long failureWindow = 30;
                long halfOpenAfter = 5000;
                ThrottlingExceptionRoutePolicy policy = new ThrottlingExceptionRoutePolicy(threshold, failureWindow, halfOpenAfter, null);
                policy.setHalfOpenHandler(new NeverCloseHandler());
                
                from(url)
                    .routePolicy(policy)
                    .log("${body}")
                    .to("log:foo?groupSize=10")
                    .to("mock:result");
            }
        };
    }
    
    public class NeverCloseHandler implements ThrottingExceptionHalfOpenHandler {

        @Override
        public boolean isReadyToBeClosed() {
            return false;
        }
        
    }
}
