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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.Component;
import org.apache.camel.TypeConverter;
import org.apache.camel.osgi.tracker.BundleTracker;
import org.apache.camel.osgi.tracker.BundleTrackerCustomizer;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.LanguageResolver;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

public class Activator implements BundleActivator, BundleTrackerCustomizer {
    public static final String META_INF_TYPE_CONVERTER = "META-INF/services/org/apache/camel/TypeConverter";
    public static final String META_INF_COMPONENT = "META-INF/services/org/apache/camel/component/";
    public static final String META_INF_LANGUAGE = "META-INF/services/org/apache/camel/language/";
    public static final String META_INF_LANGUAGE_RESOLVER = "META-INF/services/org/apache/camel/language/resolver/";
    
    private static final transient Log LOG = LogFactory.getLog(Activator.class);    
    private static final Map<String, ComponentEntry> COMPONENTS = new ConcurrentHashMap<String, ComponentEntry>();
    private static final Map<URL, TypeConverterEntry> TYPE_CONVERTERS = new ConcurrentHashMap<URL, TypeConverterEntry>();
    private static final Map<String, ComponentEntry> LANGUAGES = new ConcurrentHashMap<String, ComponentEntry>();
    private static final Map<String, ComponentEntry> LANGUAGE_RESOLVERS = new ConcurrentHashMap<String, ComponentEntry>();
    private static Bundle bundle;
    
    private BundleTracker tracker;

    private class ComponentEntry {
        Bundle bundle;
        String path;
        String name;
        Class type;
    }
    
