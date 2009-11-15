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
package org.apache.camel.component.cxf.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;

public class XMLMessageOutInterceptor extends AbstractMessageOutInterceptor<XMLMessage> {
    private static final Logger LOG = LogUtils.getL7dLogger(XMLMessageOutInterceptor.class);
   

    public XMLMessageOutInterceptor() {
        super(Phase.PREPARE_SEND);        
        addAfter(DOMOutInterceptor.class.getName());
    }

    protected Logger getLogger() {
        return LOG;
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(XMLMessage message) throws Fault {
        Exchange exchange = message.getExchange();        
        //BindingOperationInfo boi = exchange.get(BindingOperationInfo.class);
        BindingMessageInfo bmi = exchange.get(BindingMessageInfo.class);

        List<Element> payload = message.get(List.class);
        if (bmi == null && payload.size() > 1) {
            throw new Fault(new org.apache.cxf.common.i18n.Message(
                            "NO_XML_ROOT_NODE", LOG));            
        }
        
        if (bmi != null) {
            
            XMLBindingMessageFormat msgFormat = 
                bmi.getExtensor(XMLBindingMessageFormat.class);
            QName rootName = msgFormat != null ? msgFormat.getRootNode() : null;
            
            if (rootName == null) {
                if (payload.size() > 1) {
                    throw new Fault(new org.apache.cxf.common.i18n.Message(
                                    "NO_XML_ROOT_NODE", LOG));
                }
            } else {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("DOMOutInterceptor Create xmlformat RootNode element");
                }                
                Element el = createElement(rootName, payload);
                payload = new ArrayList<Element>();
                payload.add(el);
            }

            message.put(List.class, payload);
            message.remove(Element.class);
        }
    }
    
}
