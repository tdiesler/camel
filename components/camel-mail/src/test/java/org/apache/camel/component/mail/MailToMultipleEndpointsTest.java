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
package org.apache.camel.component.mail;

import javax.mail.Message;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.jvnet.mock_javamail.Mailbox;

/**
 * @version $Revision$
 */
public class MailToMultipleEndpointsTest extends ContextTestSupport {

    public void testMultipleEndpoints() throws Exception {
        Mailbox.clearAll();

        template.sendBodyAndHeader("direct:a", "Hello World", "Subject", "Hello a");
        template.sendBodyAndHeader("direct:b", "Bye World", "Subject", "Hello b");
        template.sendBodyAndHeader("direct:c", "Hi World", "Subject", "Hello c");

        Mailbox boxA = Mailbox.get("a@a.com");
        assertEquals(1, boxA.size());
        assertEquals("Hello a", boxA.get(0).getSubject());
        assertEquals("Hello World", boxA.get(0).getContent());
        assertEquals("me@me.com", boxA.get(0).getFrom()[0].toString());

        Mailbox boxB = Mailbox.get("b@b.com");
        assertEquals(1, boxB.size());
        assertEquals("Hello b", boxB.get(0).getSubject());
        assertEquals("Bye World", boxB.get(0).getContent());
        assertEquals("you@you.com", boxB.get(0).getFrom()[0].toString());

        Mailbox boxC = Mailbox.get("c@c.com");
        assertEquals(1, boxC.size());
        assertEquals("Hello c", boxC.get(0).getSubject());
        assertEquals("Hi World", boxC.get(0).getContent());
        assertEquals("me@me.com", boxC.get(0).getFrom()[0].toString());
        assertEquals("you@you.com", boxC.get(0).getRecipients(Message.RecipientType.CC)[0].toString());
        assertEquals("them@them.com", boxC.get(0).getRecipients(Message.RecipientType.CC)[1].toString());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:a").to("smtp://localhost?username=james2&password=secret&To=a@a.com&From=me@me.com");

                from("direct:b").to("smtp://localhost?username=james&password=secret&To=b@b.com&From=you@you.com");

                from("direct:c").to("smtp://localhost?username=admin&password=secret&To=c@c.com&From=me@me.com&CC=you@you.com,them@them.com");
            }
        };
    }
}
