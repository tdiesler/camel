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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.presentation.Sink;
import biz.c24.io.api.presentation.Source;
import org.apache.camel.Exchange;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterAware;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Auto-detect {@link ComplexDataObject} instances from the
 * <a href="http://activemq.apache.org/camel/artix-data-services.html">Artix Data Services</a> and allow them to be
 * transformed to and from sources and sinks.
 *
 * @version $Revision$
 */
public class FallbackTypeConverter implements TypeConverter, TypeConverterAware {
    private static final transient Log LOG = LogFactory.getLog(FallbackTypeConverter.class);
    private TypeConverter parentTypeConverter;
    private boolean prettyPrint = true;
    private Sink sink;

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setTypeConverter(TypeConverter parentTypeConverter) {
        this.parentTypeConverter = parentTypeConverter;
    }

    public <T> T convertTo(Class<T> type, Object value) {
        return convertTo(type, value, null);
    }

    public <T> T convertTo(Class<T> type, Object value, Exchange exchange) {
        try {
            if (isComplexDataObject(type)) {
                return unmarshall(type, value, exchange);
            }
            if (value instanceof ComplexDataObject) {
                marshall(type, (ComplexDataObject) value, exchange);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeCamelException(e);
        }
    }

    protected <T> boolean isComplexDataObject(Class<T> type) {
        return type.isAssignableFrom(ComplexDataObject.class);
    }

    /**
     * Lets try parse via JAXB
     */
    protected <T> T unmarshall(Class<T> type, Object value, Exchange exchange) throws IOException {
        Element element = getElementForType(type, exchange);
        if (element == null) {
            return null;
        }

        Source source = getSource(type, element, exchange);

        boolean configured = false;
        if (parentTypeConverter != null) {
            try {
                InputStream inputStream = parentTypeConverter.convertTo(InputStream.class, value);
                source.setInputStream(inputStream);
                configured = true;
            } catch (NoTypeConversionAvailableException ex1) {
                try {
                    Reader reader = parentTypeConverter.convertTo(Reader.class, value);
                    source.setReader(reader);
                    configured = true;
                } catch (NoTypeConversionAvailableException ex2) {
                    // do nothing here
                }
            }
 
            if (!configured) {
                if (value instanceof String) {
                    value = new StringReader((String) value);
                }
                if (value instanceof InputStream) {
                    source.setInputStream((InputStream) value);
                    configured = true;
                }
                if (value instanceof Reader) {
                    source.setReader((Reader) value);
                    configured = true;
                }
            }
        }
        if (configured) {
            ComplexDataObject object = source.readObject(element);
            return ObjectHelper.cast(type, object);
        } else {
            return null;
        }
    }

    protected Element getElementForType(Class<?> type, Exchange exchange) {
        return ArtixDSHelper.getElement(type);
    }

    protected <T> T marshall(Class<T> type, ComplexDataObject dataObject, Exchange exchange) throws IOException {
        if (parentTypeConverter != null) {
            // TODO allow configuration to determine the sink from the Exchange

            Sink sink = getSink(dataObject, exchange);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            sink.setOutputStream(buffer);
            sink.writeObject(dataObject);

            byte[] data = buffer.toByteArray();

            try {
                return parentTypeConverter.convertTo(type, data);
            } catch (NoTypeConversionAvailableException e) {
                return null;
            }
        }

        return null;
    }

    protected Source getSource(Class<?> type, Element element, Exchange exchange) {
        Source answer = null;
        if (exchange != null) {
            answer = exchange.getProperty("org.apache.camel.artixds.source", Source.class);
        }
        if (answer == null) {
            answer = element.getModel().source();
        }
        return answer;
    }

    protected Sink getSink(ComplexDataObject dataObject, Exchange exchange) {
        Sink answer = null;
        if (exchange != null) {
            answer = exchange.getProperty("org.apache.camel.artixds.sink", Sink.class);
        }
        if (answer == null) {
            answer = dataObject.getModel().sink();
        }
        return answer;
    }

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        return convertTo(type, value, exchange);
    }
}
