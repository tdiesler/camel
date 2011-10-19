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
package org.apache.camel.component.aws.sqs;

/**
 * Test to verify that the polling consumer delivers an empty Exchange when the
 * sendEmptyMessageWhenIdle property is set and a polling event yields no results.
 */
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SqsConsumerIdleMessageTest extends CamelTestSupport {
    
    @Test
    public void testConsumeIdleMessages() throws Exception {
        Thread.sleep(110);
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(2);
        assertMockEndpointsSatisfied();
        assertTrue(mock.getExchanges().get(0).getIn().getBody() == null);
        assertTrue(mock.getExchanges().get(1).getIn().getBody() == null);
    }
    
    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        
        AmazonSQSClientMock clientMock = new AmazonSQSClientMock();        
        registry.bind("amazonSQSClient", clientMock);
        
        return registry;
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("aws-sqs://MyQueue?amazonSQSClient=#amazonSQSClient&delay=50&maxMessagesPerPoll=5"
                        + "&sendEmptyMessageWhenIdle=true")
                    .to("mock:result");
            }
        };
    }

}
