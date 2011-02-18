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
package org.apache.camel.converter.jaxp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;

/**
 * A helper class to transform to and from various JAXB types such as {@link Source} and {@link Document}
 *
 * @version $Revision$
 */
@Converter
public class XmlConverter {
    @Deprecated
    //It will be removed in Camel 3.0, please use the Exchange.DEFAULT_CHARSET 
    public static final String DEFAULT_CHARSET_PROPERTY = "org.apache.camel.default.charset";
    
    public static final String OUTPUT_PROPERTIES_PREFIX = "org.apache.camel.xmlconverter.output.";
    public static String defaultCharset = ObjectHelper.getSystemProperty(Exchange.DEFAULT_CHARSET_PROPERTY, "UTF-8");

    /*
     * When converting a DOM tree to a SAXSource, we try to use Xalan internal DOM parser if
     * available. Else, transform the DOM tree to a String and build a SAXSource on top of it.
     */
    private static final Class<?> DOM_TO_SAX_CLASS;

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;

    static {
        Class<?> cl = null;
        try {
            // will not warn the user if the class could not be found
            cl = ObjectHelper.loadClass("org.apache.xalan.xsltc.trax.DOM2SAX", XmlConverter.class.getClassLoader(), false);
        } catch (Exception e) {
            // ignore
        }
        DOM_TO_SAX_CLASS = cl;
    }


    public XmlConverter() {
    }

    public XmlConverter(DocumentBuilderFactory documentBuilderFactory) {
        this.documentBuilderFactory = documentBuilderFactory;
    }

    /**
     * Returns the default set of output properties for conversions.
     */
    public Properties defaultOutputProperties() {
        Properties properties = new Properties();
        properties.put(OutputKeys.ENCODING, defaultCharset);
        properties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return properties;
    }

    /**
     * Converts the given input Source into the required result
     */
    public void toResult(Source source, Result result) throws TransformerException {
        toResult(source, result, defaultOutputProperties());
    }

    /**
     * Converts the given input Source into the required result
     */
    public void toResult(Source source, Result result, Properties outputProperties) throws TransformerException {
        if (source == null) {
            return;
        }

        Transformer transformer = createTransformer();
        if (transformer == null) {
            throw new TransformerException("Could not create a transformer - JAXP is misconfigured!");
        }
        transformer.setOutputProperties(outputProperties);
        transformer.transform(source, result);
    }

    /**
     * Converts the given NodeList to a boolean
     */
    @Converter
    public Boolean toBoolean(NodeList list) {
        return list.getLength() > 0;
    }

    /**
     * Converts the given byte[] to a Source
     */
    @Converter
    public BytesSource toBytesSource(byte[] data) {
        return new BytesSource(data);
    }


    /**
     * Converts the given String to a Source
     */
    @Converter
    public StringSource toStringSource(String data) {
        return new StringSource(data);
    }

    /**
     * Converts the given Document to a Source
     * @deprecated use toDOMSource instead
     */
    @Deprecated
    public DOMSource toSource(Document document) {
        return toDOMSource(document);
    }

    /**
     * Converts the given Document to a Source
     */
    @Converter
    public DOMSource toDOMSource(Document document) {
        return new DOMSource(document);
    }

    /**
     * Converts the given Node to a Source
     */
    @Converter
    public Source toSource(Node node) {
        return new DOMSource(node);
    }
    
    /**
     * Converts the given String to a Source
     */
    @Converter
    public Source toSource(String data) {
        return new StringSource(data);
    }

    /**
     * Converts the given input Source into text
     */
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public String toString(Source source) throws TransformerException {
        return toString(source, null);
    }

    /**
     * Converts the given input Source into text
     */
    @Converter
    public String toString(Source source, Exchange exchange) throws TransformerException {
        if (source == null) {
            return null;
        } else if (source instanceof StringSource) {
            return ((StringSource) source).getText();
        } else if (source instanceof BytesSource) {
            return new String(((BytesSource) source).getData());
        } else {
            StringWriter buffer = new StringWriter();           
            if (exchange != null) {
                // check the camelContext properties first
                Properties properties = ObjectHelper.getCamelPropertiesWithPrefix(OUTPUT_PROPERTIES_PREFIX, exchange.getContext());
                if (properties.size() > 0) {
                    toResult(source, new StreamResult(buffer), properties);
                    return buffer.toString();
                }            
            }
            // using the old way to deal with it
            toResult(source, new StreamResult(buffer));            
            return buffer.toString();
        }
    }

