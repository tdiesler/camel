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
package org.apache.camel.component.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A parser to parse a string which contains property placeholders
 *
 * @version $Revision$
 */
public final class PropertiesParser {
    private static final transient Log LOG = LogFactory.getLog(PropertiesParser.class);
    
    private PropertiesParser() {
    }

    /**
     * Parses the string and replaces the property placeholders with values from the given properties
     *
     * @param uri the uri
     * @param properties the properties
     * @param prefixToken the prefix token
     * @param suffixToken the suffix token
     * @return the uri with replaced placeholders
     * @throws IllegalArgumentException if uri syntax is not valid or a property is not found
     */
    public static String parseUri(String uri, Properties properties, String prefixToken, String suffixToken) throws IllegalArgumentException {
        String answer = uri;
        boolean done = false;

        // the placeholders can contain nested placeholders so we need to do recursive parsing
        // we must therefore also do circular reference check and must keep a list of visited keys
        List<String> visited = new ArrayList<String>();
        while (!done) {
            List<String> replaced = new ArrayList<String>();
            answer = doParseUri(answer, properties, replaced, prefixToken, suffixToken);

            // check the replaced with the visited to avoid circular reference
            for (String replace : replaced) {
                if (visited.contains(replace)) {
                    throw new IllegalArgumentException("Circular reference detected with key [" + replace + "] in uri " + uri);
                }
            }
            // okay all okay so add the replaced as visited
            visited.addAll(replaced);

            // are we done yet
            done = !answer.contains(prefixToken);
        }
        return answer;
    }

    private static String doParseUri(String uri, Properties properties, List<String> replaced, String prefixToken, String suffixToken) {
        StringBuilder sb = new StringBuilder();

        int pivot = 0;
        int size = uri.length();
        while (pivot < size) {
            int idx = uri.indexOf(prefixToken, pivot);
            if (idx < 0) {
                sb.append(createConstantPart(uri, pivot, size));
                break;
            } else {
                if (pivot < idx) {
                    sb.append(createConstantPart(uri, pivot, idx));
                }
                pivot = idx + prefixToken.length();
                int endIdx = uri.indexOf(suffixToken, pivot);
                if (endIdx < 0) {
                    throw new IllegalArgumentException("Expecting " + suffixToken + " but found end of string for uri: " + uri);
                }
                String key = uri.substring(pivot, endIdx);

                String part = createPlaceholderPart(key, properties, replaced);
                if (part == null) {
                    throw new IllegalArgumentException("Property with key [" + key + "] not found in properties for uri: " + uri);
                }
                sb.append(part);
                pivot = endIdx + suffixToken.length();
            }
        }
        return sb.toString();
    }

    private static String createConstantPart(String uri, int start, int end) {
        return uri.substring(start, end);
    }

    private static String createPlaceholderPart(String placeholderPart, Properties properties, List<String> replaced) {
        String propertyValue;
        
        replaced.add(placeholderPart);
        
        propertyValue = System.getProperty(placeholderPart);
        if (propertyValue != null) {
            LOG.info("Found a JVM system property: " + placeholderPart + ". Overriding property set via Property Location");
        } else {
            propertyValue = properties.getProperty(placeholderPart);
        }
        
        return propertyValue;
    }

}
