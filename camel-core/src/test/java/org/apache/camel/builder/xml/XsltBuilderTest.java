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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.impl.DefaultExchange;

/**
 * @version $Revision$
 */
public class XsltBuilderTest extends ContextTestSupport {

    public void testXsltUrl() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltTransformerUrl() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = new XsltBuilder();
        builder.setTransformerURL(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltFile() throws Exception {
        File styleSheet = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }
    
    public void testXsltTransformerFile() throws Exception {
        File styleSheet = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();

        XsltBuilder builder = new XsltBuilder();
        builder.setTransformerFile(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltInputStream() throws Exception {
        File styleSheet = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();

        XsltBuilder builder = XsltBuilder.xslt(new FileInputStream(styleSheet));

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltTransformerInputStream() throws Exception {
        File styleSheet = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();

        XsltBuilder builder = new XsltBuilder();
        builder.setTransformerInputStream(new FileInputStream(styleSheet));

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltSource() throws Exception {
        File file = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();
        Source styleSheet = new SAXSource(new InputSource(new FileInputStream(file)));

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltTemplates() throws Exception {
        File file = new File("src/test/resources/org/apache/camel/builder/xml/example.xsl").getAbsoluteFile();
        Source source = new SAXSource(new InputSource(new FileInputStream(file)));

        XmlConverter converter = new XmlConverter();
        Templates styleSheet = converter.getTransformerFactory().newTemplates(source);

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltOutputString() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet).outputString();

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);
        assertIsInstanceOf(String.class, exchange.getOut().getBody());

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltOutputBytes() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet).outputBytes();

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);
        assertIsInstanceOf(byte[].class, exchange.getOut().getBody());

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody(String.class));
    }

    public void testXsltOutputDOM() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet).outputDOM();

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);
        assertIsInstanceOf(Document.class, exchange.getOut().getBody());

        assertEquals("<goodbye>world!</goodbye>", exchange.getOut().getBody(String.class));
    }

    public void testXsltOutputFile() throws Exception {
        // directory must exists
        deleteDirectory("target/xslt");
        createDirectory("target/xslt");

        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet).outputFile();

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");
        exchange.getIn().setHeader(Exchange.XSLT_FILE_NAME, "target/xslt/xsltout.xml");

        builder.process(exchange);
        assertIsInstanceOf(File.class, exchange.getOut().getBody());

        File file = new File("target/xslt/xsltout.xml").getAbsoluteFile();
        assertTrue("Output file should exist", file.exists());

        String body = exchange.getOut().getBody(String.class);
        assertTrue(body.endsWith("<goodbye>world!</goodbye>"));
    }

    public void testXsltSetConverter() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);
        XmlConverter converter = new XmlConverter();
        builder.setConverter(converter);
        assertSame(converter, builder.getConverter());

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody());
    }

    public void testXsltResultHandler() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);
        assertNotNull(builder.getResultHandlerFactory());
        
        builder.outputBytes();
        assertIsInstanceOf(StreamResultHandlerFactory.class, builder.getResultHandlerFactory());

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("<hello>world!</hello>");

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>", exchange.getOut().getBody(String.class));
    }

    public void testNullBodyDefault() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(null);

        try {
            builder.process(exchange);
            fail("Should thrown an exception");
        } catch (ExpectedBodyTypeException e) {
            // expected
        }
    }

    public void testFailNullBody() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);
        builder.setFailOnNullBody(true);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(null);

        try {
            builder.process(exchange);
            fail("Should thrown an exception");
        } catch (ExpectedBodyTypeException e) {
            // expected
        }
    }

    public void testNotFailNullBody() throws Exception {
        URL styleSheet = getClass().getResource("example.xsl");

        XsltBuilder builder = XsltBuilder.xslt(styleSheet);
        builder.setFailOnNullBody(false);

        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(null);

        builder.process(exchange);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye/>", exchange.getOut().getBody(String.class));
    }

}
