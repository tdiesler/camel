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

package org.apache.camel.component.cxf.jaxrs;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.cxf.CxfConstants;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServiceFactoryBean;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * CxfRsProducer binds a Camel exchange to a CXF exchange, acts as a CXF 
 * JAXRS client, it will turn the normal Object invocation to a RESTful request
 * according to resource annotation.  Any response will be bound to Camel exchange. 
 */
public class CxfRsProducer extends DefaultProducer {
    
    private static final Log LOG = LogFactory.getLog(CxfRsProducer.class);

    JAXRSClientFactoryBean cfb;

    public CxfRsProducer(CxfRsEndpoint endpoint) {
        super(endpoint);
        cfb = endpoint.createJAXRSClientFactoryBean();
    }

    public void process(Exchange exchange) throws Exception {
        
        if (LOG.isTraceEnabled()) {
            LOG.trace("Process exchange: " + exchange);
        }
        
        Message inMessage = exchange.getIn();
        Boolean httpClientAPI = inMessage.getHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.class);
        // set the value with endpoint's option
        if (httpClientAPI == null) {
            httpClientAPI = ((CxfRsEndpoint)getEndpoint()).isHttpClientAPI();
        }
        if (httpClientAPI.booleanValue()) {
            invokeHttpClient(exchange);
        } else {
            invokeProxyClient(exchange);            
        }
        
    }
    
    @SuppressWarnings("unchecked")
    protected void invokeHttpClient(Exchange exchange) throws Exception {
        Message inMessage = exchange.getIn();       
        WebClient client = cfb.createWebClient();
        
        String httpMethod = inMessage.getHeader(Exchange.HTTP_METHOD, String.class); 
        Class responseClass = inMessage.getHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Class.class);        
        String path = inMessage.getHeader(Exchange.HTTP_PATH, String.class);
       
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP method = " + httpMethod);
            LOG.trace("path = " + path);
            LOG.trace("responseClass = " + responseClass);
        }
        
        // set the path
        if (path != null) {
            client.path(path);
        }
        
        CxfRsEndpoint cxfRsEndpoint = (CxfRsEndpoint)getEndpoint();
        // check if there is a query map in the message header
        Map<String, String> maps = inMessage.getHeader(CxfConstants.CAMEL_CXF_RS_QUERY_MAP, Map.class);
        if (maps == null) {            
            maps = cxfRsEndpoint.getParameters();
        }
        if (maps != null) {
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                client.query(entry.getKey(), entry.getValue());
            }            
        }
        
        CxfRsBinding binding = cxfRsEndpoint.getBinding();

        // set the body
        Object body = null;
        if (!"GET".equals(httpMethod)) {
            // need to check the request object.           
            body = binding.bindCamelMessageBodyToRequestBody(inMessage, exchange);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Request body = " + body);
            }
        }
        
        // set headers
        client.headers(binding.bindCamelHeadersToRequestHeaders(inMessage.getHeaders(),
                                                                exchange));
        
        // invoke the client
        Object response = null;        
        if (responseClass == null || Response.class.equals(responseClass)) {
            response = client.invoke(httpMethod, body);
        } else {
            response = client.invoke(httpMethod, body, responseClass);
        }
        
        // set response
        if (exchange.getPattern().isOutCapable()) {     
            if (LOG.isTraceEnabled()) {
                LOG.trace("Response body = " + response);
            }
            
            exchange.getOut().setBody(binding.bindResponseToCamelBody(response, exchange));
            exchange.getOut().setHeaders(binding.bindResponseHeadersToCamelHeaders(response, exchange));
        }
    }

    protected void invokeProxyClient(Exchange exchange) throws Exception {
        Message inMessage = exchange.getIn();
        Object[] varValues = inMessage.getHeader(CxfConstants.CAMEL_CXF_RS_VAR_VALUES, Object[].class);
        String methodName = inMessage.getHeader(CxfConstants.OPERATION_NAME, String.class);
        Client target = null;
        if (varValues == null) {
            target = cfb.create();
        } else {
            target = cfb.createWithValues(varValues);
        }    
        // find out the method which we want to invoke
        JAXRSServiceFactoryBean sfb = cfb.getServiceFactory();
        sfb.getResourceClasses();
        Object[] parameters = inMessage.getBody(Object[].class);
        // get the method
        Method method = findRightMethod(sfb.getResourceClasses(), methodName, getParameterTypes(parameters));
        // Will send out the message to
        // Need to deal with the sub resource class
        Object response = method.invoke(target, parameters);
        if (exchange.getPattern().isOutCapable()) {
            exchange.getOut().setBody(response);
        }
    }

    @SuppressWarnings("unchecked")
    private Method findRightMethod(List<Class<?>> resourceClasses, String methodName, Class[] parameterTypes) throws NoSuchMethodException {        
        Method answer = null;
        for (Class<?> clazz : resourceClasses) {
            try {
                answer = clazz.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ex) {
                // keep looking 
            } catch (SecurityException ex) {
                // keep looking
            }
            if (answer != null) {
                return answer;
            }
        }
        throw new NoSuchMethodException("Can find the method " + methodName 
            + "withe these parameter " + arrayToString(parameterTypes));
    }
    
    
    private Class<?>[] getParameterTypes(Object[] objects) {
        Class<?>[] answer = new Class[objects.length];
        int i = 0;
        for (Object obj : objects) {
            answer[i] = obj.getClass();
            i++;
        }
        return answer;
    }
    
    private String arrayToString(Object[] array) {
        StringBuffer buffer = new StringBuffer("[");
        for (Object obj : array) {
            if (buffer.length() > 2) {
                buffer.append(",");
            }
            buffer.append(obj.toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

}
