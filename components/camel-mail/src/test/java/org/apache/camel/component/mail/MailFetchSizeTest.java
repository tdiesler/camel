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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Unit test for fetch size.
 */
public class MailFetchSizeTest extends ContextTestSupport {

    public void testFetchSize() throws Exception {
        prepareMailbox();
        Mailbox mailbox = Mailbox.get("james@localhost");
        assertEquals(5, mailbox.size());

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.expectedBodiesReceived("Message 0", "Message 1");
        // should be done within 2 seconds as no delay when started
        mock.setResultWaitTime(2000L);
        mock.assertIsSatisfied();

        assertEquals(3, mailbox.size());

        // reset mock to assert the next batch of 2 messages polled
        mock.reset();
        mock.expectedMessageCount(2);
        mock.expectedBodiesReceived("Message 2", "Message 3");
        // should be done within 5 (delay) + 1 seconds (polling)
        mock.setResultWaitTime(7000L);
        mock.assertIsSatisfied();

        assertEquals(1, mailbox.size());

        // reset mock to assert the last message polled
        mock.reset();
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Message 4");
        mock.assertIsSatisfied();
    }

    private void prepareMailbox() throws Exception {
        // connect to mailbox
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        Store store = sender.getSession().getStore("pop3");
        store.connect("localhost", 25, "james", "secret");
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);

        // inserts 5 new messages
        Message[] messages = new Message[5];
        for (int i = 0; i < 5; i++) {
            messages[i] = new MimeMessage(sender.getSession());
            messages[i].setText("Message " + i);
        }
        folder.appendMessages(messages);
        folder.close(true);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("pop3://james@localhost?password=secret&fetchSize=2&consumer.delay=5000").to("mock:result");
            }
        };
    }
}
