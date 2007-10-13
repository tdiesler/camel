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
package org.apache.camel.spring.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.model.dataformat.ArtixDSDataFormat;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.model.dataformat.SerializationDataFormat;
import org.apache.camel.model.dataformat.XMLBeansDataFormat;
import org.apache.camel.spi.NamespaceAware;
import org.apache.camel.spring.CamelBeanPostProcessor;
import org.apache.camel.spring.CamelContextFactoryBean;
import org.apache.camel.spring.EndpointFactoryBean;
import org.apache.camel.spring.remoting.CamelProxyFactoryBean;
import org.apache.camel.spring.remoting.CamelServiceExporter;
import org.apache.camel.util.ObjectHelper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CamelNamespaceHandler extends NamespaceHandlerSupport {
    public static final String JAXB_PACKAGES = "org.apache.camel.spring:org.apache.camel.model:org.apache.camel.model.config:org.apache.camel.model.dataformat:org.apache.camel.model.language";
    protected BeanDefinitionParser endpointParser = new BeanDefinitionParser(EndpointFactoryBean.class);
    protected BeanDefinitionParser beanPostProcessorParser = new BeanDefinitionParser(CamelBeanPostProcessor.class);
    protected Set<String> parserElementNames = new HashSet<String>();
    private JAXBContext jaxbContext;
    private Map<String, BeanDefinitionParser> parserMap = new HashMap<String, BeanDefinitionParser>();
    private Binder<Node> binder;

    public void init() {
        // remoting
        addBeanDefinitionParser("proxy", CamelProxyFactoryBean.class);
        addBeanDefinitionParser("export", CamelServiceExporter.class);

        // data types
        addBeanDefinitionParser("artixDS", ArtixDSDataFormat.class);
        addBeanDefinitionParser("jaxb", JaxbDataFormat.class);
        addBeanDefinitionParser("serialization", SerializationDataFormat.class);
        addBeanDefinitionParser("xmlBeans", XMLBeansDataFormat.class);

        // TODO switch to use the above mechanism?
        registerParser("endpoint", endpointParser);

        registerParser("camelContext", new CamelContextBeanDefinitionParser(CamelContextFactoryBean.class));
    }

    private void addBeanDefinitionParser(String elementName, Class<?> type) {
        BeanDefinitionParser parser = new BeanDefinitionParser(type);
        registerParser(elementName, parser);
        parserMap.put(elementName, parser);
    }

    protected void createBeanPostProcessor(ParserContext parserContext, String contextId, Element childElement) {
        String beanPostProcessorId = contextId + ":beanPostProcessor";
        childElement.setAttribute("id", beanPostProcessorId);
        BeanDefinition definition = beanPostProcessorParser.parse(childElement, parserContext);
        definition.getPropertyValues().addPropertyValue("camelContext", new RuntimeBeanReference(contextId));
    }

    protected void registerScriptParser(String elementName, String engineName) {
        registerParser(elementName, new ScriptDefinitionParser(engineName));
    }

    protected void registerParser(String name, org.springframework.beans.factory.xml.BeanDefinitionParser parser) {
        parserElementNames.add(name);
        registerBeanDefinitionParser(name, parser);
    }

    public Set<String> getParserElementNames() {
        return parserElementNames;
    }

    protected Object parseUsingJaxb(Element element, ParserContext parserContext) {
        try {
            binder = getJaxbContext().createBinder();
            return binder.unmarshal(element);
/*
            Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
            return unmarshaller.unmarshal(element);
*/
        }
        catch (JAXBException e) {
            throw new BeanDefinitionStoreException("Failed to parse JAXB element: " + e, e);
        }
    }

    protected JAXBContext getJaxbContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = createJaxbContext();
        }
        return jaxbContext;
    }

    protected JAXBContext createJaxbContext() throws JAXBException {
        return JAXBContext.newInstance(JAXB_PACKAGES);
    }

    protected class CamelContextBeanDefinitionParser extends BeanDefinitionParser {
        public CamelContextBeanDefinitionParser(Class type) {
            super(type);
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            super.doParse(element, parserContext, builder);

            String contextId = element.getAttribute("id");

            // lets avoid folks having to explicitly give an ID to a camel
            // context
            if (ObjectHelper.isNullOrBlank(contextId)) {
                contextId = "camelContext";
                element.setAttribute("id", contextId);
            }

            // now lets parse the routes
            Object value = parseUsingJaxb(element, parserContext);
            if (value instanceof CamelContextFactoryBean) {
                CamelContextFactoryBean factoryBean = (CamelContextFactoryBean) value;
                builder.addPropertyValue("id", contextId);
                builder.addPropertyValue("routes", factoryBean.getRoutes());

                if (factoryBean.getPackages().length > 0) {
                    builder.addPropertyValue("packages", factoryBean.getPackages());
                }
            }

            boolean createdBeanPostProcessor = false;
            NodeList list = element.getChildNodes();
            int size = list.getLength();
            for (int i = 0; i < size; i++) {
                Node child = list.item(i);
                if (child instanceof Element) {
                    Element childElement = (Element) child;
                    String localName = child.getLocalName();
                    if (localName.equals("beanPostProcessor")) {
                        createBeanPostProcessor(parserContext, contextId, childElement);
                        createdBeanPostProcessor = true;
                    }
                    else if (localName.equals("endpoint")) {
                        BeanDefinition definition = endpointParser.parse(childElement, parserContext);
                        String id = childElement.getAttribute("id");
                        if (ObjectHelper.isNotNullAndNonEmpty(id)) {
                            // TODO we can zap this?
                            definition.getPropertyValues().addPropertyValue("camelContext", new RuntimeBeanReference(contextId));
                            // definition.getPropertyValues().addPropertyValue("context",
                            // builder.getBeanDefinition());
                            parserContext.registerComponent(new BeanComponentDefinition(definition, id));
                        }
                    }
                    else {
                        BeanDefinitionParser parser = parserMap.get(localName);
                        if (parser != null) {
                            BeanDefinition definition = parser.parse(childElement, parserContext);
                            String id = childElement.getAttribute("id");
                            if (ObjectHelper.isNotNullAndNonEmpty(id)) {
                                parserContext.registerComponent(new BeanComponentDefinition(definition, id));
                            }
                        }
                    }
                }
            }
            // lets inject the namespaces into any namespace aware POJOs
            injectNamespaces(element);
            if (!createdBeanPostProcessor) {
                // no bean processor element so lets add a fake one
                Element childElement = element.getOwnerDocument().createElement("beanPostProcessor");
                element.appendChild(childElement);
                createBeanPostProcessor(parserContext, contextId, childElement);
            }
        }
    }

    protected void injectNamespaces(Element element) {
        NodeList list = element.getChildNodes();
        Namespaces namespaces = null;
        int size = list.getLength();
        for (int i = 0; i < size; i++) {
            Node child = list.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                Object object = binder.getJAXBNode(child);
                if (object instanceof NamespaceAware) {
                    NamespaceAware namespaceAware = (NamespaceAware) object;
                    if (namespaces == null) {
                        namespaces = new Namespaces(element);
                    }
                    namespaces.configure(namespaceAware);
                }
                injectNamespaces(childElement);
            }
        }
    }
}
