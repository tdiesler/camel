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
package org.apache.camel.component.cxf;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CxfProducer binds a Camel exchange to a CXF exchange, acts as a CXF 
 * client, and sends the request to a CXF to a server.  Any response will 
 * be bound to Camel exchange. 
 *
 * @version 
 */
public class CxfProducer extends DefaultProducer implements AsyncProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(CxfProducer.class);
    private Client client;
    private CxfEndpoint endpoint;

    /**
     * Constructor to create a CxfProducer.  It will create a CXF client
     * object.
     * 
     * @param endpoint a CxfEndpoint that creates this producer
     * @throws Exception any exception thrown during the creation of a 
     * CXF client
     */
    public CxfProducer(CxfEndpoint endpoint) throws Exception {
        super(endpoint);
        this.endpoint = endpoint;
        client = endpoint.createClient();
    }
   
    // As the cxf client async and sync api is implement different,
    // so we don't delegate the sync process call to the async process 
    public boolean process(Exchange camelExchange, AsyncCallback callback) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Process exchange: " + camelExchange + " in an async way.");
        }
        
        try {
            // create CXF exchange
            ExchangeImpl cxfExchange = new ExchangeImpl();
            // set the Bus on the exchange in case the CXF interceptor need to access it from exchange
            cxfExchange.put(Bus.class, endpoint.getBus());
            
            // prepare binding operation info
            BindingOperationInfo boi = prepareBindingOperation(camelExchange, cxfExchange);
            
            Map<String, Object> invocationContext = new HashMap<String, Object>();
            Map<String, Object> responseContext = new HashMap<String, Object>();
            invocationContext.put(Client.RESPONSE_CONTEXT, responseContext);
            invocationContext.put(Client.REQUEST_CONTEXT, prepareRequest(camelExchange, cxfExchange));
            
            CxfClientCallback cxfClientCallback = new CxfClientCallback(callback, camelExchange, cxfExchange, boi, endpoint);
            // send the CXF async request
            client.invoke(cxfClientCallback, boi, getParams(endpoint, camelExchange), 
                          invocationContext, cxfExchange);
            if (boi.getOperationInfo().isOneWay()) {
                callback.done(false);
            }
        } catch (Throwable ex) {
            // error occurred before we had a chance to go async
            // so set exception and invoke callback true
            camelExchange.setException(ex);
            callback.done(true);
            return true;
        }
        return false;
    }

    /**
     * This processor binds Camel exchange to a CXF exchange and
     * invokes the CXF client.
     */
    public void process(Exchange camelExchange) throws Exception {
        
        if (LOG.isTraceEnabled()) {
            LOG.trace("Process exchange: " + camelExchange + "in sync way.");
        }
        
        // create CXF exchange
        ExchangeImpl cxfExchange = new ExchangeImpl();
        // set the Bus on the exchange in case the CXF interceptor need to access it from exchange
        cxfExchange.put(Bus.class, endpoint.getBus());
        
        // prepare binding operation info
        BindingOperationInfo boi = prepareBindingOperation(camelExchange, cxfExchange);
        
        Map<String, Object> invocationContext = new HashMap<String, Object>();
        Map<String, Object> responseContext = new HashMap<String, Object>();
        invocationContext.put(Client.RESPONSE_CONTEXT, responseContext);
        invocationContext.put(Client.REQUEST_CONTEXT, prepareRequest(camelExchange, cxfExchange));
        
        try {
            // send the CXF request
            client.invoke(boi, getParams(endpoint, camelExchange), 
                      invocationContext, cxfExchange);
        } catch (Exception exception) {
            camelExchange.setException(exception);
        } finally {
            // bind the CXF response to Camel exchange
            if (!boi.getOperationInfo().isOneWay()) {
                // copy the InMessage header to OutMessage header
                camelExchange.getOut().getHeaders().putAll(camelExchange.getIn().getHeaders());
                endpoint.getCxfBinding().populateExchangeFromCxfResponse(camelExchange, cxfExchange,
                        responseContext);
            }
        }
    }
    
    protected Map<String, Object> prepareRequest(Exchange camelExchange, org.apache.cxf.message.Exchange cxfExchange) throws Exception {
        
        // create invocation context
        WrappedMessageContext requestContext = new WrappedMessageContext(
                new HashMap<String, Object>(), null, Scope.APPLICATION);

        camelExchange.setProperty(Message.MTOM_ENABLED, String.valueOf(endpoint.isMtomEnabled()));
        
        // set data format mode in exchange
        DataFormat dataFormat = endpoint.getDataFormat();
        camelExchange.setProperty(CxfConstants.DATA_FORMAT_PROPERTY, dataFormat);   
        if (LOG.isTraceEnabled()) {
            LOG.trace("Set Camel Exchange property: " + DataFormat.class.getName() 
                    + "=" + dataFormat);
        }
        
        // set data format mode in the request context
        requestContext.put(DataFormat.class.getName(), dataFormat);

        // don't let CXF ClientImpl close the input stream 
        if (dataFormat == DataFormat.MESSAGE) {
            cxfExchange.put(Client.KEEP_CONDUIT_ALIVE, true);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Set CXF Exchange property: " + Client.KEEP_CONDUIT_ALIVE  
                        + "=" + true);
            }
        }
     
        // bind the request CXF exchange
        endpoint.getCxfBinding().populateCxfRequestFromExchange(cxfExchange, camelExchange, 
                requestContext);
        
        // Remove protocol headers from scopes.  Otherwise, response headers can be
        // overwritten by request headers when SOAPHandlerInterceptor tries to create
        // a wrapped message context by the copyScoped() method.
        requestContext.getScopes().remove(Message.PROTOCOL_HEADERS);
        
        return requestContext.getWrappedMap();
    }
    
    private BindingOperationInfo prepareBindingOperation(Exchange camelExchange, org.apache.cxf.message.Exchange cxfExchange) {
        // get binding operation info
        BindingOperationInfo boi = getBindingOperationInfo(camelExchange);
        ObjectHelper.notNull(boi, "BindingOperationInfo");
        
        // keep the message wrapper in PAYLOAD mode
        if (endpoint.getDataFormat() == DataFormat.PAYLOAD && boi.isUnwrapped()) {
            boi = boi.getWrappedOperation();
            cxfExchange.put(BindingOperationInfo.class, boi);
            
        } 
        
        // store the original boi in the exchange
        camelExchange.setProperty(BindingOperationInfo.class.getName(), boi);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Set exchange property: BindingOperationInfo: " + boi);
        }

        // Unwrap boi before passing it to make a client call
        if (endpoint.getDataFormat() != DataFormat.PAYLOAD && !endpoint.isWrapped() && boi != null) {
            if (boi.isUnwrappedCapable()) {
                boi = boi.getUnwrappedOperation();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Unwrapped BOI " + boi);
                }
            }
        }
        return  boi;
    }
    
    private void checkParameterSize(CxfEndpoint endpoint, Exchange exchange, Object[] parameters) {
        BindingOperationInfo boi = getBindingOperationInfo(exchange);
        if (boi == null) {
            throw new RuntimeCamelException("Can't find the binding operation information from camel exchange");
        }
        if (!endpoint.isWrapped()) {
            if (boi.isUnwrappedCapable()) {
                boi = boi.getUnwrappedOperation();
            }
        }
        int experctMessagePartsSize = boi.getInput().getMessageParts().size();
        
        if (parameters.length < experctMessagePartsSize) {
            throw new IllegalArgumentException("Get the wrong parameter size to invoke the out service, Expect size "
                                               + experctMessagePartsSize + ", Parameter size " + parameters.length
                                               + ". Please check if the message body matches the CXFEndpoint POJO Dataformat request.");
        }
        
        if (parameters.length > experctMessagePartsSize) {
            // need to check the holder parameters        
            int holdersSize = 0;            
            for (Object parameter : parameters) {
                if (parameter instanceof Holder) {
                    holdersSize++;
                } 
            }
            // need to check the soap header information
            int soapHeadersSize = 0; 
            BindingMessageInfo bmi =  boi.getInput();
            if (bmi != null) {
                List<SoapHeaderInfo> headers = bmi.getExtensors(SoapHeaderInfo.class);
                if (headers != null) {
                    soapHeadersSize = headers.size();
                }
            }
          
            if (holdersSize + experctMessagePartsSize + soapHeadersSize < parameters.length) {
                throw new IllegalArgumentException("Get the wrong parameter size to invoke the out service, Expect size "
                                                   + (experctMessagePartsSize + holdersSize + soapHeadersSize) + ", Parameter size " + parameters.length
                                                   + ". Please check if the message body matches the CXFEndpoint POJO Dataformat request.");
            }
        }
    }

    /**
     * Get the parameters for the web service operation
     */
    private Object[] getParams(CxfEndpoint endpoint, Exchange exchange) throws InvalidPayloadException {
      
        Object[] params = null;
        if (endpoint.getDataFormat() == DataFormat.POJO) {
            List<?> list = exchange.getIn().getBody(List.class);
            if (list != null) {
                params = list.toArray();
            } else {
                // maybe we can iterate the body and that way create a list for the parameters
                // then end users do not need to trouble with List
                Iterator it = exchange.getIn().getBody(Iterator.class);
                if (it != null && it.hasNext()) {
                    list = exchange.getContext().getTypeConverter().convertTo(List.class, it);
                    if (list != null) {
                        params = list.toArray();
                    }
                }
                if (params == null) {
                    // no we could not then use the body as single parameter
                    params = new Object[1];
                    params[0] = exchange.getIn().getBody();
                }
            }
            // make sure we have the right number of parameters
            checkParameterSize(endpoint, exchange, params);

        } else if (endpoint.getDataFormat() == DataFormat.PAYLOAD) {
            params = new Object[1];
            params[0] = exchange.getIn().getMandatoryBody(CxfPayload.class);
        } else if (endpoint.getDataFormat() == DataFormat.MESSAGE) {
            params = new Object[1];
            params[0] = exchange.getIn().getMandatoryBody(InputStream.class);
        }

        if (LOG.isTraceEnabled()) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    LOG.trace("params[" + i + "] = " + params[i]);
                }
            }
        }
        
        return params;
    }

    /**
     * Get operation name from header and use it to lookup and return a 
     * {@link BindingOperationInfo}.
     */
    private BindingOperationInfo getBindingOperationInfo(Exchange ex) {
        CxfEndpoint endpoint = (CxfEndpoint)this.getEndpoint();
        BindingOperationInfo answer = null;
        String lp = ex.getIn().getHeader(CxfConstants.OPERATION_NAME, String.class);
        if (lp == null) {
            lp = endpoint.getDefaultOperationName();
        }
        if (lp == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Try to find a default operation.  You should set '" 
                        + CxfConstants.OPERATION_NAME + "' in header.");
            }
            Collection<BindingOperationInfo> bois = 
                client.getEndpoint().getEndpointInfo().getBinding().getOperations();
            
            Iterator<BindingOperationInfo> iter = bois.iterator(); 
            if (iter.hasNext()) {
                answer = iter.next();
            }
            
        } else {
            String ns = ex.getIn().getHeader(CxfConstants.OPERATION_NAMESPACE, String.class);
            if (ns == null) {
                ns = endpoint.getDefaultOperationNamespace();
            }
            if (ns == null) {
                ns = client.getEndpoint().getService().getName().getNamespaceURI();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Operation namespace not in header.  Set it to: " + ns);
                }
            }            

            QName qname = new QName(ns, lp);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Operation qname = " + qname.toString());
            }
            
            answer = client.getEndpoint().getEndpointInfo().getBinding().getOperation(qname);
            if (answer == null) {
                throw new IllegalArgumentException("Can't find the BindingOperationInfo with operation name " + qname
                                                   + ". Please check the message headers of operationName and operationNamespace."); 
            }
        }
        return answer;
    }
    
    public Client getClient() {
        return client;
    }

}
