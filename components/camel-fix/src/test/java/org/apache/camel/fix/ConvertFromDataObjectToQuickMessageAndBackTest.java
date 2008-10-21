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

import java.io.FileInputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.fix42.NewOrderSingleElement;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.impl.DefaultExchange;
import static org.apache.camel.util.ExchangeHelper.getMandatoryInBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Message;

/**
 * @version $Revision$
 */
public class ConvertFromDataObjectToQuickMessageAndBackTest extends ConvertFromDataObjectToQuickFixToFromStringTest {

    @Override
    public void testConvertToAndFromQuickTest() throws Exception {
        TextualSource src = new TextualSource(new FileInputStream("src/test/data/nos.txt"));
        ComplexDataObject dataObject = src.readObject(NewOrderSingleElement.getInstance());

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(dataObject);

        setToMandatoryType(exchange, ComplexDataObject.class);

        setToMandatoryType(exchange, Message.class);
        setToMandatoryType(exchange, ComplexDataObject.class);
        setToMandatoryType(exchange, Message.class);
    }
}
