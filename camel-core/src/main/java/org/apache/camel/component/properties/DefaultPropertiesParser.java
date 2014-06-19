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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * A parser to parse a string which contains property placeholders
 */
public class DefaultPropertiesParser implements AugmentedPropertyNameAwarePropertiesParser {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String parseUri(String text, Properties properties, String prefixToken, String suffixToken) throws IllegalArgumentException {
        return parseUri(text, properties, prefixToken, suffixToken, null, null, false);
    }

    public String parseUri(String text, Properties properties, String prefixToken, String suffixToken, String propertyPrefix, String propertySuffix,
                           boolean fallbackToUnaugmentedProperty) throws IllegalArgumentException {
        ParsingContext context = new ParsingContext(properties, prefixToken, suffixToken, propertyPrefix, propertySuffix, fallbackToUnaugmentedProperty);
        return context.parse(text);
    }

    public String parseProperty(String key, String value, Properties properties) {
        return value;
    }

    /**
     * This inner class helps replacing properties.
     */
    private final class ParsingContext {
        private final Properties properties;
        private final String prefixToken;
        private final String suffixToken;
        private final String propertyPrefix;
        private final String propertySuffix;
        private final boolean fallbackToUnaugmentedProperty;

        public ParsingContext(Properties properties, String prefixToken, String suffixToken, String propertyPrefix, String propertySuffix,
                              boolean fallbackToUnaugmentedProperty) {
            this.properties = properties;
            this.prefixToken = prefixToken;
            this.suffixToken = suffixToken;
            this.propertyPrefix = propertyPrefix;
            this.propertySuffix = propertySuffix;
            this.fallbackToUnaugmentedProperty = fallbackToUnaugmentedProperty;
        }

        /**
         * Parses the given input string and replaces all properties
         *
         * @param input Input string
         * @return Evaluated string
         */
        public String parse(String input) {
            return doParse(input, new HashSet<String>());
        }

        /**
         * Recursively parses the given input string and replaces all properties
         *
         * @param input                Input string
         * @param replacedPropertyKeys Already replaced property keys used for tracking circular references
         * @return Evaluated string
         */
        private String doParse(String input, Set<String> replacedPropertyKeys) {
            String answer = input;
            Property property;
            while ((property = readProperty(answer)) != null) {
                // Check for circular references
                if (replacedPropertyKeys.contains(property.getKey())) {
                    throw new IllegalArgumentException("Circular reference detected with key [" + property.getKey() + "] from text: " + input);
                }

                Set<String> newReplaced = new HashSet<String>(replacedPropertyKeys);
                newReplaced.add(property.getKey());

                String before = answer.substring(0, property.getBeginIndex());
                String after = answer.substring(property.getEndIndex());
                answer = before + doParse(property.getValue(), newReplaced) + after;
            }
            return answer;
        }

        /**
         * Finds a property in the given string. It returns {@code null} if there's no property defined.
         *
         * @param input Input string
         * @return A property in the given string or {@code null} if not found
         */
        private Property readProperty(String input) {
            // Find the index of the first valid suffix token
            int suffix = getSuffixIndex(input);

            // If not found, ensure that there is no valid prefix token in the string
            if (suffix == -1) {
                if (getMatchingPrefixIndex(input, input.length()) != -1) {
                    throw new IllegalArgumentException(format("Missing %s from the text: %s", suffixToken, input));
                }
                return null;
            }

            // Find the index of the prefix token that matches the suffix token
            int prefix = getMatchingPrefixIndex(input, suffix);
            if (prefix == -1) {
                throw new IllegalArgumentException(format("Missing %s from the text: %s", prefixToken, input));
            }

            String key = input.substring(prefix + prefixToken.length(), suffix);
            String value = getPropertyValue(key, input);
            return new Property(prefix, suffix + suffixToken.length(), key, value);
        }

