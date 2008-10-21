/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.fix;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.InvalidTypeException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Message;

/**
 * @version $Revision$
 */
public class FixProducer extends DefaultProducer {
    private static final transient Log LOG = LogFactory.getLog(FixProducer.class);
    private final FixEndpoint endpoint;

    public FixProducer(FixEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        Message message = toQuickMessage(exchange);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending FIX message : " + message);
        }
        endpoint.getSession().send(message);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sent FIX message : " + message);
        }
    }

    protected Message toQuickMessage(Exchange exchange) throws InvalidPayloadException, InvalidTypeException, IOException {
        return ExchangeHelper.getMandatoryInBody(exchange, Message.class);
    }
}
