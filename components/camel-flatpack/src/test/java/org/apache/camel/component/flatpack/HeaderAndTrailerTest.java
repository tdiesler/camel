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
package org.apache.camel.component.flatpack;

import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * @version $Revision$
 */
@ContextConfiguration
public class HeaderAndTrailerTest extends AbstractJUnit38SpringContextTests {
    private static final transient Log LOG = LogFactory.getLog(HeaderAndTrailerTest.class);

    @EndpointInject(uri = "mock:results")
    protected MockEndpoint results;

    protected String[] expectedFirstName = {"JOHN", "JIMMY", "JANE", "FRED"};

    public void testHeaderAndTrailer() throws Exception {
        results.expectedMessageCount(6);
        results.assertIsSatisfied();

        int counter = 0;
        List<Exchange> list = results.getReceivedExchanges();

        // assert header
        Map header = list.get(0).getIn().getBody(Map.class);
        assertEquals("HBT", header.get("INDICATOR"));
        assertEquals("20080817", header.get("DATE"));

        // assert body
        for (Exchange exchange : list.subList(1, 5)) {
            Message in = exchange.getIn();
            Map body = in.getBody(Map.class);
            assertNotNull("Should have found body as a Map but was: " + ObjectHelper.className(in.getBody()), body);
            assertEquals("FIRSTNAME", expectedFirstName[counter], body.get("FIRSTNAME"));
            LOG.info("Result: " + counter + " = " + body);
            counter++;
        }

        // assert trailer
        Map trailer = list.get(5).getIn().getBody(Map.class);
        assertEquals("FBT", trailer.get("INDICATOR"));
        assertEquals("SUCCESS", trailer.get("STATUS"));
    }

}