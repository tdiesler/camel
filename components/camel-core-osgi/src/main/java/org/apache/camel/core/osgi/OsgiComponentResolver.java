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
package org.apache.camel.core.osgi;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.spi.ComponentResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OsgiComponentResolver implements ComponentResolver {
    
    private static final transient Log LOG = LogFactory.getLog(OsgiComponentResolver.class);

    @SuppressWarnings("unchecked")
    public Component resolveComponent(String name, CamelContext context) throws Exception {
        Object bean = null;
        try {
            bean = context.getRegistry().lookup(name);
            if (bean != null && LOG.isDebugEnabled()) {
                LOG.debug("Found component: " + name + " in registry: " + bean);
            }
        } catch (Exception e) {
            LOG.debug("Ignored error looking up bean: " + name + ". Error: " + e);
        }
        if (bean != null) {
            if (bean instanceof Component) {
                return (Component)bean;
            }
            // we do not throw the exception here and try to auto create a component
        }

        // Check in OSGi bundles
        Class type;
        try {
            type = getComponent(name);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid URI, no Component registered for scheme : " + name, e);
        }
        if (type == null) {
            return null;
        }

        if (Component.class.isAssignableFrom(type)) {
            return (Component)context.getInjector().newInstance(type);
        } else {
            throw new IllegalArgumentException("Type is not a Component implementation. Found: " + type.getName());
        }
    }

    protected Class getComponent(String name) throws Exception {
        return Activator.getComponent(name);
    }

}
