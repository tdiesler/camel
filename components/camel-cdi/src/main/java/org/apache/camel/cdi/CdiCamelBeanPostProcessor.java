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
package org.apache.camel.cdi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.camel.BeanInject;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.PropertyInject;
import org.apache.camel.impl.CamelPostProcessorHelper;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;
import org.apache.camel.util.ReflectionHelper;

@Vetoed
final class CdiCamelBeanPostProcessor extends DefaultCamelBeanPostProcessor {

    private final BeanManager manager;

    private final Map<String, CamelPostProcessorHelper> postProcessorHelpers = new HashMap<>();

    // TODO: proper support for multi Camel contexts and custom context qualifiers
    CdiCamelBeanPostProcessor(BeanManager manager) {
        this.manager = manager;
    }

    protected void injectFields(final Object bean, final String beanName) {
        ReflectionHelper.doWithFields(bean.getClass(), new ReflectionHelper.FieldCallback() {
            public void doWith(Field field) throws IllegalAccessException {
                PropertyInject propertyInject = field.getAnnotation(PropertyInject.class);
                if (propertyInject != null) {
                    try {
                        injectFieldProperty(field, propertyInject.value(), propertyInject.defaultValue(), propertyInject.context(), bean, beanName);
                    } catch (Exception cause) {
                        throw new InjectionException("Injection of [" + propertyInject + "] for field [" + field + "] failed!", cause);
                    }
                }

                BeanInject beanInject = field.getAnnotation(BeanInject.class);
                // TODO: proper support for multi Camel contexts
                if (beanInject != null && getPostProcessorHelper().matchContext(beanInject.context())) {
                    try {
                        injectFieldBean(field, beanInject.value(), bean, beanName);
                    } catch (Exception cause) {
                        throw new InjectionException("Injection of [" + beanInject + "] for field [" + field + "] failed!", cause);
                    }
                }

                EndpointInject endpointInject = field.getAnnotation(EndpointInject.class);
                if (endpointInject != null) {
                    try {
                        injectField(field, endpointInject.uri(), endpointInject.ref(), endpointInject.property(), endpointInject.context(), bean, beanName);
                    } catch (Exception cause) {
                        throw new InjectionException("Injection of [" + endpointInject + "] for field [" + field + "] failed!", cause);
                    }
                }

                Produce produce = field.getAnnotation(Produce.class);
                if (produce != null) {
                    try {
                        injectField(field, produce.uri(), produce.ref(), produce.property(), produce.context(), bean, beanName);
                    } catch (Exception cause) {
                        throw new InjectionException("Injection of [" + produce + "] for field [" + field + "] failed!", cause);
                    }
                }
            }
        });
    }

    private void injectField(Field field, String uri, String ref, String property, String context, Object bean, String beanName) {
        ReflectionHelper.setField(field, bean, getPostProcessorHelper(context).getInjectionValue(field.getType(), uri, ref, property, field.getName(), bean, beanName));
    }

    private void injectFieldProperty(Field field, String property, String defaultValue, String context, Object bean, String beanName) {
        ReflectionHelper.setField(field, bean, getPostProcessorHelper(context).getInjectionPropertyValue(field.getType(), property, defaultValue, field.getName(), bean, beanName));
    }

    private CamelPostProcessorHelper getPostProcessorHelper(String contextName) {
        CamelPostProcessorHelper helper = postProcessorHelpers.get(contextName);
        if (helper == null) {
            CamelContext context = getOrLookupCamelContext(contextName);
            if (context == null) {
                throw new UnsatisfiedResolutionException("No Camel context with name [" + contextName + "] is deployed!");
            }
            helper = new CamelPostProcessorHelper(context);
            postProcessorHelpers.put(contextName, helper);
        }
        return helper;
    }

    private CamelContext getOrLookupCamelContext(String contextName) {
        // TODO: proper support for custom context qualifiers
        return BeanManagerHelper.getReferenceByType(manager, CamelContext.class, contextName.isEmpty() ? DefaultLiteral.INSTANCE : new ContextName.Literal(contextName));
    }

    @Override
    public CamelContext getOrLookupCamelContext() {
        return BeanManagerHelper.getReferenceByType(manager, CamelContext.class);
    }
}
