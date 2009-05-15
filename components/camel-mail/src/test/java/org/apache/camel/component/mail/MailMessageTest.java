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

import java.util.Iterator;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.util.ObjectHelper;

/**
 * @version $Revision$
 */
public class MailMessageTest extends ContextTestSupport {
    private Session mailSession;
    private MimeMessage mimeMessage;
    private MailEndpoint endpoint;
    private String body = "Hello World!";

    public void testMailMessageHandlesMultipleHeaders() throws Exception {
        mimeMessage.setRecipients(Message.RecipientType.TO, new Address[] {new InternetAddress("foo@localhost"), new InternetAddress("bar@localhost")});

        MailExchange exchange = endpoint.createExchange(mimeMessage);
        MailMessage in = exchange.getIn();

        assertEquals("mail body", body, in.getBody());

        // need to use iterator as some mail impl returns String[] and others a single String with comma as separator
        // so we let Camel create an iterator so we can use the same code for the test
        Object to = in.getHeader("TO");
        Iterator<String> it = ObjectHelper.createIterator(to);
        int i = 0;
        while (it.hasNext()) {
            if (i == 0) {
                assertEquals("foo@localhost", it.next().trim());
            } else {
                assertEquals("bar@localhost", it.next().trim());
            }
            i++;
        }
    }

    public void testMailMessageHandlesSingleHeader() throws Exception {
        mimeMessage.setRecipients(Message.RecipientType.TO, new Address[] {new InternetAddress("frank@localhost")});

        MailExchange exchange = endpoint.createExchange(mimeMessage);
        MailMessage in = exchange.getIn();
        Object header = in.getHeader("TO");
        String value = assertIsInstanceOf(String.class, header);
        assertEquals("value", "frank@localhost", value);

        assertEquals("body", body, in.getBody());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        endpoint = resolveMandatoryEndpoint("pop3://someone@myhost:30/subject");

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        mailSession = Session.getInstance(properties, null);

        mimeMessage = new MimeMessage(mailSession);
        mimeMessage.setText(body);
    }

    @Override
    protected MailEndpoint resolveMandatoryEndpoint(String uri) {
        Endpoint endpoint = super.resolveMandatoryEndpoint(uri);
        return assertIsInstanceOf(MailEndpoint.class, endpoint);
    }
}