    public class TypeConverterEntry {
        Bundle bundle;
        URL resource;
        Set<String> converterPackages;
    }
    
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        modifiedBundle(bundle, event, null);
        return bundle;
    }

    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Bundle started: " + bundle.getSymbolicName());
        }
        mayBeAddComponentAndLanguageFor(bundle);
        mayBeAddTypeConverterFor(bundle);
    }

    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Bundle stopped: " + bundle.getSymbolicName());
        }
        mayBeRemoveComponentAndLanguageFor(bundle);
        mayBeRemoveTypeConverterFor(bundle);
    }

    protected void addComponentEntry(String entryPath, Bundle bundle, Map<String, ComponentEntry> entries, Class clazz) {
        // Check bundle compatibility
        try {
            if (bundle.loadClass(clazz.getName()) != clazz) {
                return;
            }
        } catch (Throwable t) {
            return;
        }
        Enumeration e = bundle.getEntryPaths(entryPath);
        if (e != null) {
            while (e.hasMoreElements()) {
                String path = (String)e.nextElement();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found entry: " + path + " in bundle " + bundle.getSymbolicName());
                }
                ComponentEntry entry = new ComponentEntry();
                entry.bundle = bundle;
                entry.path = path;
                entry.name = path.substring(path.lastIndexOf("/") + 1);
                entries.put(entry.name, entry);
            }
        }
        
    }

    protected void mayBeAddComponentAndLanguageFor(Bundle bundle) {        
        addComponentEntry(META_INF_COMPONENT, bundle, COMPONENTS, Component.class);
        addComponentEntry(META_INF_LANGUAGE, bundle, LANGUAGES, Language.class);
        addComponentEntry(META_INF_LANGUAGE_RESOLVER, bundle, LANGUAGE_RESOLVERS, LanguageResolver.class);
    }
    
    protected void mayBeAddTypeConverterFor(Bundle bundle) {
        // Check bundle compatibility
        try {
            Class clazz = TypeConverter.class;
            if (bundle.loadClass(clazz.getName()) != clazz) {
                return;
            }
        } catch (Throwable t) {
            return;
        }
        try {
            Enumeration e = bundle.getResources(META_INF_TYPE_CONVERTER);
            if (e != null) {
                while (e.hasMoreElements()) {
                    URL resource = (URL)e.nextElement();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found entry: " + resource + " in bundle " + bundle.getSymbolicName());
                    }
                    TypeConverterEntry entry = new TypeConverterEntry();
                    entry.bundle = bundle;                   
                    entry.resource = resource;
                    entry.converterPackages = getConverterPackages(resource);
                    TYPE_CONVERTERS.put(resource, entry);
                }
            }
        } catch (IOException ignore) {
            // can't find the resource
        }
    }

    protected void mayBeRemoveComponentAndLanguageFor(Bundle bundle) {
        removeComponentEntry(bundle, COMPONENTS);
        removeComponentEntry(bundle, LANGUAGES);
        removeComponentEntry(bundle, LANGUAGE_RESOLVERS);
    }
    
    protected void removeComponentEntry(Bundle bundle, Map<String, ComponentEntry> entries) {
        ComponentEntry[] entriesArray = entries.values().toArray(new ComponentEntry[0]);
        for (ComponentEntry entry : entriesArray) {        
            if (entry.bundle == bundle) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing entry: " + entry.path + " in bundle " + bundle.getSymbolicName());
                }
                entries.remove(entry.name);
            }
        }        
    }
    
    protected void mayBeRemoveTypeConverterFor(Bundle bundle) {
        TypeConverterEntry[] entriesArray = TYPE_CONVERTERS.values().toArray(new TypeConverterEntry[0]);
        for (TypeConverterEntry entry : entriesArray) {
            if (entry.bundle == bundle) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing entry: " + entry.resource + " in bundle " + bundle.getSymbolicName());
                }
                COMPONENTS.remove(entry.resource);
            }
        }
    }

    public void start(BundleContext context) throws Exception {
        LOG.info("Camel activator starting");

        bundle = context.getBundle();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using bundle: " + bundle);
        }

        tracker = new BundleTracker(context, Bundle.ACTIVE, this);
        tracker.open();
        LOG.info("Camel activator started");
    }

    public void stop(BundleContext context) throws Exception {
        LOG.info("Camel activator stopping");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing Camel bundles");
        }
        tracker.close();
        LOG.info("Camel activator stopped");
    }
    
    protected Set<String> getConverterPackages(URL resource) {
        Set<String> packages = new HashSet<String>();
        if (resource != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(resource.openStream()));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.startsWith("#") || line.length() == 0) {
                        continue;
                    }
                    tokenize(packages, line);
                }
            } catch (Exception ignore) {
                // Do nothing here
            } finally {
                if (reader != null) {
                    ObjectHelper.close(reader, null, LOG);
                }
            }
        }
        return packages;
    }
    
    protected void tokenize(Set<String> packages, String line) {
        StringTokenizer iter = new StringTokenizer(line, ",");
        while (iter.hasMoreTokens()) {
            String name = iter.nextToken().trim();
            if (name.length() > 0) {
                packages.add(name);
            }
        }
    }
    
    protected static Bundle getBundle() {
        return bundle;
    }

    protected static TypeConverterEntry[] getTypeConverterEntries() {
        Collection<TypeConverterEntry> entries = TYPE_CONVERTERS.values();
        return entries.toArray(new TypeConverterEntry[entries.size()]);
    }

    public static Class getComponent(String name) throws Exception {
        LOG.trace("Finding Component: " + name);
        return getClassFromEntries(name, COMPONENTS);
    }
    
    public static Class getLanguage(String name) throws Exception {
        LOG.trace("Finding Language: " + name);
        return getClassFromEntries(name, LANGUAGES);
    }
    
    public static Class getLanguageResolver(String name) throws Exception {
        LOG.trace("Finding LanguageResolver: " + name);
        return getClassFromEntries(name, LANGUAGE_RESOLVERS);
    }
    
    protected static Class getClassFromEntries(String name, Map<String, ComponentEntry> entries) throws Exception {
        ComponentEntry entry = entries.get(name);
        if (entry == null) {
            return null;
        }
        if (entry.type == null) {
            URL url = entry.bundle.getEntry(entry.path);            
            if (LOG.isDebugEnabled()) {
                LOG.debug("The entry " + name + "'s url is" + url);
            }
            // lets load the file
            Properties properties = new Properties();
            BufferedInputStream reader = null;
            try {
                reader = new BufferedInputStream(url.openStream());
                properties.load(reader);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception ignore) {
                }
            }
            String classname = (String)properties.get("class");
            ClassLoader loader = BundleDelegatingClassLoader.createBundleClassLoaderFor(entry.bundle);
            entry.type = loader.loadClass(classname);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found entry: " + name + " via type: " + entry.type.getName());
        }
        return entry.type;
    }

}
