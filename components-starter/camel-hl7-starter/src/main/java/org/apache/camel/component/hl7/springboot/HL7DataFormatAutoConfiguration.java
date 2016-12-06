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
package org.apache.camel.component.hl7.springboot;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.component.hl7.HL7DataFormat;
import org.apache.camel.util.IntrospectionSupport;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Configuration
@EnableConfigurationProperties(HL7DataFormatConfiguration.class)
@Conditional(HL7DataFormatAutoConfiguration.Condition.class)
public class HL7DataFormatAutoConfiguration {

    @Bean(name = "hl7-dataformat")
    @Scope("prototype")
    @ConditionalOnClass(CamelContext.class)
    @ConditionalOnMissingBean(HL7DataFormat.class)
    public HL7DataFormat configureHL7DataFormat(CamelContext camelContext,
            HL7DataFormatConfiguration configuration) throws Exception {
        HL7DataFormat dataformat = new HL7DataFormat();
        if (dataformat instanceof CamelContextAware) {
            ((CamelContextAware) dataformat).setCamelContext(camelContext);
        }
        Map<String, Object> parameters = new HashMap<>();
        IntrospectionSupport.getProperties(configuration, parameters, null,
                false);
        IntrospectionSupport.setProperties(camelContext,
                camelContext.getTypeConverter(), dataformat, parameters);
        return dataformat;
    }

    public static class Condition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(
                ConditionContext conditionContext,
                AnnotatedTypeMetadata annotatedTypeMetadata) {
            boolean groupEnabled = isEnabled(conditionContext,
                    "camel.dataformat.", true);
            ConditionMessage.Builder message = ConditionMessage
                    .forCondition("camel.dataformat.hl7");
            if (isEnabled(conditionContext, "camel.dataformat.hl7.",
                    groupEnabled)) {
                return ConditionOutcome.match(message.because("enabled"));
            }
            return ConditionOutcome.noMatch(message.because("not enabled"));
        }

        private boolean isEnabled(
                org.springframework.context.annotation.ConditionContext context,
                java.lang.String prefix, boolean defaultValue) {
            RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
                    context.getEnvironment(), prefix);
            return resolver.getProperty("enabled", Boolean.class, defaultValue);
        }
    }
}