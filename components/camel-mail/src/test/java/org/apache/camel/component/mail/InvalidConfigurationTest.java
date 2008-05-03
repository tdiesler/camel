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

import javax.mail.NoSuchProviderException;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.PollingConsumer;

/**
 * Unit test for various invalid configurations etc.
 */
public class InvalidConfigurationTest extends ContextTestSupport {

    public void testSMTPCanNotBeUsedForConsumingMails() throws Exception {
        Endpoint endpoint = this.context.getEndpoint("smtp://localhost?username=james");
        PollingConsumer consumer = endpoint.createPollingConsumer();
        try {
            consumer.start();
            fail("Should have thrown NoSuchProviderException as stmp protocol can not be used for consuming mails");
        } catch (NoSuchProviderException e) {
            // expected
        }
    }

}
