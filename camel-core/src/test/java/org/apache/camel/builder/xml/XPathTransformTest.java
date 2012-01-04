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
package org.apache.camel.builder.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.camel.ContextTestSupport;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.slf4j.Logger;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;


/**
 * @version
 */
public class XPathTransformTest extends ContextTestSupport {

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public void testXPathTransform() throws Exception {
        Document doc = context.getTypeConverter().convertTo(Document.class, "<root><firstname>Apache</firstname><lastname>Camel</lastname></root>");
        NodeList list = XPathBuilder.xpath("/root/firstname", NodeList.class).evaluate(context, doc, NodeList.class);
        assertNotNull(list);
        list.item(0).setTextContent("Servicemix");

        String out = context.getTypeConverter().convertTo(String.class, doc);
        assertEquals("<root><firstname>Servicemix</firstname><lastname>Camel</lastname></root>", out);
    }

    public void testXPathNamespaceTracingEnabledJavaDSL() throws Exception {
        Logger l = createNiceMock(Logger.class);

        expect(l.isTraceEnabled()).andReturn(true).anyTimes();

        l.trace(contains("Namespaces discovered in message"), anyObject());
        expectLastCall().times(1);
        replay(l);

        String body = "<aRoot xmlns:nsa=\"http://namespacec.net\"><nsa:a xmlns:nsa=\"http://namespacea.net\">Hello|there|Camel</nsa:a>"
                + "<nsb:a xmlns:nsb=\"http://namespaceb.net\">Hello|there|Camel</nsb:a><nsb:a xmlns:nsb=\"http://namespaceb.net\">Hello|there|Camel</nsb:a>"
                + "<a xmlns=\"http://defaultNamespace.net\">Hello|there|Camel</a><a>Hello|there|Camel</a></aRoot>"; 
        Document doc = context.getTypeConverter().convertTo(Document.class, body);
        Field logField = XPathBuilder.class.getDeclaredField("LOG");
        logField.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(logField, logField.getModifiers() & ~Modifier.FINAL);

        logField.set(null, l);

        NodeList list = XPathBuilder.xpath("//*", NodeList.class).traceNamespaces().evaluate(context, doc, NodeList.class);
        assertNotNull(list);

        verify(l);
    }

    public void testXPathNamespaceTracingDisabledJavaDSL() throws Exception {
        Logger l = createNiceMock(Logger.class);

        expect(l.isTraceEnabled()).andReturn(true).anyTimes();

        Capture<String> captures = new Capture<String>(CaptureType.ALL);
        l.trace(capture(captures), anyObject());
        expectLastCall().anyTimes();

        replay(l);

        String body = "<aRoot xmlns:nsa=\"http://namespacec.net\"><nsa:a xmlns:nsa=\"http://namespacea.net\">Hello|there|Camel</nsa:a>"
                + "<nsb:a xmlns:nsb=\"http://namespaceb.net\">Hello|there|Camel</nsb:a><nsb:a xmlns:nsb=\"http://namespaceb.net\">Hello|there|Camel</nsb:a>"
                + "<a xmlns=\"http://defaultNamespace.net\">Hello|there|Camel</a><a>Hello|there|Camel</a></aRoot>";
        Document doc = context.getTypeConverter().convertTo(Document.class, body);
        Field logField = XPathBuilder.class.getDeclaredField("LOG");
        logField.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(logField, logField.getModifiers() & ~Modifier.FINAL);

        logField.set(null, l);

        NodeList list = XPathBuilder.xpath("//*", NodeList.class).evaluate(context, doc, NodeList.class);
        assertNotNull(list);

        verify(l);

        for (String c : captures.getValues()) {
            if (c.contains("Namespaces discovered in message")) {
                throw new AssertionError("Did not expect LOG.trace with 'Namespaces discovered in message'");
            }
        }
    }

}
