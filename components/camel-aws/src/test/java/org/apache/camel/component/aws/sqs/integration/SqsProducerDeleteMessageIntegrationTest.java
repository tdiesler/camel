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
package org.apache.camel.component.aws.sqs.integration;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * To provide aws credentials, please export following environment variables:
 * export AWS_ACCESS_KEY=xxx
 * export AWS_SECRET_KEY=xxx
 * export AWS_REGION=EU_WEST_1
 */
@Ignore("Must be manually tested. Provide your own accessKey and secretKey!")
public class SqsProducerDeleteMessageIntegrationTest extends CamelTestSupport {

    @EndpointInject(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @AfterClass
    public static void deleteQueues() {
        SQSIntegrationTestHelper.deleteQueues("camel-2");
    }

    @Test
    public void sendInOnly() throws Exception {
        result.expectedMessageCount(1);

        Exchange exchange = template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Test sqs");
            }
        });

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        final String sqsEndpointUri = "aws-sqs://camel-2?" + SQSIntegrationTestHelper.urlCredentials();

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").startupOrder(2).to(sqsEndpointUri);

                from(String.format("aws-sqs://camel-2?deleteAfterRead=false&" + SQSIntegrationTestHelper.urlCredentials())).startupOrder(1).log("${body}")
                .to("aws-sqs://camel-2?operation=deleteMessage&autoCreateQueue=true&" + SQSIntegrationTestHelper.urlCredentials()).log("${body}")
                .log("${header.CamelAwsSqsReceiptHandle}")
                .to("mock:result");
            }
        };
    }
}
