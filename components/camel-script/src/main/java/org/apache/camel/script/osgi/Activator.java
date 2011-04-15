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
package org.apache.camel.script.osgi;

import java.lang.reflect.Method;

import javax.script.ScriptEngine;

import org.apache.camel.spi.ScriptEngineResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final transient Logger LOG = LoggerFactory.getLogger(Activator.class);
    private static BundleContext context;
    private static Bundle bundle;
    

    public static Bundle getBundle() {
        return bundle;
    }
    
    public static BundleContext getBundleContext() {
        return context;
    }
    
    public static ScriptEngine resolveScriptEngine(String scriptEngineName) throws InvalidSyntaxException {
        ServiceReference[] refs = context.getServiceReferences(ScriptEngineResolver.class.getName(), null);
        if (refs == null) {
            LOG.info("No OSGi script engine resolvers available!");
            return null;
        }
        
        LOG.debug("Found " + refs.length + " OSGi ScriptEngineResolver services");
        
        for (ServiceReference ref : refs) {
            ScriptEngineResolver resolver = (ScriptEngineResolver) context.getService(ref);
            ScriptEngine engine = resolver.resolveScriptEngine(scriptEngineName);
            context.ungetService(ref);
            LOG.debug("OSGi resolver " + resolver + " produced " + scriptEngineName + " engine " + engine);
            if (engine != null) {
                return engine;
            }
        }
        return null;
    }

    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        Activator.bundle = context.getBundle();
    }

    public void stop(BundleContext context) throws Exception {
        Activator.context = null;
        Activator.bundle = null;
    }
}
