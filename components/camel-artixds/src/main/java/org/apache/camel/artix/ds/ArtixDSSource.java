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
package org.apache.camel.artix.ds;

import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.presentation.Source;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.api.presentation.XMLSource;
import biz.c24.io.api.presentation.SAXSource;
import biz.c24.io.api.presentation.BinarySource;
import biz.c24.io.api.presentation.JavaClassSource;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.util.ExchangeHelper;

/**
 * A parser of objects using the Artix Data Services
 *
 * @version $Revision$
 */
public class ArtixDSSource<T extends ArtixDSSource> implements Processor {
    private Element element;
    private Source source;

    public static ArtixDSSource adsSource(String modelClassName) {
        try {
            Class<Element> elementType = (Class<Element>) Class.forName(modelClassName);
            return adsSource(elementType);
        }
        catch (RuntimeCamelException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    public static ArtixDSSource adsSource(Class<?> elementType) {
        try {
            Element element = (Element) elementType.getMethod("getInstance", null).invoke(null, null);
            return adsSource(element);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeCamelException(e.getTargetException());
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    public static ArtixDSSource adsSource(Element element) {
        return new ArtixDSSource(element);
    }

    public ArtixDSSource() {
    }

    public ArtixDSSource(Element element) {
        this.element = element;
    }

    public void process(Exchange exchange) throws Exception {
        ComplexDataObject object = parseDataObject(exchange);

        Message out = exchange.getOut(true);
        out.setHeader("org.apache.camel.artixds.element", element);
        out.setBody(object);
    }

    protected ComplexDataObject parseDataObject(Exchange exchange) throws InvalidPayloadException, IOException {
        Source source = getSource();

        // lets set the input stream
        Reader reader = exchange.getIn().getBody(Reader.class);
        if (reader != null) {
            source.setReader(reader);
        }
        else {
            // TODO have some SAXSource handling code here?

            InputStream inStream = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
            source.setInputStream(inStream);
        }
        ComplexDataObject object = source.readObject(element);
        return object;
    }

    public static Element element(String modelClassName) {
        try {
            Class<?> elementType = Class.forName(modelClassName);
            return element(elementType);
        }
        catch (RuntimeCamelException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    public static Element element(Class<?> elementType) {
        try {
            return (Element) elementType.getMethod("getInstance", null).invoke(null, null);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeCamelException(e.getTargetException());
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public Element getElement() {
        return element;
    }

    public Source getSource() {
        if (source == null) {
            return getElement().getModel().source();
        }
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * Sets the source parser to text
     */
    public T textSource() {
        setSource(new TextualSource());
        return (T) this;
    }

    /**
     * Sets the source parser to XML
     */
    public T xmlSource() {
        setSource(new XMLSource());
        return (T) this;
    }

    /**
     * Sets the source parser to SAX
     */
    public T saxSource() {
        setSource(new SAXSource());
        return (T) this;
    }

    /**
     * Sets the source parser to XML
     */
    public T binarySource() {
        setSource(new BinarySource());
        return (T) this;
    }

    /**
     * Sets the source parser to XML
     */
    public T javaSource() {
        setSource(new JavaClassSource());
        return (T) this;
    }
}
