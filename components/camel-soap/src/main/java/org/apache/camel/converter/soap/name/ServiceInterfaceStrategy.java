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
package org.apache.camel.converter.soap.name;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.camel.RuntimeCamelException;

/**
 * Offers a finder for a webservice interface to determine the QName of a
 * webservice data element
 */
public class ServiceInterfaceStrategy implements ElementNameStrategy {
    private Map<String, MethodInfo> soapActionToMethodInfo = new HashMap<String, MethodInfo>();
    private Map<String, QName> inTypeNameToQName = new HashMap<String, QName>();
    private Map<String, QName> outTypeNameToQName = new HashMap<String, QName>();
    private boolean isClient;
    private ElementNameStrategy fallBackStrategy;

    /**
     * Init with JAX-WS service interface
     * 
     * @param serviceInterface
     * @param isClient
     *            determines if marhalling looks at input or output of method
     */
    public ServiceInterfaceStrategy(Class<?> serviceInterface, boolean isClient) {
        analyzeServiceInterface(serviceInterface);
        this.isClient = isClient;
        this.fallBackStrategy = new TypeNameStrategy();
    }

    private TypeInfo getOutInfo(Method method) {
        ResponseWrapper respWrap = method.getAnnotation(ResponseWrapper.class);
        if (respWrap != null) {
            if (respWrap.className() != null) {
                return new TypeInfo(respWrap.className(), new QName(respWrap.targetNamespace(), respWrap.localName()));
            }
        } else {
            Class<?> type = method.getReturnType();
            WebResult webResult = method.getAnnotation(WebResult.class);
            if (webResult != null) {
                return new TypeInfo(type.getName(), new QName(webResult.targetNamespace(), webResult.name()));
            }
        }
        throw new RuntimeCamelException("The method " + method.getName()
                + " has no ResponseWrapper and no suitable return type");
    }

    private TypeInfo getInInfo(Method method) {
        RequestWrapper requestWrapper = method.getAnnotation(RequestWrapper.class);
        Class<?>[] types = method.getParameterTypes();
        if (requestWrapper != null) {
            if (requestWrapper.className() != null) {
                return new TypeInfo(requestWrapper.className(), new QName(requestWrapper.targetNamespace(),
                        requestWrapper.localName()));
            }
        } else if (types.length == 1) {
            Annotation[] firstParamAnnotations = method.getParameterAnnotations()[0];
            for (Annotation annotation : firstParamAnnotations) {
                if (annotation instanceof WebParam) {
                    WebParam webParam = (WebParam) annotation;
                    return new TypeInfo(types[0].getName(), new QName(webParam.targetNamespace(), webParam.name()));
                }
            }
        }
        return null;
    }

    /**
     * Determines how the parameter object of the service method will be named
     * in xml. It will use either the RequestWrapper annotation of the method if
     * present or the WebParam method of the parameter.
     * 
     * @param method
     */
    private MethodInfo analyzeMethod(Method method) {
        TypeInfo inInfo = getInInfo(method);
        TypeInfo outInfo = getOutInfo(method);
        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        String soapAction = (webMethod != null) ? webMethod.action() : null;
        return new MethodInfo(soapAction, inInfo, outInfo);
    }

    private void analyzeServiceInterface(Class<?> serviceInterface) {
        Method[] methods = serviceInterface.getMethods();
        for (Method method : methods) {
            MethodInfo info = analyzeMethod(method);
            inTypeNameToQName.put(info.getIn().getTypeName(), info.getIn().getElName());
            if (info.getSoapAction() != null && !"".equals(info.getSoapAction())) {
                soapActionToMethodInfo.put(info.getSoapAction(), info);
            }
            outTypeNameToQName.put(info.getOut().getTypeName(), info.getOut().getElName());
        }
    }

    /**
     * Determine the QName of the method parameter of the method that matches
     * either soapAction and type or if not possible only the type
     * 
     * @param soapAction
     * @param type
     * @return matching QName throws RuntimeException if no matching QName was
     *         found
     */
    public QName findQNameForSoapActionOrType(String soapAction, Class<?> type) {
        MethodInfo info = soapActionToMethodInfo.get(soapAction);
        if (info != null) {
            if (isClient) {
                return info.getIn().getElName();
            } else {
                return info.getOut().getElName();
            }
        }
        QName qName = null;
        if (isClient) {
            qName = inTypeNameToQName.get(type.getName());
        } else {
            qName = outTypeNameToQName.get(type.getName());
        }
        if (qName == null) {
            try {
                qName = fallBackStrategy.findQNameForSoapActionOrType(soapAction, type);
            } catch (Exception e) {
                String msg = "No method found that matches the given SoapAction " + soapAction + " or that has an "
                        + (isClient ? "input" : "output") + " of type " + type.getName();
                throw new RuntimeCamelException(msg, e);
            }
        }
        return qName;
    }
}
