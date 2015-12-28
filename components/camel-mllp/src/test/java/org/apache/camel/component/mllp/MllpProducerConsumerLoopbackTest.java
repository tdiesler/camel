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
package org.apache.camel.component.mllp;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.mllp.PassthroughProcessor;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.camel.test.mllp.Hl7MessageGenerator.generateMessage;


public class MllpProducerConsumerLoopbackTest extends CamelTestSupport {
    int mllpPort = AvailablePortFinder.getNextAvailable();

    @EndpointInject(uri = "direct://source")
    ProducerTemplate source;

    @EndpointInject(uri = "mock://acknowledged")
    MockEndpoint acknowledged;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        DefaultCamelContext context = (DefaultCamelContext) super.createCamelContext();

        context.setUseMDCLogging(true);
        context.setName(this.getClass().getSimpleName());

        return context;
    }


    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        RouteBuilder[] builders = new RouteBuilder[2];
        final int groupInterval = 1000;
        final boolean groupActiveOnly = false;

        builders[0] = new RouteBuilder() {
            String routeId = "mllp-receiver";

            public void configure() {
                fromF("mllp:%d?autoAck=true", mllpPort)
                        .convertBodyTo(String.class)
                        .to(acknowledged)
                        .process(new PassthroughProcessor("after send to result"))
                        .log(LoggingLevel.DEBUG, routeId, "Receiving: ${body}")
                        .toF("log://%s?level=INFO&groupInterval=%d&groupActiveOnly=%b", routeId, groupInterval, groupActiveOnly);
            }
        };

        builders[1] = new RouteBuilder() {
            String routeId = "mllp-sender";

            String host = "0.0.0.0";

            public void configure() {
                from(source.getDefaultEndpoint()).routeId(routeId)
                        .log(LoggingLevel.DEBUG, routeId, "Sending: ${body}")
                        .toF("mllp://%s:%d", host, mllpPort)
                        .setBody(header(MllpConstants.MLLP_ACKNOWLEDGEMENT))
                        .toF("log://%s?level=INFO&groupInterval=%d&groupActiveOnly=%b", routeId, groupInterval, groupActiveOnly);
            }
        };

        return builders;
    }

    @Test
    public void testLoopbackWithOneMessage() throws Exception {
        String testMessage = generateMessage();
        acknowledged.expectedBodiesReceived(testMessage);

        String acknowledgement = source.requestBody((Object) testMessage, String.class);
        Assert.assertThat("Should be acknowledgment for message 1", acknowledgement, CoreMatchers.containsString(String.format("MSA|AA|00001")));

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Test
    public void testLoopbackMultipleMessages() throws Exception {
        int messageCount = 1000;
        acknowledged.expectedMessageCount(messageCount);

        for (int i = 1; i <= messageCount; ++i) {
            String testMessage = generateMessage(i);
            acknowledged.message(i - 1).body().isEqualTo(testMessage);
            String acknowledgement = source.requestBody((Object) testMessage, String.class);
            Assert.assertThat("Should be acknowledgment for message " + i, acknowledgement, CoreMatchers.containsString(String.format("MSA|AA|%05d", i)));

        }

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }
}

