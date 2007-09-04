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
import javax.mail.MessagingException;
import javax.mail.Store;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * An extension of Spring's {@link JavaMailSenderImpl} to provide helper methods
 * for listening for new mail
 * 
 * @version $Revision: 1.1 $
 */
public class JavaMailConnection extends JavaMailSenderImpl {

    public Folder getFolder(String protocol, String folderName) {
        try {
            Store store = getSession().getStore(protocol);
            store.connect(getHost(), getPort(), getUsername(), getPassword());
            return store.getFolder(folderName);
        } catch (MessagingException e) {
            throw new MailSendException("Mail server connection failed", e);
        }
    }
}
