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
package org.apache.camel.artix.ds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.BinarySink;
import biz.c24.io.api.presentation.JavaClassSink;
import biz.c24.io.api.presentation.SAXSink;
import biz.c24.io.api.presentation.Sink;
import biz.c24.io.api.presentation.TagValuePairSink;
import biz.c24.io.api.presentation.TextualSink;
import biz.c24.io.api.presentation.XMLSink;
import biz.c24.io.api.presentation.swift.SwiftPreParser;

import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.ArtixDSContentType;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.ExchangeHelper;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * A {@link DataFormat} using SWIFT with
 * <a href="http://activemq.apache.org/camel/artix-data-services.html">Artix Data Services</a>
 *
 * @version $Revision$
 */
public class SwiftFormat implements DataFormat {
    private final SwiftPreParser swiftParser;
    private Sink sink;
    private ArtixDSContentType contentType;

    public SwiftFormat() {
        this(new SwiftPreParser());
    }

    public SwiftFormat(SwiftPreParser swiftParser) {
        this.swiftParser = swiftParser;
    }

    public SwiftFormat(SwiftPreParser swiftParser, Sink sink) {
        this(swiftParser);
        this.sink = sink;
    }

    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        ComplexDataObject dataObject = ExchangeHelper.convertToMandatoryType(exchange, ComplexDataObject.class, graph);
        Sink s = getSink(exchange);
        notNull(s, "sink or element");
        s.setOutputStream(stream);
        s.writeObject(dataObject);
    }

    public Object unmarshal(Exchange exchange, InputStream stream) throws IOException {
        swiftParser.setInputStream(stream);
        return swiftParser.readObject();
    }

    public Sink getSink(Exchange exchange) {
        if (sink == null) {
            ArtixDSContentType content = getContentType();
            if (content != null) {
                sink = createSink(content);
            }
            if (sink == null) {
                // lets default to the one from the element
                sink = createDefaultSink();
            }
        }
        return sink;
    }

    // Properties
    //-------------------------------------------------------------------------
    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public ArtixDSContentType getContentType() {
        return contentType;
    }

    public void setContentType(ArtixDSContentType contentType) {
        this.contentType = contentType;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected Sink createSink(ArtixDSContentType content) {
        switch (content) {
        case Default:
            return createDefaultSink();
        case Binary:
            return new BinarySink();
        case Java:
            return new JavaClassSink();
        case Sax:
            return new SAXSink();
        case Text:
            return new TextualSink();
        case Xml:
            return new XMLSink();
        case TagValuePair:
            return new TagValuePairSink();
        default:
            throw new IllegalArgumentException("Unknown format type: " + content);
        }
    }

    protected Sink createDefaultSink() {
        return new TextualSink();
    }
}
