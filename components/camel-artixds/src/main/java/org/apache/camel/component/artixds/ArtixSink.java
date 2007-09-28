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
package org.apache.camel.component.artixds;

import java.io.ByteArrayOutputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.presentation.BinarySink;
import biz.c24.io.api.presentation.JavaClassSink;
import biz.c24.io.api.presentation.SAXSink;
import biz.c24.io.api.presentation.Sink;
import biz.c24.io.api.presentation.TagValuePairSink;
import biz.c24.io.api.presentation.TextualSink;
import biz.c24.io.api.presentation.XMLSink;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;

/**
 * Transforms an Artix data object into some output format
 *
 * @version $Revision: 1.1 $
 */
public class ArtixSink extends ArtixSource {
    private Sink sink;


    public static ArtixSink artixSink() {
        return new ArtixSink();
    }

    public static ArtixSink artixSink(Sink sink) {
        return new ArtixSink(sink);
    }

    public ArtixSink() {
    }

    public ArtixSink(Sink sink) {
        this.sink = sink;
    }

    public void process(Exchange exchange) throws Exception {
        ComplexDataObject dataObject = ExchangeHelper.getMandatoryInBody(exchange, ComplexDataObject.class);

        dataObject = transformDataObject(exchange, dataObject);

        Sink s = getSink();
        if (s == null) {
            s = dataObject.getModel().sink();
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        s.setOutputStream(buffer);
        s.writeObject(dataObject);

        Message out = exchange.getOut(true);
        out.setHeader("org.apache.camel.artixds.sink", sink);
        out.setBody(buffer.toByteArray());
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    // Fluent API
    //-------------------------------------------------------------------------

    /**
     * Sets the output sink to be text
     */
    public ArtixSink text() {
        setSink(new TextualSink());
        return this;
    }

    /**
     * Sets the output sink to be binary
     */
    public ArtixSink binary() {
        setSink(new BinarySink());
        return this;
    }

    /**
     * Sets the output sink to be XML
     */
    public ArtixSink xml() {
        setSink(new XMLSink());
        return this;
    }

    /**
     * Sets the output sink to be SAX
     */
    public ArtixSink sax() {
        setSink(new SAXSink());
        return this;
    }

    /**
     * Sets the output sink to be tag value pair
     */
    public ArtixSink tagValuePair() {
        setSink(new TagValuePairSink());
        return this;
    }

    /**
     * Sets the output sink to be Java
     */
    public ArtixSink java() {
        setSink(new JavaClassSink());
        return this;
    }



    protected ComplexDataObject transformDataObject(Exchange exchange, ComplexDataObject dataObject) throws ValidationException {
        return dataObject;
    }
}