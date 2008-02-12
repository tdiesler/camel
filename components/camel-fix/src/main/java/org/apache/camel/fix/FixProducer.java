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

import biz.c24.io.api.data.ComplexDataObject;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Message;
import quickfix.Session;

/**
 * @version $Revision: 1.1 $
 */
public class FixProducer extends DefaultProducer {
    private static final transient Log LOG = LogFactory.getLog(FixProducer.class);

    private final FixEndpoint endpoint;

    public FixProducer(FixEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        org.apache.camel.Message in = exchange.getIn();
        Message message = in.getBody(Message.class);
        if (message == null) {
            ComplexDataObject dataObject = ExchangeHelper.getMandatoryInBody(exchange, ComplexDataObject.class);
            message = ExchangeHelper.convertToMandatoryType(exchange, Message.class, dataObject);
        }

        LOG.info("Sending FIX message : " + message);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending FIX message : " + message);
        }
        endpoint.getSession().send(message);
        LOG.info("Sent FIX message : " + message);
    }
}
