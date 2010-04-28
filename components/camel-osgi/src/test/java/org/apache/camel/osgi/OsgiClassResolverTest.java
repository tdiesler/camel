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

import java.io.InputStream;
import java.net.URL;

import org.apache.camel.spi.ClassResolver;
import org.junit.Test;

public class OsgiClassResolverTest extends CamelOsgiTestSupport {
    
    @Test
    public void testResolveClass() {
        ClassResolver classResolver = getClassResolver();
        Class routeBuilder = classResolver.resolveClass("org.apache.camel.osgi.test.MyRouteBuilder");
        assertNotNull("The class of routeBuilder should not be null.", routeBuilder);
    }
    
    @Test
    public void testResolverResource() {
        ClassResolver classResolver = getClassResolver();
        InputStream is = classResolver.loadResourceAsStream("META-INF/services/org/apache/camel/TypeConverter");
        assertNotNull("The InputStream should not be null.", is);
    }

}