        /**
         * Gets the first index of the suffix token that is not surrounded by quotes
         *
         * @param input Input string
         * @return First index of the suffix token that is not surrounded by quotes
         */
        private int getSuffixIndex(String input) {
            int index = -1;
            do {
                index = input.indexOf(suffixToken, index + 1);
            } while (index != -1 && isQuoted(input, index, suffixToken));
            return index;
        }

        /**
         * Gets the index of the prefix token that matches the suffix at the given index and that is not surrounded by quotes
         *
         * @param input       Input string
         * @param suffixIndex Index of the suffix token
         * @return Index of the prefix token that matches the suffix at the given index and that is not surrounded by quotes
         */
        private int getMatchingPrefixIndex(String input, int suffixIndex) {
            int index = suffixIndex;
            do {
                index = input.lastIndexOf(prefixToken, index - 1);
            } while (index != -1 && isQuoted(input, index, prefixToken));
            return index;
        }

        /**
         * Indicates whether or not the token at the given index is surrounded by single or double quotes
         *
         * @param input Input string
         * @param index Index of the token
         * @param token Token
         * @return {@code true}
         */
        private boolean isQuoted(String input, int index, String token) {
            int beforeIndex = index - 1;
            int afterIndex = index + token.length();
            if (beforeIndex >= 0 && afterIndex < input.length()) {
                char before = input.charAt(beforeIndex);
                char after = input.charAt(afterIndex);
                return (before == after) && (before == '\'' || before == '"');
            }
            return false;
        }

        /**
         * Gets the value of the property with given key
         *
         * @param key   Key of the property
         * @param input Input string (used for exception message if value not found)
         * @return Value of the property with the given key
         */
        private String getPropertyValue(String key, String input) {
            String augmentedKey = getAugmentedKey(key);
            boolean shouldFallback = fallbackToUnaugmentedProperty && !key.equals(augmentedKey);

            String value = doGetPropertyValue(augmentedKey);
            if (value == null && shouldFallback) {
                log.debug("Property with key [{}] not found, attempting with unaugmented key: {}", augmentedKey, key);
                value = doGetPropertyValue(key);
            }

            if (value == null) {
                StringBuilder esb = new StringBuilder();
                esb.append("Property with key [").append(augmentedKey).append("] ");
                if (shouldFallback) {
                    esb.append("(and original key [").append(key).append("]) ");
                }
                esb.append("not found in properties from text: ").append(input);
                throw new IllegalArgumentException(esb.toString());
            }

            return value;
        }

        /**
         * Gets the augmented key of the given base key
         *
         * @param key Base key
         * @return Augmented key
         */
        private String getAugmentedKey(String key) {
            String augmentedKey = key;
            if (propertyPrefix != null) {
                log.debug("Augmenting property key [{}] with prefix: {}", key, propertyPrefix);
                augmentedKey = propertyPrefix + augmentedKey;
            }
            if (propertySuffix != null) {
                log.debug("Augmenting property key [{}] with suffix: {}", key, propertySuffix);
                augmentedKey = augmentedKey + propertySuffix;
            }
            return augmentedKey;
        }

        /**
         * Gets the property with the given key, it returns {@code null} if the property is not found
         *
         * @param key Key of the property
         * @return Value of the property or {@code null} if not found
         */
        private String doGetPropertyValue(String key) {
            String value = System.getProperty(key);
            if (value != null) {
                log.debug("Found a JVM system property: {} with value: {} to be used.", key, value);
            } else if (properties != null) {
                value = properties.getProperty(key);
            }
            return parseProperty(key, value, properties);
        }
    }

    /**
     * This inner class is the definition of a property used in a string
     */
    private static final class Property {
        private final int beginIndex;
        private final int endIndex;
        private final String key;
        private final String value;

        private Property(int beginIndex, int endIndex, String key, String value) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the begin index of the property (including the prefix token).
         */
        public int getBeginIndex() {
            return beginIndex;
        }

        /**
         * Gets the end index of the property (including the suffix token).
         */
        public int getEndIndex() {
            return endIndex;
        }

        /**
         * Gets the key of the property.
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the value of the property.
         */
        public String getValue() {
            return value;
        }
    }
}
