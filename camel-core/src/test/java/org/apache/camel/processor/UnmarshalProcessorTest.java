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
package org.apache.camel.processor;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.TestSupport;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.spi.DataFormat;

public class UnmarshalProcessorTest extends TestSupport {

    public void testDataFormatReturnsSameExchange() throws Exception {
        Exchange exchange = createExchangeWithBody(new DefaultCamelContext(), "body");
        Processor processor = new UnmarshalProcessor(new MyDataFormat(exchange));

        processor.process(exchange);

        // as the process method call above acts as noop there's nothing to assert on
    }

    public void testDataFormatReturnsAnotherExchange() throws Exception {
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = createExchangeWithBody(context, "body");
        Exchange exchange2 = createExchangeWithBody(context, "body2");
        Processor processor = new UnmarshalProcessor(new MyDataFormat(exchange2));

        try {
            processor.process(exchange);
            fail("Should have thrown exception");
        } catch (RuntimeCamelException e) {
            assertEquals("The returned exchange " + exchange2 + " is not the same as " + exchange + " provided to the DataFormat", e.getMessage());
        }
    }

    public void testDataFormatReturnsMessage() throws Exception {
        Exchange exchange = createExchangeWithBody(new DefaultCamelContext(), "body");
        Message out = new DefaultMessage();
        out.setBody(new Object());
        Processor processor = new UnmarshalProcessor(new MyDataFormat(out));

        processor.process(exchange);
        assertSame("UnmarshalProcessor did not make use of the returned OUT message", out, exchange.getOut());
        assertSame("UnmarshalProcessor did change the body bound to the OUT message", out.getBody(), exchange.getOut().getBody());
    }

    public void testDataFormatReturnsBody() throws Exception {
        Exchange exchange = createExchangeWithBody(new DefaultCamelContext(), "body");
        Object unmarshalled = new Object();
        Processor processor = new UnmarshalProcessor(new MyDataFormat(unmarshalled));

        processor.process(exchange);
        assertSame("UnmarshalProcessor did not make use of the returned object being returned while unmarshalling", unmarshalled, exchange.getOut().getBody());
    }

    private static class MyDataFormat implements DataFormat {

        private final Object object;

        MyDataFormat(Exchange exchange) {
            object = exchange;
        }

        MyDataFormat(Message message) {
            object = message;
        }

        MyDataFormat(Object unmarshalled) {
            object = unmarshalled;
        }

        @Override
        public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
            throw new IllegalAccessException("This method is not expected to be used by UnmarshalProcessor");
        }

        @Override
        public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
            return object;
        }
    }

}
