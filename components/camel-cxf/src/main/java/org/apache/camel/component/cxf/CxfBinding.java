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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.component.cxf.headers.DefaultMessageHeadersRelay;
import org.apache.camel.component.cxf.headers.Direction;
import org.apache.camel.component.cxf.headers.MessageHeadersRelay;
import org.apache.camel.component.cxf.util.CxfHeaderHelper;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;

/**
 * The binding/mapping of Camel messages to Apache CXF and back again
 *
 * @version $Revision$
 */
public final class CxfBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(CxfBinding.class);
    
    private CxfBinding() {
        // Helper class
    }
    public static Object extractBodyFromCxf(CxfExchange exchange, Message message) {
        // TODO how do we choose a format?
        return getBody(message);
    }

    protected static Object getBody(Message message) {
        Set<Class<?>> contentFormats = message.getContentFormats();
        if (contentFormats != null) {
            for (Class<?> contentFormat : contentFormats) {
                Object answer = message.getContent(contentFormat);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }

    /**
     * @deprecated please use {@link #createCxfMessage(HeaderFilterStrategy, CxfExchange)} instead
     */
    public static Message createCxfMessage(CxfExchange exchange) {
        return CxfBinding.createCxfMessage(new CxfHeaderFilterStrategy(), exchange);
    }

    public static Message createCxfMessage(HeaderFilterStrategy strategy, CxfExchange exchange) {

        Message answer = exchange.getInMessage();
        if (answer == null) {
            answer = new MessageImpl();
        }
        org.apache.camel.Message in = exchange.getIn();
      
        // Check the body if the POJO parameter list first
        Object body = in.getBody();
        if (body instanceof List) {
            // just set the operation's parameter
            answer.setContent(List.class, body);
            CxfHeaderHelper.propagateCamelToCxf(strategy, in.getHeaders(), answer);
        } else {
            // CXF uses StAX which is based on the stream API to parse the XML,
            // so the CXF transport is also based on the stream API.
            // And the interceptors are also based on the stream API,
            // so let's use an InputStream to host the CXF on wire message.
            try {
                InputStream is = in.getBody(InputStream.class);
                answer.setContent(InputStream.class, is);
            } catch (NoTypeConversionAvailableException ex2) {
                // ignore
            }
            // TODO do we propagate header the same way in non-POJO mode?
            // CxfHeaderHelper.propagateCamelToCxf(strategy, in.getHeaders(), answer);
        }

        //Ensure there is a request context, which is needed by propogateContext() below
        Map<String, Object> requestContext = CastUtils.cast((Map)answer.get(Client.REQUEST_CONTEXT));
        if (requestContext == null) {
            requestContext = new HashMap<String, Object>();
        }
        if (exchange.getExchange() != null) {
            requestContext.putAll(exchange.getExchange());
        }
        if (exchange.getProperties() != null) {
            //Allows other components to pass properties into cxf request context
            requestContext.putAll(exchange.getProperties());
        }
        
        // Make sure we don't propagate HandleChainInvoker as it can mess up JAXWS handler
        requestContext.remove(HandlerChainInvoker.class.getName());
        
        answer.put(Client.REQUEST_CONTEXT, requestContext);
        
        return answer;
    }

    /**
     * @deprecated please use {@link #storeCxfResponse(HeaderFilterStrategy, CxfExchange, Message)} instead.
     */
    public static void storeCxfResponse(CxfExchange exchange, Message response) {
        CxfBinding.storeCxfResponse(new CxfHeaderFilterStrategy(), exchange, response);
    }

    public static void storeCxfResponse(HeaderFilterStrategy strategy, CxfExchange exchange,
            Message response) {
        org.apache.camel.Message out = exchange.getOut();
        if (response != null) {
            CxfHeaderHelper.propagateCxfToCamel(strategy, response, out.getHeaders());
            if (out instanceof CxfMessage) {
                ((CxfMessage)out).setMessage(response);
            } else {
                out.setBody(response.get(List.class));
            }
            DataFormat dataFormat = (DataFormat) exchange.getProperty(CxfConstants.DATA_FORMAT_PROPERTY);
            if (dataFormat.equals(DataFormat.MESSAGE)) {
                out.setBody(response.getContent(InputStream.class));
            }
            if (dataFormat.equals(DataFormat.PAYLOAD)) {
                out.setBody(response);
            }
        }
    }

    /**
     * @deprecated Please use {@link #copyMessage(HeaderFilterStrategy, org.apache.camel.Message, Message)} instead.
     */
    public static void copyMessage(org.apache.camel.Message camelMessage, org.apache.cxf.message.Message cxfMessage) {
        CxfBinding.copyMessage(new CxfHeaderFilterStrategy(), camelMessage, cxfMessage);
    }

    // Copy the Camel message to CXF message
    public static void copyMessage(HeaderFilterStrategy strategy,
            org.apache.camel.Message camelMessage, org.apache.cxf.message.Message cxfMessage) {

        CxfHeaderHelper.propagateCamelToCxf(strategy, camelMessage.getHeaders(), cxfMessage);
        try {
            InputStream is = camelMessage.getBody(InputStream.class);
            if (is != null) {
                cxfMessage.setContent(InputStream.class, is);
            }
        } catch (NoTypeConversionAvailableException ex) {
            Object result = camelMessage.getBody();
            if (result instanceof InputStream) {
                cxfMessage.setContent(InputStream.class, result);
            } else {
                cxfMessage.setContent(result.getClass(), result);
            }
        }
    }

    public static void storeCXfResponseContext(Message response, Map<String, Object> context) {
        if (context != null) {
            MessageContext messageContext = new WrappedMessageContext(context, null, Scope.HANDLER);
            response.put(Client.RESPONSE_CONTEXT, messageContext);
            //put the protocol headers into the message headers
            Map<String, List<String>> protocolHeaders =
                CastUtils.cast((Map)messageContext.get(Message.PROTOCOL_HEADERS));
            response.put(Message.PROTOCOL_HEADERS, protocolHeaders);            
            Object value = context.get(Message.RESPONSE_CODE);            
            if (value != null) {
                response.put(Message.RESPONSE_CODE, value);
            }
        }
    }

    public static void storeCxfResponse(CxfExchange exchange, Object response) {
        org.apache.camel.Message out = exchange.getOut();
        if (response != null) {
            out.setBody(response);
        }
    }

    public static void storeCxfFault(CxfExchange exchange, Message message) {
        org.apache.camel.Message fault = exchange.getFault();
        if (fault != null) {
            fault.setBody(getBody(message));
        }
    }


    public static Map<String, Object> propogateContext(CxfExchange exchange, Map<String, Object> context) {
        Message message = exchange.getInMessage();
        Map<String, Object> requestContext = CastUtils.cast((Map<?, ?>)message.get(Client.REQUEST_CONTEXT));
        Map<String, Object> responseContext = CastUtils.cast((Map<?, ?>)message.get(Client.RESPONSE_CONTEXT));
        Map<String, Object> camelRequestContext = 
            CastUtils.cast((Map<?, ?>)exchange.getIn().getHeader(Client.REQUEST_CONTEXT));
        
        Map<String, Object> mergedRequestContext = new HashMap<String, Object>();
        WrappedMessageContext ctx = new WrappedMessageContext(mergedRequestContext,
                                                              null,
                                                              Scope.APPLICATION);
        if (requestContext != null) {
            ctx.putAll(requestContext);
        }
        
        if (camelRequestContext != null) {
            ctx.putAll(camelRequestContext);
        }
        
        // Remove protocol headers from scopes.  Otherwise, response headers can be
        // overwritten by request headers when SOAPHandlerInterceptor tries to create
        // a wrapped message context by the copyScoped() method.
        if (ctx.size() > 0) {
            ctx.getScopes().remove(Message.PROTOCOL_HEADERS);
        }
            
        if (responseContext == null) {
            responseContext = new HashMap<String, Object>();
        } else {
            // clear the response context
            responseContext.clear();
        }
        context.put(Client.REQUEST_CONTEXT, mergedRequestContext);
        context.put(Client.RESPONSE_CONTEXT, responseContext);
        return responseContext;
    }

    protected static void relayRequestHeaders(CxfEndpoint endpoint, 
                                              CxfExchange exchange, 
                                              Map<String, Object> context) {
        
        Message message = exchange.getInMessage();
        if (message == null) {
            return;
        }
        Map<String, Object> requestContext = CastUtils.cast((Map<?, ?>)context.get(Client.REQUEST_CONTEXT));
        if (!endpoint.isRelayHeaders()) {
            message.remove(Header.HEADER_LIST);
            if (exchange.getExchange() == null) {
                return;
            }
            BindingOperationInfo bop = exchange.getExchange().get(BindingOperationInfo.class);
            if (null == bop) {
                return;
            }
            BindingMessageInfo bmi = bop.getInput();
            List<SoapHeaderInfo> headersInfo = bmi.getExtensors(SoapHeaderInfo.class);
            if (headersInfo == null || headersInfo.size() == 0) {
                return;
            }
            MessageContentsList parameters = MessageContentsList.getContentsList(message);
            for (SoapHeaderInfo headerInfo : headersInfo) {
                Object o = parameters.get(headerInfo.getPart());
                if (o instanceof Holder) {
                    Holder holder = (Holder)o;
                    holder.value = null;
                } else {
                    parameters.remove(headerInfo.getPart());
                }
            }
            return;
        }
        if (!message.containsKey(Header.HEADER_LIST)) {
            return;
        }
        MessageHeadersRelay relay = getRelay(endpoint, exchange);
        relayHeaders(Direction.OUT, 
                     (List<Header>)message.get(Header.HEADER_LIST),
                     requestContext, 
                     relay);
    }

    protected static void relayResponseHeaders(CxfEndpoint endpoint, 
                                               CxfExchange exchange, 
                                               Map<String, Object> context) {
        Message message = exchange.getOutMessage();
        if (message == null) {
            return;
        }
        Map<String, Object> responseContext = (Map<String, Object>)context.get(Client.RESPONSE_CONTEXT);
        List<Header> headers = (List<Header>)responseContext.get(Header.HEADER_LIST);
        responseContext.remove(Header.HEADER_LIST);
        if (!endpoint.isRelayHeaders()) {
            Exchange e = exchange.getExchange();
            if (e == null) {
                return;
            }
            BindingOperationInfo bop = e.get(BindingOperationInfo.class);
            if (null == bop) {
                return;
            }
            BindingMessageInfo bmi = bop.getOutput();
            List<SoapHeaderInfo> headersInfo = bmi.getExtensors(SoapHeaderInfo.class);
            if (headersInfo == null || headersInfo.size() == 0) {
                return;
            }
            MessageContentsList parameters = MessageContentsList.getContentsList(message);
            for (SoapHeaderInfo headerInfo : headersInfo) {
                Object o = parameters.get(headerInfo.getPart());
                if (o instanceof Holder) {
                    Holder holder = (Holder)o;
                    holder.value = null;
                } else {
                    parameters.remove(headerInfo.getPart());
                }
            }
            return;
        }
        if (headers == null || headers.size() == 0) {
            return;
        }
        MessageHeadersRelay relay = getRelay(endpoint, exchange);
        relayHeaders(Direction.IN, 
                     headers,
                     responseContext,
                     relay);
    }

    protected static MessageHeadersRelay getRelay(CxfEndpoint endpoint, CxfExchange exchange) {
        MessageHeadersRelay relay = null;
        String ns = null;
        Endpoint e = null;
        Exchange cxfExchange = exchange.getExchange();
        if (cxfExchange != null) {
            e = cxfExchange.get(Endpoint.class);
        }
        if (e != null && e.getBinding() != null) {
            Binding b = e.getBinding();
            if (b != null && b.getBindingInfo() != null) {
                ns = b.getBindingInfo().getBindingId();
            }
        }
        if (ns == null) {
            LOG.log(Level.WARNING,
                    "No CXF Binding namespace can be resolved for relaying message headers, " 
                     + " using " + DefaultMessageHeadersRelay.ACTIVATION_NAMESPACE + " namespace");
        }
        if (ns != null) {
            relay = endpoint.getMessageHeadersRelay(ns);
        }
        if (relay == null) {
            LOG.log(Level.WARNING,
                    "No MessageHeadersRelay instance is bound to '" 
                     + ns + "' namespace; using " 
                     + DefaultMessageHeadersRelay.ACTIVATION_NAMESPACE + " namespace");
            relay = 
                endpoint.getMessageHeadersRelay(DefaultMessageHeadersRelay.ACTIVATION_NAMESPACE);
        }
        return relay;
    }
    
    protected static void relayHeaders(Direction direction, 
                                       List<Header> from,
                                       Map<String, Object> context,
                                       MessageHeadersRelay relay) {
        
        List<Header> to = (List<Header>)context.get(Header.HEADER_LIST);
        boolean hasHeaders = true;
        if (to == null) {
            to = new ArrayList<Header>();
            hasHeaders = false;
        }
        relay.relay(direction, from, to);
        for (Header header : to) {
            header.setDirection(Header.Direction.DIRECTION_OUT);
        }
        if (!hasHeaders && to.size() > 0) {
            context.put(Header.HEADER_LIST, to);
        }
    }
}


