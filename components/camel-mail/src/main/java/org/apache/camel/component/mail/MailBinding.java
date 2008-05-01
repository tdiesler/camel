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
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Exchange;
import org.apache.camel.converter.ObjectConverter;

/**
 * A Strategy used to convert between a Camel {@link Exchange} and {@link Message} to and
 * from a Mail {@link MimeMessage}
 *
 * @version $Revision$
 */
public class MailBinding {

    public void populateMailMessage(MailEndpoint endpoint, MimeMessage mimeMessage, Exchange exchange) {
        try {
            appendHeadersFromCamel(mimeMessage, exchange, exchange.getIn());

            String destination = endpoint.getConfiguration().getDestination();
            if (destination != null) {
                mimeMessage.setRecipients(Message.RecipientType.TO, destination);
            }
            // must have a destination otherwise we do not know where to send the mail
            if (mimeMessage.getAllRecipients() == null) {
                throw new IllegalArgumentException("The MineMessage does not have any recipients set. "
                    + "Add a destination (Recipient.TO) to the MailConfiguration.");
            }

            if (empty(mimeMessage.getFrom())) {
                // lets default the address to the endpoint destination
                String from = endpoint.getConfiguration().getFrom();
                mimeMessage.setFrom(new InternetAddress(from));
            }

            if (exchange.getIn().getAttachments() != null && exchange.getIn().getAttachments().size() > 0) {
                appendAttachmentsFromCamel(mimeMessage, exchange, exchange.getIn());
            } else {
                mimeMessage.setText(exchange.getIn().getBody(String.class));
            }
        } catch (Exception e) {
            throw new RuntimeMailException("Failed to populate body due to: " + e.getMessage()
                                           + ". Exchange: " + exchange, e);
        }
    }

    protected boolean empty(Address[] addresses) {
        return addresses == null || addresses.length == 0;
    }

    /**
     * Extracts the body from the Mail message
     */
    public Object extractBodyFromMail(MailExchange exchange, Message message) {
        try {
            return message.getContent();
        } catch (Exception e) {
            throw new RuntimeMailException("Failed to extract body due to: " + e.getMessage()
                                           + ". Exchange: " + exchange + ". Message: " + message, e);
        }
    }

    /**
     * Appends the Mail headers from the Camel {@link MailMessage}
     */
    protected void appendHeadersFromCamel(MimeMessage mimeMessage, Exchange exchange,
                                          org.apache.camel.Message camelMessage) throws MessagingException {
        Set<Map.Entry<String, Object>> entries = camelMessage.getHeaders().entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String headerName = entry.getKey();
            Object headerValue = entry.getValue();
            if (headerValue != null) {
                if (shouldOutputHeader(camelMessage, headerName, headerValue)) {

                    // Mail messages can repeat the same header...
                    if (ObjectConverter.isCollection(headerValue)) {
                        Iterator iter = ObjectConverter.iterator(headerValue);
                        while (iter.hasNext()) {
                            Object value = iter.next();
                            mimeMessage.addHeader(headerName, asString(exchange, value));
                        }
                    } else {
                        mimeMessage.setHeader(headerName, asString(exchange, headerValue));
                    }
                }
            }
        }
    }

    /**
     * Appends the Mail attachments from the Camel {@link MailMessage}
     */
    protected void appendAttachmentsFromCamel(MimeMessage mimeMessage, Exchange exchange,
                                              org.apache.camel.Message camelMessage)
        throws MessagingException {

        // Create a Multipart
        MimeMultipart multipart = new MimeMultipart();

        // fill the body with text
        multipart.setSubType("mixed");
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(exchange.getIn().getBody(String.class), "text/plain");
        multipart.addBodyPart(textBodyPart);

        BodyPart messageBodyPart;

        Set<Map.Entry<String, DataHandler>> entries = camelMessage.getAttachments().entrySet();
        for (Map.Entry<String, DataHandler> entry : entries) {
            String attName = entry.getKey();
            DataHandler attValue = entry.getValue();
            if (attValue != null) {
                if (shouldOutputAttachment(camelMessage, attName, attValue)) {
                    // Create another body part
                    messageBodyPart = new MimeBodyPart();
                    // Set the data handler to the attachment
                    messageBodyPart.setDataHandler(attValue);
                    // Set the filename
                    messageBodyPart.setFileName(attName);
                    // Set Disposition
                    messageBodyPart.setDisposition(Part.ATTACHMENT);
                    // Add part to multipart
                    multipart.addBodyPart(messageBodyPart);
                }
            }
        }
        // Put parts in message
        mimeMessage.setContent(multipart);
    }

    /**
     * Converts the given object value to a String
     */
    protected String asString(Exchange exchange, Object value) {
        return exchange.getContext().getTypeConverter().convertTo(String.class, value);
    }

    /**
     * Strategy to allow filtering of headers which are put on the Mail message
     */
    protected boolean shouldOutputHeader(org.apache.camel.Message camelMessage, String headerName,
                                         Object headerValue) {
        return true;
    }

    /**
     * Strategy to allow filtering of attachments which are put on the Mail message
     */
    protected boolean shouldOutputAttachment(org.apache.camel.Message camelMessage, String headerName,
                                             DataHandler headerValue) {
        return true;
    }
}
