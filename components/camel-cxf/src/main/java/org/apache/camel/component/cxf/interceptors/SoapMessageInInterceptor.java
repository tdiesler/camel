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
import java.util.Collection;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.logging.LogUtils;


import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.StaxUtils;

public class SoapMessageInInterceptor extends AbstractMessageInInterceptor<SoapMessage> {
    private static final Logger LOG = LogUtils.getL7dLogger(SoapMessageInInterceptor.class);

    public SoapMessageInInterceptor() {
        super(Phase.READ);
    }

    protected Logger getLogger() {
        return LOG;
    }

    protected boolean isFaultMessage(SoapMessage message) {
        XMLStreamReader xsr = message.getContent(XMLStreamReader.class);
        boolean isFault = false;
        try {
            if (StaxUtils.skipToStartOfElement(xsr)) {
                QName startQName = xsr.getName();
                isFault = message.getVersion().getFault().equals(startQName);
            }
        } catch (XMLStreamException xse) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("STAX_READ_EXC", LOG));
        }

        return isFault;
    }

    protected BindingOperationInfo getBindingOperation(SoapMessage message, Document doc) {
        Exchange ex = message.getExchange();
        SoapBindingInfo soapBinding = (SoapBindingInfo)ex.get(BindingInfo.class);

        Element payloadEl = (Element)doc.getChildNodes().item(0);
        QName startQName = new QName(payloadEl.getNamespaceURI(), payloadEl.getLocalName());

        // handling xml normal inbound message
        boolean client = isRequestor(message);

        List<BindingOperationInfo> boiList = new ArrayList<BindingOperationInfo>();
        for (BindingOperationInfo boi : soapBinding.getOperations()) {
            String style = soapBinding.getStyle(boi.getOperationInfo());
            QName rootName = null;
            if ("rpc".equals(style)) {
                rootName = boi.getOperationInfo().getName();
            } else {
                BindingMessageInfo bmi = client ?  boi.getOutput() : boi.getInput();
                if (bmi != null) {
                    Collection<MessagePartInfo> bodyParts = bmi.getMessageParts();
                    if (bodyParts.size() == 1) {
                        MessagePartInfo p = bodyParts.iterator().next();
                        rootName = p.getConcreteName();
                    }
                }
            }

            if (startQName.equals(rootName)) {
                boiList.add(boi);
            }
        }

        if (boiList.size() > 1
            && LOG.isLoggable(Level.INFO)) {
            LOG.info("Mulitple matching BindingOperationIno found in Binding.");
        }

        return boiList.size() != 1 ? null : boiList.get(0);
    }

    protected List<Element> getPartList(SoapMessage inMessage, Element rootNode, BindingMessageInfo bmi) {
        List<Element> partList = new ArrayList<Element>();
        Exchange ex = inMessage.getExchange();
        SoapBindingInfo soapBinding = (SoapBindingInfo)ex.get(BindingInfo.class);

        String style = soapBinding.getStyle(bmi.getBindingOperation().getOperationInfo());
        if ("rpc".equals(style)) {
            //Remove the operation element.
            rootNode = (Element)DOMUtils.getChild(rootNode, Node.ELEMENT_NODE);
        }
        partList.add(rootNode);
        return partList;
    }

   

    

}
