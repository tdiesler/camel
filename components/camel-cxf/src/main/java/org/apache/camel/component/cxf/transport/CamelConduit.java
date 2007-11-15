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
package org.apache.camel.component.cxf.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfSoapBinding;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

/**
 * @version $Revision$
 */
public class CamelConduit extends AbstractConduit implements Configurable {
    protected static final String BASE_BEAN_NAME_SUFFIX = ".camel-conduit";
    private static final Logger LOG = LogUtils.getL7dLogger(CamelConduit.class);
    private CamelContext camelContext;
    private EndpointInfo endpointInfo;
    private String targetCamelEndpointUri;
    private CamelTemplate<Exchange> camelTemplate;
    private Bus bus;
    
    
    public CamelConduit(CamelContext context, Bus b, EndpointInfo epInfo, EndpointReferenceType targetReference) {
        super(targetReference);
        AttributedURIType address = targetReference.getAddress();
        if (address != null) {
            this.targetCamelEndpointUri = address.getValue();
        }
        endpointInfo = epInfo;
        camelContext = context;
        bus = b;
        initConfig();
    }
    
    public void setCamelContext(CamelContext context) {        
        camelContext = context;
    }
    
    public CamelContext getCamelContext() {
        return camelContext;
    }

    // prepare the message for send out , not actually send out the message
    public void prepare(Message message) throws IOException {
        getLogger().log(Level.FINE, "CamelConduit send message");
        message.setContent(OutputStream.class, new CamelOutputStream(message));
    }

    public void close() {
        getLogger().log(Level.FINE, "CamelConduit closed ");
        
    }

    protected Logger getLogger() {
        return LOG;
    }

    public String getBeanName() {
        
        if (endpointInfo == null) {
            return "default.camel-conduit";
        }
        return endpointInfo.getName() + ".camel-conduit";
    }

    private void initConfig() {
        // we could configure the camel context here     
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(this);
        }
    }
    
    public CamelTemplate getCamelTemplate() {
        if (camelTemplate == null) {
            if (camelContext != null) {
                camelTemplate = new CamelTemplate<Exchange>(camelContext);
            } else {
                camelTemplate = new CamelTemplate<Exchange>(new DefaultCamelContext());
            }
        }
        return camelTemplate;
    }
    
    public void setCamelTemplate(CamelTemplate template) {
        
    }
    private class CamelOutputStream extends CachedOutputStream {
        private Message outMessage;
        private boolean isOneWay;

        public CamelOutputStream(Message m) {
            outMessage = m;
        }

        protected void doFlush() throws IOException {
            // do nothing here
        }

        protected void doClose() throws IOException {
            isOneWay = outMessage.getExchange().isOneWay();
            commitOutputMessage();
        }

        protected void onWrite() throws IOException {
            // do nothing here
        }

        
        private void commitOutputMessage() {
            // we could wait for the rely asynconized
            org.apache.camel.Exchange exchange = getCamelTemplate().send(targetCamelEndpointUri, new Processor() {
                public void process(org.apache.camel.Exchange ex) throws IOException {
                    CachedOutputStream outputStream = (CachedOutputStream)outMessage.getContent(OutputStream.class);
                    // send out the request message here
                    ex.getIn().setHeaders(outMessage);
                    ex.getIn().setBody(outputStream.getInputStream());
                    // setup the out message
                    getLogger().log(Level.FINE, "template sending request: ", ex.getIn());
                }
            });
            if (!isOneWay) {
                handleResponse(exchange);
            }
            
        }

        private void handleResponse(org.apache.camel.Exchange exchange) {
            org.apache.cxf.message.Message inMessage = CxfSoapBinding.getCxfInMessage(exchange, true); 
            getLogger().log(Level.FINE, "incoming observer is " + incomingObserver);
            incomingObserver.onMessage(inMessage);
        }
    }

    /**
     * Represented decoupled response endpoint.
     */
    protected class DecoupledDestination implements Destination {
        protected MessageObserver decoupledMessageObserver;
        private EndpointReferenceType address;

        DecoupledDestination(EndpointReferenceType ref, MessageObserver incomingObserver) {
            address = ref;
            decoupledMessageObserver = incomingObserver;
        }

        public EndpointReferenceType getAddress() {
            return address;
        }

        public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType addr) throws IOException {
            // shouldn't be called on decoupled endpoint
            return null;
        }

        public void shutdown() {
            // TODO Auto-generated method stub
        }

        public synchronized void setMessageObserver(MessageObserver observer) {
            decoupledMessageObserver = observer;
        }

        public synchronized MessageObserver getMessageObserver() {
            return decoupledMessageObserver;
        }
    }

}
