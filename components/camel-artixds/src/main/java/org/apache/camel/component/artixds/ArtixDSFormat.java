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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.presentation.Sink;
import biz.c24.io.api.presentation.Source;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.ExchangeHelper;
import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * A {@link DataFormat} for working with Artix Data Services
 *
 * @version $Revision: 1.1 $
 */
public class ArtixDSFormat implements DataFormat {
    private Sink sink;
    private Source source;
    private Element element;

    public ArtixDSFormat() {
    }

    public ArtixDSFormat(Element element) {
        this.element = element;
    }

    public ArtixDSFormat(Element element, Source source, Sink sink) {
        this.element = element;
        this.source = source;
        this.sink = sink;
    }

    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        ComplexDataObject dataObject = ExchangeHelper.convertToMandatoryType(exchange, ComplexDataObject.class, graph);
        Sink s = getSink();
        notNull(s, "sink or element");
        s.setOutputStream(stream);
        s.writeObject(dataObject);
    }

    public Object unmarshal(Exchange exchange, InputStream stream) throws IOException {
        Source s = source;
        Element e = getElement();
        notNull(s, "source");
        notNull(e, "element");

        s.setInputStream(stream);
        return s.readObject(e);
    }

    // Properties
    //-------------------------------------------------------------------------

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Sink getSink() {
        if (sink == null) {
            // lets default to the one from the element
            Element e = getElement();
            if (e != null) {
                sink = e.getModel().sink();
            }
        }
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public Source getSource() {
        if (source == null) {
            // lets default to the one from the element
            Element e = getElement();
            if (e != null) {
                source = e.getModel().source();
            }
        }
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}