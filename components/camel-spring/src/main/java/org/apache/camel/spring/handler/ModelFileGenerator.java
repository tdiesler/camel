package org.apache.camel.spring.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.model.RouteType;
import org.apache.camel.model.RoutesType;
import org.apache.camel.spring.handler.CamelNamespaceHandler;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.RuntimeTransformException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ModelFileGenerator extends CamelNamespaceHandler {
    
    private static final String DEFAULT_ROOT_ELEMENT_NAME = "routes";

    /**
     * Write the specified 'routeTypes' to 'fileName' as XML using JAXB.
     */
    public void marshalRoutesUsingJaxb(String fileName, List<RouteType> routeTypes) throws IOException {      
        OutputStream outputStream = outputStream(fileName);
        
        try {
            XmlConverter converter = converter();
            Document doc = converter.createDocument();
        
            Element root = doc.createElement(rootElementName());
            root.setAttribute("xmlns", Namespaces.DEFAULT_NAMESPACE);     
            doc.appendChild(root);
    
            for (RouteType routeType : routeTypes) {
                addJaxbElementToNode(root, routeType); 
            }      
    
            Result result = new StreamResult(new OutputStreamWriter(outputStream, XmlConverter.defaultCharset));
    
            copyToResult(converter, doc, result);
        } catch (ParserConfigurationException e) {
            throw new RuntimeTransformException(e);                
        } catch (TransformerException e) {
            throw new RuntimeTransformException(e);
        } finally {
            outputStream.close();
        }
    }

    /**
     * Returns a configured XmlConverter
     */
    private XmlConverter converter() {
        XmlConverter converter = new XmlConverter();
        TransformerFactory transformerFactory = converter.getTransformerFactory();
        transformerFactory.setAttribute("indent-number", new Integer(2));
        return converter;
    }   
    
    /**
     * Copies the given input Document into the required result using the provided converter.
     */
    private void copyToResult(XmlConverter converter, Document doc, Result result) throws TransformerException {       
        Properties outputProperties = converter.defaultOutputProperties();
        outputProperties.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        outputProperties.put(OutputKeys.INDENT, "yes");
        
        converter.toResult(converter.toSource(doc), result, outputProperties);        
    }

    /**
     * Convert the specified object into XML and add it as a child of 'node' using JAXB.
     */
    private void addJaxbElementToNode(Node node, Object jaxbElement) {
        try {    
            binder = getJaxbContext().createBinder();
            binder.marshal(jaxbElement, node);          
        } catch (JAXBException e) {
            throw new RuntimeCamelException(e);
        }
    }

    /**
     * Return the root element name for the list of routes.
     */
    private String rootElementName() {
        XmlRootElement annotation = (RoutesType.class).getAnnotation(XmlRootElement.class);
        if (annotation != null) {
            String elementName = annotation.name();
            if (ObjectHelper.isNotNullAndNonEmpty(elementName)) {
                return elementName;
            }          
        }
        return DEFAULT_ROOT_ELEMENT_NAME;
    }    

    /**
     * returns an output stream for the filename specified.
     */
    private OutputStream outputStream(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();     
            }
        }
        return new FileOutputStream(file);
    }
}
