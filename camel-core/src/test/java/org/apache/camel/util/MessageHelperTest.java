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
package org.apache.camel.util;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.StreamCache;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

/**
 * Test cases for {@link MessageHelper}
 */
public class MessageHelperTest extends TestCase {
    
    private Message message;
    
    @Override
    protected void setUp() throws Exception {
        message = new DefaultMessage();
    }

    /*
     * Tests the {@link MessageHelper#resetStreamCache(Message)} method
     */
    public void testResetStreamCache() throws Exception {
        // should not throw exceptions when Message or message body is null
        MessageHelper.resetStreamCache(null);
        MessageHelper.resetStreamCache(message);
        
        // handle StreamCache
        final ValueHolder<Boolean> reset = new ValueHolder<Boolean>(Boolean.FALSE);
        message.setBody(new StreamCache() {
            @SuppressWarnings("deprecation")
            public void reset() {
                reset.set(Boolean.TRUE);
            }

            public void writeTo(OutputStream os) throws IOException {
                // noop
            }
        });
        MessageHelper.resetStreamCache(message);
        assertTrue("Should have reset the stream cache", reset.get());
    }

    public void testGetContentType() throws Exception {
        message.setHeader(Exchange.CONTENT_TYPE, "text/xml");
        assertEquals("text/xml", MessageHelper.getContentType(message));
    }

    public void testGetContentEncpding() throws Exception {
        message.setHeader(Exchange.CONTENT_ENCODING, "iso-8859-1");
        assertEquals("iso-8859-1", MessageHelper.getContentEncoding(message));
    }

    public void testCopyHeaders() throws Exception {
        Message source = message;
        Message target = new DefaultMessage();

        source.setHeader("foo", 123);
        source.setHeader("bar", 456);
        target.setHeader("bar", "yes");

        MessageHelper.copyHeaders(source, target, false);

        assertEquals(123, target.getHeader("foo"));
        assertEquals("yes", target.getHeader("bar"));
    }

    public void testCopyHeadersOverride() throws Exception {
        Message source = message;
        Message target = new DefaultMessage();

        source.setHeader("foo", 123);
        source.setHeader("bar", 456);
        target.setHeader("bar", "yes");

        MessageHelper.copyHeaders(source, target, true);

        assertEquals(123, target.getHeader("foo"));
        assertEquals(456, target.getHeader("bar"));
    }

    public void testDumpAsXmlPlainBody() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        message = new DefaultExchange(context).getIn();

        // xml message body
        message.setBody("Hello World");
        message.setHeader("foo", 123);

        String out = MessageHelper.dumpAsXml(message);
        assertTrue("Should contain body", out.contains("<body type=\"java.lang.String\">Hello World</body>"));

        context.stop();
    }

    public void testDumpAsXmlXmlBody() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        message = new DefaultExchange(context).getIn();

        // xml message body
        message.setBody("<?xml version=\"1.0\"?><hi>Hello World</hi>");
        message.setHeader("foo", 123);

        String out = MessageHelper.dumpAsXml(message);
        assertTrue("Should contain body", out.contains("<body type=\"java.lang.String\">&lt;?xml version=&quot;1.0&quot;?&gt;&lt;hi&gt;Hello World&lt;/hi&gt;</body>"));

        context.stop();
    }

}