    /**
     * Converts the given input Source into bytes
     */
    @Converter
    public byte[] toByteArray(Source source, Exchange exchange) throws TransformerException {
        String answer = toString(source, exchange);
        if (exchange != null) {
            return exchange.getContext().getTypeConverter().convertTo(byte[].class, exchange, answer);
        } else {
            return answer.getBytes();
        }
    }
    
    /**
     * Converts the given input Node into text
     */
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public String toString(Node node) throws TransformerException {
        return toString(node, null);
    }
    
    /**
     * Converts the given input Node into text
     */
    @Converter
    public String toString(Node node, Exchange exchange) throws TransformerException {
        return toString(new DOMSource(node), exchange);
    }

    /**
     * Converts the source instance to a {@link DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public DOMSource toDOMSource(Source source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        if (source instanceof DOMSource) {
            return (DOMSource) source;
        } else if (source instanceof SAXSource) {
            return toDOMSourceFromSAX((SAXSource) source);
        } else if (source instanceof StreamSource) {
            return toDOMSourceFromStream((StreamSource) source);
        } else {
            return null;
        }
    }

    /**
     * Converts the source instance to a {@link DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public DOMSource toDOMSource(String text) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Source source = toSource(text);
        if (source != null) {
            return toDOMSourceFromStream((StreamSource) source);
        } else {
            return null;
        }
    }

    /**
     * Converts the source instance to a {@link DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public DOMSource toDOMSource(byte[] bytes) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            return toDOMSource(is);
        } finally {
            IOHelper.close(is);
        }
    }


    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public SAXSource toSAXSource(String source) throws IOException, SAXException, TransformerException {
        return toSAXSource(source, null);
    }
    
    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public SAXSource toSAXSource(String source, Exchange exchange) throws IOException, SAXException, TransformerException {
        return toSAXSource(toSource(source), exchange);
    }
   
    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter    
    public SAXSource toSAXSource(InputStream source) throws IOException, SAXException, TransformerException {
        return toSAXSource(source, null);
    }
    
    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public SAXSource toSAXSource(InputStream source, Exchange exchange) throws IOException, SAXException, TransformerException {
        return toSAXSource(toStreamSource(source), exchange);
    }
    
    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public SAXSource toSAXSource(Source source) throws IOException, SAXException, TransformerException {
        return toSAXSource(source, null);
    }
    
    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    @Converter
    public SAXSource toSAXSource(Source source, Exchange exchange) throws IOException, SAXException, TransformerException {
        if (source instanceof SAXSource) {
            return (SAXSource) source;
        } else if (source instanceof DOMSource) {
            return toSAXSourceFromDOM((DOMSource) source, exchange);
        } else if (source instanceof StreamSource) {
            return toSAXSourceFromStream((StreamSource) source);
        } else {
            return null;
        }
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter    
    public StreamSource toStreamSource(Source source) throws TransformerException {
        return toStreamSource(source, null);
    }
    
    @Converter
    public StreamSource toStreamSource(Source source, Exchange exchange) throws TransformerException {
        if (source instanceof StreamSource) {
            return (StreamSource) source;
        } else if (source instanceof DOMSource) {
            return toStreamSourceFromDOM((DOMSource) source, exchange);
        } else if (source instanceof SAXSource) {
            return toStreamSourceFromSAX((SAXSource) source, exchange);
        } else {
            return null;
        }
    }

    @Converter
    public StreamSource toStreamSource(InputStream in) throws TransformerException {
        if (in != null) {
            return new StreamSource(in);
        }
        return null;
    }

    @Converter
    public StreamSource toStreamSource(Reader in) throws TransformerException {
        if (in != null) {
            return new StreamSource(in);
        }
        return null;
    }

    @Converter
    public StreamSource toStreamSource(File in) throws TransformerException {
        if (in != null) {
            return new StreamSource(in);
        }
        return null;
    }

    @Converter
    public StreamSource toStreamSource(byte[] in, Exchange exchange) throws TransformerException {
        if (in != null) {
            InputStream is = exchange.getContext().getTypeConverter().convertTo(InputStream.class, in);
            return new StreamSource(is);
        }
        return null;
    }

    @Converter
    public StreamSource toStreamSource(ByteBuffer in, Exchange exchange) throws TransformerException {
        if (in != null) {
            InputStream is = exchange.getContext().getTypeConverter().convertTo(InputStream.class, in);
            return new StreamSource(is);
        }
        return null;
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public StreamSource toStreamSourceFromSAX(SAXSource source) throws TransformerException {
        return toStreamSourceFromSAX(source, null);
    }
    
    @Converter
    public StreamSource toStreamSourceFromSAX(SAXSource source, Exchange exchange) throws TransformerException {
        InputSource inputSource = source.getInputSource();
        if (inputSource != null) {
            if (inputSource.getCharacterStream() != null) {
                return new StreamSource(inputSource.getCharacterStream());
            }
            if (inputSource.getByteStream() != null) {
                return new StreamSource(inputSource.getByteStream());
            }
        }
        String result = toString(source, exchange);
        return new StringSource(result);
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public StreamSource toStreamSourceFromDOM(DOMSource source) throws TransformerException {
        return toStreamSourceFromDOM(source, null);
    }
    
    @Converter
    public StreamSource toStreamSourceFromDOM(DOMSource source, Exchange exchange) throws TransformerException {
        String result = toString(source, exchange);
        return new StringSource(result);
    }

    @Converter
    public SAXSource toSAXSourceFromStream(StreamSource source) {
        InputSource inputSource;
        if (source.getReader() != null) {
            inputSource = new InputSource(source.getReader());
        } else {
            inputSource = new InputSource(source.getInputStream());
        }
        inputSource.setSystemId(source.getSystemId());
        inputSource.setPublicId(source.getPublicId());
        return new SAXSource(inputSource);
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public Reader toReaderFromSource(Source src) throws TransformerException {
        return toReaderFromSource(src, null);
    }
    
    @Converter
    public Reader toReaderFromSource(Source src, Exchange exchange) throws TransformerException {
        StreamSource stSrc = toStreamSource(src, exchange);
        Reader r = stSrc.getReader();
        if (r == null) {
            r = new InputStreamReader(stSrc.getInputStream());
        }
        return r;
    }

    @Converter
    public DOMSource toDOMSource(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        InputSource source = new InputSource(is);
        String systemId = source.getSystemId();
        DocumentBuilder builder = createDocumentBuilder();
        Document document = builder.parse(source);
        return new DOMSource(document, systemId);
    }

    @Converter
    public DOMSource toDOMSourceFromStream(StreamSource source) throws ParserConfigurationException, IOException, SAXException {
        Document document;
        String systemId = source.getSystemId();

        DocumentBuilder builder = createDocumentBuilder();
        Reader reader = source.getReader();
        if (reader != null) {
            document = builder.parse(new InputSource(reader));
        } else {
            InputStream inputStream = source.getInputStream();
            if (inputStream != null) {
                InputSource inputsource = new InputSource(inputStream);
                inputsource.setSystemId(systemId);
                document = builder.parse(inputsource);
            } else {
                throw new IOException("No input stream or reader available on StreamSource: " + source);
            }
        }
        return new DOMSource(document, systemId);
    }
    
    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public SAXSource toSAXSourceFromDOM(DOMSource source) throws TransformerException {
        return toSAXSourceFromDOM(source, null);
    }
    
    @Converter
    public SAXSource toSAXSourceFromDOM(DOMSource source, Exchange exchange) throws TransformerException {
        if (DOM_TO_SAX_CLASS != null) {
            try {
                Constructor<?> cns = DOM_TO_SAX_CLASS.getConstructor(Node.class);
                XMLReader converter = (XMLReader) cns.newInstance(source.getNode());
                return new SAXSource(converter, new InputSource());
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        } else {
            String str = toString(source, exchange);
            StringReader reader = new StringReader(str);
            return new SAXSource(new InputSource(reader));
        }
    }

    @Converter
    public DOMSource toDOMSourceFromSAX(SAXSource source) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        return new DOMSource(toDOMNodeFromSAX(source));
    }

    @Converter
    public Node toDOMNodeFromSAX(SAXSource source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DOMResult result = new DOMResult();
        toResult(source, result);
        return result.getNode();
    }

    /**
     * Converts the given TRaX Source into a W3C DOM node
     */
    @Converter
    public Node toDOMNode(Source source) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        DOMSource domSrc = toDOMSource(source);
        return domSrc != null ? domSrc.getNode() : null;
    }

    /**
     * Create a DOM element from the given source.
     */
    @Converter
    public Element toDOMElement(Source source) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        Node node = toDOMNode(source);
        return toDOMElement(node);
    }

    /**
     * Create a DOM element from the DOM node.
     * Simply cast if the node is an Element, or
     * return the root element if it is a Document.
     */
    @Converter
    public Element toDOMElement(Node node) throws TransformerException {
        // If the node is an document, return the root element
        if (node instanceof Document) {
            return ((Document) node).getDocumentElement();
            // If the node is an element, just cast it
        } else if (node instanceof Element) {
            return (Element) node;
            // Other node types are not handled
        } else {
            throw new TransformerException("Unable to convert DOM node to an Element");
        }
    }

    /**
     * Converts the given data to a DOM document
     *
     * @param data is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(byte[] data) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(data));
    }

    /**
     * Converts the given {@link InputStream} to a DOM document
     *
     * @param in is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        return documentBuilder.parse(in);
    }

    /**
     * Converts the given {@link InputStream} to a DOM document
     *
     * @param in is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(Reader in) throws IOException, SAXException, ParserConfigurationException {
        return toDOMDocument(new InputSource(in));
    }

    /**
     * Converts the given {@link InputSource} to a DOM document
     *
     * @param in is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(InputSource in) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        return documentBuilder.parse(in);
    }

    /**
     * Converts the given {@link String} to a DOM document
     *
     * @param text is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(String text) throws IOException, SAXException, ParserConfigurationException {
        return toDOMDocument(new StringReader(text));
    }

    /**
     * Converts the given {@link File} to a DOM document
     *
     * @param file is the data to be parsed
     * @return the parsed document
     */
    @Converter
    public Document toDOMDocument(File file) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder documentBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        return documentBuilder.parse(file);
    }

    /**
     * Create a DOM document from the given source.
     */
    @Converter
    public Document toDOMDocument(Source source) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        Node node = toDOMNode(source);
        return toDOMDocument(node);
    }

    /**
     * Create a DOM document from the given Node.
     *
     * If the node is an document, just cast it, if the node is an root element, retrieve its
     * owner element or create a new document and import the node.
     */
    @Converter
    public Document toDOMDocument(final Node node) throws ParserConfigurationException, TransformerException {
        ObjectHelper.notNull(node, "node");

        // If the node is the document, just cast it
        if (node instanceof Document) {
            return (Document) node;
            // If the node is an element
        } else if (node instanceof Element) {
            Element elem = (Element) node;
            // If this is the root element, return its owner document
            if (elem.getOwnerDocument().getDocumentElement() == elem) {
                return elem.getOwnerDocument();
                // else, create a new doc and copy the element inside it
            } else {
                Document doc = createDocument();
                // import node must not occur concurrent on the same node (must be its owner)
                // so we need to synchronize on it
                synchronized (node.getOwnerDocument()) {
                    doc.appendChild(doc.importNode(node, true));
                }
                return doc;
            }
            // other element types are not handled
        } else {
            throw new TransformerException("Unable to convert DOM node to a Document: " + node);
        }
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public InputStream toInputStream(DOMSource source) throws TransformerException, IOException {
        return toInputStream(source, null);
    }
    
    @Converter
    public InputStream toInputStream(DOMSource source, Exchange exchange) throws TransformerException, IOException {
        String s = toString(source, exchange);
        return new ByteArrayInputStream(s.getBytes());
    }

    @Deprecated
    //It will be removed in Camel 3.0, please use the method which take the exchange as the parameter
    public InputStream toInputStream(Document dom) throws TransformerException, IOException {
        return toInputStream(dom, null);
    }
    
    @Converter
    public InputStream toInputStream(Document dom, Exchange exchange) throws TransformerException, IOException {
        String s = toString(dom, exchange);
        return new ByteArrayInputStream(s.getBytes());
    }

    @Converter
    public InputSource toInputSource(InputStream is) {
        return new InputSource(is);
    }

    // Properties
    //-------------------------------------------------------------------------
    public DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = createDocumentBuilderFactory();
        }
        return documentBuilderFactory;
    }

    public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
        this.documentBuilderFactory = documentBuilderFactory;
    }


    // Helper methods
    //-------------------------------------------------------------------------
    public DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        return factory;
    }


    public DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = getDocumentBuilderFactory();
        return factory.newDocumentBuilder();
    }

    public Document createDocument() throws ParserConfigurationException {
        DocumentBuilder builder = createDocumentBuilder();
        return builder.newDocument();
    }

    public TransformerFactory getTransformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = createTransformerFactory();
        }
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    /**
     * @deprecated use {@link #createTransformer}, will be removed in Camel 3.0
     */
    @Deprecated
    public Transformer createTransfomer() throws TransformerConfigurationException {
        return createTransformer();
    }

    public Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = getTransformerFactory();
        return factory.newTransformer();
    }

    public TransformerFactory createTransformerFactory() {
        return TransformerFactory.newInstance();
    }

}
