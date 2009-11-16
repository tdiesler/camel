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
package org.apache.camel.util;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.TypeConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper for introspections of beans.
 */
public final class IntrospectionSupport {

    private static final transient Log LOG = LogFactory.getLog(IntrospectionSupport.class);
    private static final Pattern GETTER_PATTERN = Pattern.compile("(get|is)[A-Z].*");
    private static final Pattern SETTER_PATTERN = Pattern.compile("set[A-Z].*");

    /**
     * Utility classes should not have a public constructor.
     */
    private IntrospectionSupport() {
    }

    public static boolean isGetter(Method method) {
        String name = method.getName();
        Class<?> type = method.getReturnType();
        Class<?> params[] = method.getParameterTypes();

        if (!GETTER_PATTERN.matcher(name).matches()) {
            return false;
        }

        // special for isXXX boolean
        if (name.startsWith("is")) {
            return params.length == 0 && type.getSimpleName().equalsIgnoreCase("boolean");
        }

        return params.length == 0 && !type.equals(Void.TYPE);
    }

    public static boolean isSetter(Method method) {
        String name = method.getName();
        Class<?> type = method.getReturnType();
        Class<?> params[] = method.getParameterTypes();

        if (!SETTER_PATTERN.matcher(name).matches()) {
            return false;
        }

        return params.length == 1 && type.equals(Void.TYPE);
    }

    @SuppressWarnings("unchecked")
    public static boolean getProperties(Object target, Map properties, String optionPrefix) {
        ObjectHelper.notNull(target, "target");
        ObjectHelper.notNull(properties, "properties");
        boolean rc = false;
        if (optionPrefix == null) {
            optionPrefix = "";
        }

        Class clazz = target.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            Class type = method.getReturnType();
            Class params[] = method.getParameterTypes();
            if (name.startsWith("get") && params.length == 0 && type != null && isSettableType(type)) {
                try {
                    Object value = method.invoke(target);
                    if (value == null) {
                        continue;
                    }

                    String strValue = convertToString(value, type);
                    if (strValue == null) {
                        continue;
                    }

                    name = name.substring(3, 4).toLowerCase() + name.substring(4);
                    properties.put(optionPrefix + name, strValue);
                    rc = true;
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }

        return rc;
    }

    public static boolean hasProperties(Map<String, Object> properties, String optionPrefix) {
        ObjectHelper.notNull(properties, "properties");

        if (ObjectHelper.isNotEmpty(optionPrefix)) {
            for (Object o : properties.keySet()) {
                String name = (String) o;
                if (name.startsWith(optionPrefix)) {
                    return true;
                }
            }
            // no parameters with this prefix
            return false;
        } else {
            return !properties.isEmpty();
        }
    }

    public static Object getProperty(Object target, String property) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ObjectHelper.notNull(target, "target");
        ObjectHelper.notNull(property, "property");

        property = property.substring(0, 1).toUpperCase() + property.substring(1);

        Class<?> clazz = target.getClass();
        Method method = getPropertyGetter(clazz, property);
        return method.invoke(target);
    }

    public static Method getPropertyGetter(Class<?> type, String propertyName) throws NoSuchMethodException {
        return type.getMethod("get" + ObjectHelper.capitalize(propertyName));
    }

    @SuppressWarnings("unchecked")
    public static boolean setProperties(Object target, Map properties, String optionPrefix) throws Exception {
        ObjectHelper.notNull(target, "target");
        ObjectHelper.notNull(properties, "properties");
        boolean rc = false;

        for (Iterator<Map.Entry> it = properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = it.next();
            String name = entry.getKey().toString();
            if (name.startsWith(optionPrefix)) {
                Object value = properties.get(name);
                name = name.substring(optionPrefix.length());
                if (setProperty(target, name, value)) {
                    it.remove();
                    rc = true;
                }
            }
        }

        return rc;
    }

    public static Map<String, Object> extractProperties(Map<String, Object> properties, String optionPrefix) {
        ObjectHelper.notNull(properties, "properties");

        HashMap<String, Object> rc = new LinkedHashMap<String, Object>(properties.size());

        for (Iterator<Map.Entry<String, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            if (name.startsWith(optionPrefix)) {
                Object value = properties.get(name);
                name = name.substring(optionPrefix.length());
                rc.put(name, value);
                it.remove();
            }
        }

        return rc;
    }

    public static boolean setProperties(TypeConverter typeConverter, Object target, Map<String, Object> properties) throws Exception {
        ObjectHelper.notNull(target, "target");
        ObjectHelper.notNull(properties, "properties");
        boolean rc = false;

        for (Iterator<Map.Entry<String, Object>> iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, Object> entry = iter.next();
            if (setProperty(typeConverter, target, (String)entry.getKey(), entry.getValue())) {
                iter.remove();
                rc = true;
            }
        }

        return rc;
    }

