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
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import org.apache.cxf.message.Exchange;

/**
 * Interface to bind between Camel and CXF exchange for RESTful resources.
 *
 * @version $Revision$
 */
public interface CxfRsBinding {
    
    /**
     * Populate the camel exchange from the CxfRsRequest, the exchange will be consumed 
     * by the processor which the CxfRsConsumer attached.
     * 
     * @param camelExchange  camel exchange object
     * @param cxfExchange  cxf exchange object 
     * @param method  the method which is need for the camel component
     * @param paramArray  the parameter list for the method invocation 
     */
    void populateExchangeFromCxfRsRequest(Exchange cxfExchange,
                              org.apache.camel.Exchange camelExchange,
                              Method method,
                              Object[] paramArray);    
    
    /**
     * Populate the CxfRsResponse object from the camel exchange
     * @param camelExchange  camel exchange object
     * @param cxfExchange  cxf exchange object 
     * @return the response object
     * @throws Exception 
     */
    Object populateCxfRsResponseFromExchange(org.apache.camel.Exchange camelExchange,
                               Exchange cxfExchange) throws Exception;
    
    /**
     * Bind the camel in message body to a request body that gets passed
     * to CXF RS {@link org.apache.cxf.jaxrs.client.WebClient} APIs.
     * 
     * @param camelMessage the source message
     * @param camelExchange the Camel exchange
     * @return the request object to be passed to invoke a WebClient
     * @throws Exception
     */
    Object bindCamelMessageBodyToRequestBody(org.apache.camel.Message camelMessage,
                                             org.apache.camel.Exchange camelExchange) 
        throws Exception;
    
    /**
     * Bind the camel headers to request headers that gets passed to CXF RS
     * {@link org.apache.cxf.jaxrs.client.WebClient} APIs.
     * 
     * @param camelHeaders the source headers
     * @param camelExchange the Camel exchange
     * @param headers to be passed to WebClient
     * @throws Exception
     */
    MultivaluedMap<String, String> bindCamelHeadersToRequestHeaders(Map<String, Object> camelHeaders,
                                                                    org.apache.camel.Exchange camelExchange) 
       throws Exception;
                                                         
    
    /**
     * Bind the HTTP response body to camel out body
     * @param response
     * @param exchange
     * @return the object to be set in the Camel out message body
     */
    Object bindResponseToCamelBody(Object response, org.apache.camel.Exchange camelExchange)
        throws Exception;
    
    /**
     * Bind the response headers to camel out headers.
     * 
     * @param response
     * @param exchange
     * @return headers to be set in the Camel out message
     * @throws Exception
     */
    Map<String, Object> bindResponseHeadersToCamelHeaders(Object response, 
                                                          org.apache.camel.Exchange exchange)
        throws Exception;

}
