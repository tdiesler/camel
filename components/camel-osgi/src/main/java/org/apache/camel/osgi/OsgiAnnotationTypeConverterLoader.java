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
package org.apache.camel.osgi;

import java.util.Set;

import org.apache.camel.Converter;
import org.apache.camel.impl.converter.AnnotationTypeConverterLoader;
import org.apache.camel.impl.converter.TypeConverterRegistry;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

public class OsgiAnnotationTypeConverterLoader extends AnnotationTypeConverterLoader {
    
    private static final transient Log LOG = LogFactory.getLog(OsgiAnnotationTypeConverterLoader.class);

    public OsgiAnnotationTypeConverterLoader(BundleContext context) {
        super(new OsgiResolverUtil(context));
    }
    
    @Override
    public void load(TypeConverterRegistry registry) throws Exception {
        for (Activator.TypeConverterEntry entry : Activator.getTypeConverterEntries()) {
            OsgiResolverUtil resolver = new OsgiResolverUtil(entry.bundle);
            String[] packages = entry.converterPackages.toArray(new String[entry.converterPackages.size()]);
            resolver.findAnnotated(Converter.class, packages);
            Set<Class> classes = resolver.getClasses();
            for (Class type : classes) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loading converter class: " + ObjectHelper.name(type));
                }
                loadConverterMethods(registry, type);
            }
        }
    }
}
