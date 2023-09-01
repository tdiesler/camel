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
package org.apache.camel.component.file;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenericFilePollingConsumerTest extends ContextTestSupport {

    @Rule
    public TemporaryFolder ready = new TemporaryFolder();

    @Rule
    public TemporaryFolder progressing = new TemporaryFolder();

    @Rule
    public TemporaryFolder done = new TemporaryFolder();

    public static final Logger LOG = LoggerFactory.getLogger(GenericFilePollingConsumerTest.class);


    //ENTESB-21865
    @Test
    public void entesb21865Test() throws Exception {
        LOG.info("***** START *****");
        MockEndpoint end = getMockEndpoint("mock:end");

        LOG.info("***** FIRST FILE *****");

        ready.newFile("DELIVERY_GEMAUPCOIPID.0");
        template.requestBody("direct:test", "Hi Camel!");
        end.expectedMessageCount(1);
        assertMockEndpointsSatisfied();
        assertFileExists(done.getRoot().getCanonicalPath() + "/" + "DELIVERY_GEMAUPCOIPID.0");

        LOG.info("***** TRIGGER WITH NO FILE *****");
        template.requestBody("direct:test", "Hi Camel!");

        LOG.info("***** SECOND FILE *****");

        ready.newFile("DELIVERY_GEMAUPCOIPID.1");
        template.requestBody("direct:test", "Hi Camel!");
        end.expectedMessageCount(2);
        assertMockEndpointsSatisfied();
        assertFileExists(done.getRoot().getCanonicalPath() + "/" + "DELIVERY_GEMAUPCOIPID.1");


    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test")
                        .routeId("pollEnrich")
                        .setExchangePattern(ExchangePattern.InOut)
                        .pollEnrich("file://" + ready.getRoot().getCanonicalPath() + "?" +
                                "preMove=" + progressing.getRoot().getCanonicalPath() + "&" +
                                "sendEmptyMessageWhenIdle=true&" +
                                "antInclude=DELIVERY_GEMAUPCOIPID.*&" +
                                "move=" + done.getRoot().getCanonicalPath() + "/${file:onlyname}&" +
                                "moveFailed=/tmp/incoming/error/${file:onlyname}.${exchangeProperty.esbMetaData[esb_tracking_id]}&" +
                                "idempotent=false", 2000, (AggregationStrategy) null)
                        .choice()
                        .when(body().isNull())
                        .log(LoggingLevel.INFO, "No more exchanges found. Stopping...")
                        .stop()
                        .otherwise()
                        .end()
                        .log(LoggingLevel.INFO, "Done processing ${file:name}")
                        .to("mock:end");
            }
        };
    }
}
