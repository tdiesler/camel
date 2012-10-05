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
package org.apache.camel.itest.netty;

import javax.naming.Context;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jndi.JndiContext;
import org.junit.Test;

/**
 * Doing request/reply over Netty with async processing.
 */
public class NettyAsyncRequestReplyTest extends CamelTestSupport {

    private int port;

    @Test
    public void testNetty() throws Exception {
        String out = template.requestBody("netty:tcp://localhost:" + port + "?textline=true?sync=true", "World", String.class);
        assertEquals("Bye World", out);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                port = AvailablePortFinder.getNextAvailable(8000);

                from("netty:tcp://localhost:" + port + "?textline=true?sync=true?reuseAddress=true?synchronous=false")
                    .to("activemq:queue:foo")
                    .log("Writing reply ${body}");

                from("activemq:queue:foo")
                    .transform(simple("Bye ${body}"));
            }
        };
    }

    @Override
    protected Context createJndiContext() throws Exception {
        JndiContext answer = new JndiContext();

        // add ActiveMQ with embedded broker
        ActiveMQComponent amq = ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false");
        amq.setCamelContext(context);
        answer.bind("activemq", amq);
        return answer;
    }

}
