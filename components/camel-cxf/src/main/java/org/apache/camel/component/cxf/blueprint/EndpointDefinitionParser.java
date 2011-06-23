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

package org.apache.camel.component.cxf.blueprint;

import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.camel.component.cxf.CxfBlueprintEndpoint;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.blueprint.AbstractBPBeanDefinitionParser;
import org.apache.cxf.helpers.DOMUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.reflect.Metadata;

public class EndpointDefinitionParser extends AbstractBPBeanDefinitionParser {

    public static String getIdOrName(Element elem) {
        String id = elem.getAttribute("id");

        if (null == id || "".equals(id)) {
            String names = elem.getAttribute("name");
            if (null != names) {
                StringTokenizer st = new StringTokenizer(names, ",");
                if (st.countTokens() > 0) {
                    id = st.nextToken();
                }
            }
        }
        return id;
    }

    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata endpointConfig = context.createMetadata(MutableBeanMetadata.class);
        endpointConfig.setRuntimeClass(CxfBlueprintEndpoint.class);
        endpointConfig.addProperty("blueprintContainer", createRef(context, "blueprintContainer"));
        endpointConfig.addProperty("bundleContext", createRef(context, "blueprintBundleContext"));

        if (!StringUtils.isEmpty(getIdOrName(element))) {
            endpointConfig.setId(getIdOrName(element));
        } else {
            endpointConfig.setId("camel.cxf.endpoint." + context.generateId());
        }

        NamedNodeMap atts = element.getAttributes();

        String bus = null;
        String address = null;

        for (int i = 0; i < atts.getLength(); i++) {
            Attr node = (Attr) atts.item(i);
            String val = node.getValue();
            String pre = node.getPrefix();
            String name = node.getLocalName();
            if ("bus".equals(name)) {
                bus = val;
            } else if ("address".equals(name)) {
                address = val;
            } else if (isAttribute(pre, name)) {
                if ("endpointName".equals(name) || "serviceName".equals(name)) {
                    QName q = parseQName(element, val);
                    endpointConfig.addProperty(name, createValue(context, q));
                } else if ("depends-on".equals(name)) {
                    endpointConfig.addDependsOn(val);
                } else if (!"name".equals(name)) {
                    endpointConfig.addProperty(name, AbstractBPBeanDefinitionParser.createValue(context, val));
                }
            }
        }

        Element elem = DOMUtils.getFirstElement(element);
        while (elem != null) {
            String name = elem.getLocalName();
            if ("properties".equals(name)) {
                Metadata map = parseMapData(context, endpointConfig, elem);
                endpointConfig.addProperty(name, map);
            } else if ("binding".equals(name)) {
                setFirstChildAsProperty(element, context, endpointConfig, "bindingConfig");
            } else if ("inInterceptors".equals(name) || "inFaultInterceptors".equals(name) || "outInterceptors".equals(name)
                || "outFaultInterceptors".equals(name) || "features".equals(name) || "schemaLocations".equals(name) || "handlers".equals(name)) {
                Metadata list = parseListData(context, endpointConfig, elem);
                endpointConfig.addProperty(name, list);
            } else {
                setFirstChildAsProperty(element, context, endpointConfig, name);
            }

            elem = DOMUtils.getNextElement(elem);
        }
        if (StringUtils.isEmpty(bus)) {
            bus = "cxf";
        }
        //Will create a bus if needed...

        endpointConfig.addProperty("bus", getBusRef(context, bus));
        endpointConfig.setDestroyMethod("destroy");
        endpointConfig.addArgument(AbstractBPBeanDefinitionParser.createValue(context, address), String.class.getName(), 0);
        endpointConfig.addArgument(createRef(context, "blueprintBundleContext"),
                                   BundleContext.class.getName(), 1);

        return endpointConfig;
    }

    public static class PassThroughCallable<T> implements Callable<T> {

        private T value;

        public PassThroughCallable(T value) {
            this.value = value;
        }

        public T call() throws Exception {
            return value;
        }
    }
}
