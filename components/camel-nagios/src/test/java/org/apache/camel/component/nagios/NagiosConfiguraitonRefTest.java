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
package org.apache.camel.component.nagios;

import com.googlecode.jsendnsca.core.Level;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.mocks.NagiosNscaStub;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class NagiosConfiguraitonRefTest extends CamelTestSupport {

    private NagiosNscaStub nagios;

    @Before
    @Override
    public void setUp() throws Exception {
        nagios = new NagiosNscaStub(5667, "secret");
        nagios.start();

        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        nagios.stop();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        NagiosConfiguration config = new NagiosConfiguration();
        config.setPassword("secret");
        config.setHost("127.0.0.1");
        config.setPort(5667);

        JndiRegistry jndi = super.createRegistry();
        jndi.bind("nagiosConf", config);
        return jndi;
    }

    @Test
    public void testSendToNagios() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.allMessages().body().isInstanceOf(String.class);

        template.sendBody("direct:start", "Hello Nagios");

        assertMockEndpointsSatisfied();

        assertEquals(1, nagios.getMessagePayloadList().size());

        MessagePayload payload = nagios.getMessagePayloadList().get(0);
        assertEquals("Hello Nagios", payload.getMessage());
        assertEquals("localhost", payload.getHostname());
        assertEquals(Level.OK.ordinal(), payload.getLevel());
        assertEquals(context.getName(), payload.getServiceName());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // START SNIPPET: e1
                from("direct:start").to("nagios:foo?configuration=#nagiosConf").to("mock:result");
                // END SNIPPET: e1
            }
        };
    }

}