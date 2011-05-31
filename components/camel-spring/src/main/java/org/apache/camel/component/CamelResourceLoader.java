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
package org.apache.camel.component;

import org.apache.camel.CamelContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * A Camel specific {@link org.springframework.core.io.ResourceLoader} which can load
 * resources from classpath using the Camel {@link org.apache.camel.spi.ClassResolver}.
 */
public class CamelResourceLoader extends DefaultResourceLoader {

    private final CamelContext camelContext;

    public CamelResourceLoader(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new CamelClassPathResource(camelContext.getClassResolver(), location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
        }
        return super.getResource(location);
    }

    @Override
    protected Resource getResourceByPath(String path) {
        return new CamelClassPathResource(camelContext.getClassResolver(), path, getClassLoader());
    }

}
