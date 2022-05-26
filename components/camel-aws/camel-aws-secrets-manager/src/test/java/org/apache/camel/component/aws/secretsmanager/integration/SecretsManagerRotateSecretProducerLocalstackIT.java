/*
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
package org.apache.camel.component.aws.secretsmanager.integration;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.secretsmanager.SecretsManagerConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.RotateSecretResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecretsManagerRotateSecretProducerLocalstackIT extends AwsSecretsManagerBaseTest {

    @EndpointInject("mock:result")
    private MockEndpoint mock;

    @Test
    public void createSecretTest() {

        mock.expectedMessageCount(1);
        Exchange exchange = template.request("direct:createSecret", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setHeader(SecretsManagerConstants.SECRET_NAME, "TestSecret4");
                exchange.getIn().setBody("Body");
            }
        });

        CreateSecretResponse resultGet = (CreateSecretResponse) exchange.getMessage().getBody();
        assertNotNull(resultGet);

        exchange = template.request("direct:rotateSecret", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setHeader(SecretsManagerConstants.SECRET_ID, resultGet.arn());
            }
        });

        RotateSecretResponse resultRotate = (RotateSecretResponse) exchange.getMessage().getBody();
        assertNotNull(resultRotate);
        assertTrue(resultRotate.sdkHttpResponse().isSuccessful());
        assertEquals("TestSecret4", resultRotate.name());

    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:createSecret")
                        .to("aws-secrets-manager://test?operation=createSecret");

                from("direct:rotateSecret")
                        .to("aws-secrets-manager://test?operation=rotateSecret")
                        .to("mock:result");
            }
        };
    }
}
