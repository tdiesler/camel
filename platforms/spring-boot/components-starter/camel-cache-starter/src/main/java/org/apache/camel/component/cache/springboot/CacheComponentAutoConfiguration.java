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
package org.apache.camel.component.cache.springboot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.camel.CamelContext;
import org.apache.camel.component.cache.CacheComponent;
import org.apache.camel.spi.ComponentCustomizer;
import org.apache.camel.spi.HasId;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.ComponentConfigurationProperties;
import org.apache.camel.spring.boot.util.CamelPropertiesHelper;
import org.apache.camel.spring.boot.util.ConditionalOnCamelContextAndAutoConfigurationBeans;
import org.apache.camel.spring.boot.util.GroupCondition;
import org.apache.camel.spring.boot.util.HierarchicalPropertiesEvaluator;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@Configuration
@Conditional({ConditionalOnCamelContextAndAutoConfigurationBeans.class,
        CacheComponentAutoConfiguration.GroupConditions.class})
@AutoConfigureAfter(CamelAutoConfiguration.class)
@EnableConfigurationProperties({ComponentConfigurationProperties.class,
        CacheComponentConfiguration.class})
public class CacheComponentAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CacheComponentAutoConfiguration.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private CacheComponentConfiguration configuration;
    @Autowired(required = false)
    private List<ComponentCustomizer<CacheComponent>> customizers;

    static class GroupConditions extends GroupCondition {
        public GroupConditions() {
            super("camel.component", "camel.component.cache");
        }
    }

    @Lazy
    @Bean(name = "cache-component")
    @ConditionalOnMissingBean(CacheComponent.class)
    public CacheComponent configureCacheComponent() throws Exception {
        CacheComponent component = new CacheComponent();
        component.setCamelContext(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        IntrospectionSupport.getProperties(configuration, parameters, null,
                false);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            Class<?> paramClass = value.getClass();
            if (paramClass.getName().endsWith("NestedConfiguration")) {
                Class nestedClass = null;
                try {
                    nestedClass = (Class) paramClass.getDeclaredField(
                            "CAMEL_NESTED_CLASS").get(null);
                    HashMap<String, Object> nestedParameters = new HashMap<>();
                    IntrospectionSupport.getProperties(value, nestedParameters,
                            null, false);
                    Object nestedProperty = nestedClass.newInstance();
                    CamelPropertiesHelper.setCamelProperties(camelContext,
                            nestedProperty, nestedParameters, false);
                    entry.setValue(nestedProperty);
                } catch (NoSuchFieldException e) {
                }
            }
        }
        CamelPropertiesHelper.setCamelProperties(camelContext, component,
                parameters, false);
        if (ObjectHelper.isNotEmpty(customizers)) {
            for (ComponentCustomizer<CacheComponent> customizer : customizers) {
                boolean useCustomizer = (customizer instanceof HasId)
                        ? HierarchicalPropertiesEvaluator.evaluate(
                                applicationContext.getEnvironment(),
                                "camel.component.customizer",
                                "camel.component.cache.customizer",
                                ((HasId) customizer).getId())
                        : HierarchicalPropertiesEvaluator.evaluate(
                                applicationContext.getEnvironment(),
                                "camel.component.customizer",
                                "camel.component.cache.customizer");
                if (useCustomizer) {
                    LOGGER.debug("Configure component {}, with customizer {}",
                            component, customizer);
                    customizer.customize(component);
                }
            }
        }
        return component;
    }
}