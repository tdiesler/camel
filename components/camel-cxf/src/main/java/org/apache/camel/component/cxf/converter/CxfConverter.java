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
package org.apache.camel.component.cxf.converter;

import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;

import org.apache.camel.Converter;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.FallbackConverter;
import org.apache.camel.TypeConverter;
import org.apache.camel.component.cxf.CxfSpringEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.apache.camel.component.cxf.spring.CxfEndpointBeanDefinitionParser.CxfSpringEndpointBean;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.MessageContentsList;

/**
 * The <a href="http://camel.apache.org/type-converter.html">Type Converters</a>
 * for CXF related types' converting .
 *
 * @version $Revision$
 */
@Converter
public final class CxfConverter {
    private static final Log LOG = LogFactory.getLog(CxfConverter.class);

    private CxfConverter() {
        // Helper class
    }

    @Converter
    public static Object[] toArray(final MessageContentsList list) throws Exception {
        if (list == null) {
            throw new IllegalArgumentException("The MessageChannel is null");
        }
        return list.toArray();
    }

    @Converter
    public static MessageContentsList toMessageContentsList(final Object[] array) {
        if (array != null) {
            return new MessageContentsList(array);
        } else {
            return new MessageContentsList();
        }
    }

    @Converter
    public static String soapMessageToString(final SOAPMessage soapMessage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            soapMessage.writeTo(baos);
        } catch (Exception e) {
            LOG.error("Get the exception when converting the SOAPMessage into String, the exception is " + e);
        }
        return baos.toString();
    }
    
    @Converter
    public static Endpoint toEndpoint(final CxfSpringEndpointBean endpointBean) throws Exception {
        if (endpointBean == null) {
            throw new IllegalArgumentException("The CxfEndpoint instance is null");
        }
        // CamelContext 
        SpringCamelContext context = SpringCamelContext.springCamelContext(endpointBean.getApplicationContext());
        // The beanId will be set from endpointBean's property        
        Endpoint answer = new CxfSpringEndpoint(context, endpointBean);        
        return answer;
    }

    @Converter
    public static DataFormat toDataFormat(final String name) {
        return DataFormat.valueOf(name.toUpperCase());
    }

    /**
     * Use a fallback type converter so we can convert the embedded list element 
     * if the value is MessageContentsList.  The algorithm of this converter
     * finds the first non-null list element from the list and applies convertion
     * to the list element.
     * 
     * @param type the desired type to be converted to
     * @param exchange optional exchange which can be null
     * @param value the object to be converted
     * @param registry type converter registry
     * @return the converted value of the desired type or null if no suitable converter found
     */
    @FallbackConverter
    public static <T> T convertTo(Class<T> type, Exchange exchange, Object value, 
            TypeConverterRegistry registry) {
        
        if (MessageContentsList.class.isAssignableFrom(value.getClass())) {
            MessageContentsList list = (MessageContentsList)value;
            
            for (int i = 0; i < list.size(); i++) {
                Object embedded = list.get(i);
                
                if (embedded != null) {
                    if (type.isInstance(embedded)) {
                        return type.cast(embedded);
                    } else {
                        TypeConverter tc = registry.lookup(type, embedded.getClass());
                        if (tc != null) {
                            return tc.convertTo(type, exchange, embedded);
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