    public static boolean setProperties(Object target, Map<String, Object> properties) throws Exception {
        return setProperties(null, target, properties);
    }

    public static boolean setProperty(TypeConverter typeConverter, Object target, String name, Object value) throws Exception {
        try {
            Class<?> clazz = target.getClass();
            // find candidates of setter methods as there can be overloaded setters
            Set<Method> setters = findSetterMethods(typeConverter, clazz, name, value);
            if (setters.isEmpty()) {
                return false;
            }

            // loop and execute the best setter method
            Exception typeConvertionFailed = null;
            for (Method setter : setters) {
                // If the type is null or it matches the needed type, just use the value directly
                if (value == null || setter.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                    setter.invoke(target, value);
                    return true;
                } else {
                    // We need to convert it
                    try {
                        // ignore exceptions as there could be another setter method where we could type convert successfully
                        Object convertedValue = convert(typeConverter, setter.getParameterTypes()[0], value);
                        setter.invoke(target, convertedValue);
                        return true;
                    } catch (NoTypeConversionAvailableException e) {
                        typeConvertionFailed = e;
                    } catch (IllegalArgumentException e) {
                        typeConvertionFailed = e;
                    }
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Setter \"" + setter + "\" with parameter type \""
                                  + setter.getParameterTypes()[0] + "\" could not be used for type conversions of " + value);
                    }
                }
            }
            // we did not find a setter method to use, and if we did try to use a type converter then throw
            // this kind of exception as the caused by will hint this error
            if (typeConvertionFailed != null) {
                throw new IllegalArgumentException("Could not find a suitable setter for property: " + name
                        + " as there isn't a setter method with same type: " + value.getClass().getCanonicalName()
                        + " nor type conversion possible: " + typeConvertionFailed.getMessage());
            } else {
                return false;
            }
        } catch (InvocationTargetException e) {
            // lets unwrap the exception
            Throwable throwable = e.getCause();
            if (throwable instanceof Exception) {
                Exception exception = (Exception)throwable;
                throw exception;
            } else {
                Error error = (Error)throwable;
                throw error;
            }
        }
    }

    public static boolean setProperty(Object target, String name, Object value) throws Exception {
        return setProperty(null, target, name, value);
    }

    @SuppressWarnings("unchecked")
    private static Object convert(TypeConverter typeConverter, Class type, Object value)
        throws URISyntaxException, NoTypeConversionAvailableException {
        if (typeConverter != null) {
            return typeConverter.mandatoryConvertTo(type, value);
        }
        PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if (editor != null) {
            editor.setAsText(value.toString());
            return editor.getValue();
        }
        if (type == URI.class) {
            return new URI(value.toString());
        }
        return null;
    }

    private static String convertToString(Object value, Class<?> type) throws URISyntaxException {
        PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if (editor != null) {
            editor.setValue(value);
            return editor.getAsText();
        }
        if (type == URI.class) {
            return value.toString();
        }
        return null;
    }

    private static Set<Method> findSetterMethods(TypeConverter typeConverter, Class<?> clazz, String name, Object value) {
        Set<Method> candidates = new LinkedHashSet<Method>();

        // Build the method name.
        name = "set" + ObjectHelper.capitalize(name);
        while (clazz != Object.class) {
            // Since Object.class.isInstance all the objects,
            // here we just make sure it will be add to the bottom of the set.
            Method objectSetMethod = null;
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                Class<?> params[] = method.getParameterTypes();
                if (method.getName().equals(name) && params.length == 1) {
                    Class<?> paramType = params[0];
                    if (paramType.equals(Object.class)) {                        
                        objectSetMethod = method;
                    } else if (typeConverter != null || isSettableType(paramType) || paramType.isInstance(value)) {
                        candidates.add(method);
                    }
                }
            }
            if (objectSetMethod != null) {
                candidates.add(objectSetMethod);
            }
            clazz = clazz.getSuperclass();
        }

        if (candidates.isEmpty()) {
            return candidates;
        } else if (candidates.size() == 1) {
            // only one
            return candidates;
        } else {
            // find the best match if possible
            if (LOG.isTraceEnabled()) {
                LOG.trace("Found " + candidates.size() + " suitable setter methods for setting " + name);
            }
            // prefer to use the one with the same instance if any exists
            for (Method method : candidates) {                               
                if (method.getParameterTypes()[0].isInstance(value)) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Method " + method + " is the best candidate as it has parameter with same instance type");
                    }
                    // retain only this method in the answer
                    candidates.clear();
                    candidates.add(method);
                    return candidates;
                }
            }
            // fallback to return what we have found as candidates so far
            return candidates;
        }
    }

    private static boolean isSettableType(Class<?> clazz) {
        if (PropertyEditorManager.findEditor(clazz) != null) {
            return true;
        }
        if (clazz == URI.class) {
            return true;
        }
        if (clazz == Boolean.class) {
            return true;
        }
        return false;
    }
}
