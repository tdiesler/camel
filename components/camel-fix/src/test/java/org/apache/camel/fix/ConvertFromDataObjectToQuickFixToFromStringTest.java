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
package org.apache.camel.fix;

import java.io.FileInputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.fix42.NewOrderSingleElement;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Message;

import static org.apache.camel.util.ExchangeHelper.getMandatoryInBody;

/**
 * @version $Revision$
 */
public class ConvertFromDataObjectToQuickFixToFromStringTest extends ContextTestSupport {
    private static final transient Log LOG = LogFactory.getLog(ConvertFromDataObjectToQuickMessageAndBackTest.class);

    public void testConvertToAndFromQuickTest() throws Exception {
        TextualSource src = new TextualSource(new FileInputStream("src/test/data/nos.txt"));
        ComplexDataObject dataObject = src.readObject(NewOrderSingleElement.getInstance());

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(dataObject);

        setToMandatoryType(exchange, ComplexDataObject.class);

        Message message = setToMandatoryType(exchange, Message.class);

/*
        setToMandatoryType(exchange, ComplexDataObject.class);
        setToMandatoryType(exchange, Message.class);
*/

        byte[] bytes = setToMandatoryType(exchange, byte[].class);
        setToMandatoryType(exchange, String.class);
        setToMandatoryType(exchange, Message.class);
        setToMandatoryType(exchange, byte[].class);
        setToMandatoryType(exchange, Message.class);

        LOG.debug("message: " + message);
        String text = new String(bytes);
        LOG.debug("text   : " + text);

        assertEquals("Text should be the same", message.toString(), text);

        setToMandatoryType(exchange, Message.class);
    }

    protected <T> T setToMandatoryType(Exchange exchange, Class<T> type) throws InvalidPayloadException {
        LOG.debug("Converting to: " + type);
        T value = getMandatoryInBody(exchange, type);
        assertNotNull("Should not be null!", value);
        LOG.debug("Converted to: " + value);
        exchange.getIn().setBody(value);
        return value;
    }
}