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
package org.apache.camel.blueprint;

import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesResolver;
import org.apache.camel.util.ObjectHelper;

/**
 * A {@link PropertiesResolver} which supports the <tt>blueprint</tt> scheme.
 * <p/>
 * This implementation will sit on top of any existing configured
 * {@link org.apache.camel.component.properties.PropertiesResolver} and will delegate
 * to any non <tt>blueprint</tt> schemes.
 */
public class BlueprintPropertiesResolver implements PropertiesResolver {

    private final PropertiesResolver delegate;
    private final BlueprintPropertiesParser blueprint;

    public BlueprintPropertiesResolver(PropertiesResolver delegate, BlueprintPropertiesParser blueprint) {
        this.delegate = delegate;
        this.blueprint = blueprint;
    }

    @Override
    public Properties resolveProperties(CamelContext context, boolean ignoreMissingLocation, String... urls) throws Exception {
        Properties answer = new Properties();

        boolean explicit = false;

        for (String url : urls) {
            if (url.startsWith("blueprint:")) {
                String ref = ObjectHelper.after(url, "blueprint:");
                blueprint.addPropertyPlaceholder(ref);
                // indicate an explicit blueprint id was configured
                explicit = true;
            } else {
                // delegate the url
                answer.putAll(delegate.resolveProperties(context, ignoreMissingLocation, url));
            }
        }

        if (!explicit) {
            // auto lookup blueprint property placeholders to use if none explicit was configured
            // this is convention over configuration
            String[] ids = blueprint.lookupPropertyPlaceholderIds();
            for (String id : ids) {
                blueprint.addPropertyPlaceholder(id);
            }
        }

        return answer;
    }

}
