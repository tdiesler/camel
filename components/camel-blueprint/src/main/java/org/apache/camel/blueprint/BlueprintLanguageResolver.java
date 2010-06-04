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

import org.apache.camel.CamelContext;
import org.apache.camel.core.osgi.OsgiLanguageResolver;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.LanguageResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

public class BlueprintLanguageResolver extends OsgiLanguageResolver {

    private static final transient Log LOG = LogFactory.getLog(BlueprintLanguageResolver.class);

    public BlueprintLanguageResolver(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Language resolveLanguage(String name, CamelContext context) {
        try {
            Object bean = context.getRegistry().lookup(".camelBlueprint.languageResolver." + name);
            if (bean instanceof LanguageResolver) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found language resolver: " + name + " in registry: " + bean);
                }
                return ((LanguageResolver) bean).resolveLanguage(name, context);
            }
        } catch (Exception e) {
            LOG.debug("Ignored error looking up bean: " + name + ". Error: " + e);
        }
        return super.resolveLanguage(name, context);
    }

}
