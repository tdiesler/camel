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

import java.util.UUID;

/**
 * Generator for Globally unique Strings.
 */
public class UuidGenerator {

    private static UuidGenerator instance = new UuidGenerator();
    
    UuidGenerator() {
    }

    /**
     * Returns a UUID generator. The instance returned by this method makes use
     * of {@link java.util.UUID#randomUUID()} for generating UUIDs. Other
     * generation strategies are currently not supported (but maybe added in
     * future versions).
     * 
     * @return a UUID generator singleton.
     */
    public static UuidGenerator get() {
        return instance;
    }

    /**
     * Generates a UUID string representation.  
     * 
     * @return a UUID string.
     */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
}
