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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.ExchangeHelper;

/**
 * Unmarshals the body of the incoming message using the given
 * <a href="http://camel.apache.org/data-format.html">data format</a>
 *
 * @version $Revision$
 */
public class UnmarshalProcessor implements Processor {
    private final DataFormat dataFormat;

    public UnmarshalProcessor(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public void process(Exchange exchange) throws Exception {
        InputStream stream = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
        try {
            // lets setup the out message before we invoke the dataFormat
            // so that it can mutate it if necessary
            Message out = exchange.getOut(true);
            out.copyFrom(exchange.getIn());

            Object result = dataFormat.unmarshal(exchange, stream);
            out.setBody(result);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}