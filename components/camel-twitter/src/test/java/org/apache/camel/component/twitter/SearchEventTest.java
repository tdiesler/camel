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
package org.apache.camel.component.twitter;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.twitter.mocks.TwitterStreamMock;
import org.apache.camel.impl.JndiRegistry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchEventTest extends CamelTwitterTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(SearchEventTest.class);

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    private TwitterStreamMock twitterStream;

    @Test
    public void testSearchTimeline() throws Exception {
        resultEndpoint.expectedMinimumMessageCount(1);

        twitterStream.updateStatus("#cameltest tweet");
        resultEndpoint.assertIsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("twitter://streaming/filter?type=event&twitterStream=#twitterStream&keywords=#cameltest")
                    .transform(body().convertToString()).to("mock:result");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        twitterStream = new TwitterStreamMock();
        JndiRegistry registry = super.createRegistry();
        registry.bind("twitterStream", twitterStream);
        return registry;
    }
}
