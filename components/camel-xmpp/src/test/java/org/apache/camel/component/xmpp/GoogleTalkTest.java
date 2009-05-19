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
package org.apache.camel.component.xmpp;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

public class GoogleTalkTest extends ContextTestSupport {
    // a disabled test... before enabling you must fill in your own gmail credentials in the route below
    public void xtestSendToGTalk() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        template.sendBody("direct:start", "Hi!");
        result.assertIsSatisfied();
    }
    
    public void xtestSendToGTalkWithSubject() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        template.sendBodyAndHeader("direct:start", "Hi!", "Subject", "From Camel");
        result.assertIsSatisfied();
    }

    // get around junit warning
    public void testNothing() throws Exception {        
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // send a message from fromuser@gmail.com to touser@gmail.com
                from("direct:start").
                    to("xmpp://talk.google.com:5222/touser@gmail.com?serviceName=gmail.com&user=fromuser&password=secret").
                    to("mock:result");
            }
        };
    }
}
