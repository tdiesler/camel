/*
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
package org.apache.camel.builder.component.dsl;

import javax.annotation.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.bean.validator.BeanValidatorComponent;

/**
 * Validate the message body using the Java Bean Validation API.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface BeanValidatorComponentBuilderFactory {

    /**
     * Bean Validator (camel-bean-validator)
     * Validate the message body using the Java Bean Validation API.
     * 
     * Category: validation
     * Since: 2.3
     * Maven coordinates: org.apache.camel:camel-bean-validator
     * 
     * @return the dsl builder
     */
    static BeanValidatorComponentBuilder beanValidator() {
        return new BeanValidatorComponentBuilderImpl();
    }

    /**
     * Builder for the Bean Validator component.
     */
    interface BeanValidatorComponentBuilder
            extends
                ComponentBuilder<BeanValidatorComponent> {
        /**
         * Whether to ignore data from the META-INF/validation.xml file.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param ignoreXmlConfiguration the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder ignoreXmlConfiguration(
                boolean ignoreXmlConfiguration) {
            doSetProperty("ignoreXmlConfiguration", ignoreXmlConfiguration);
            return this;
        }
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder lazyStartProducer(
                boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder autowiredEnabled(
                boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * To use a custom ConstraintValidatorFactory.
         * 
         * The option is a:
         * &lt;code&gt;javax.validation.ConstraintValidatorFactory&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param constraintValidatorFactory the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder constraintValidatorFactory(
                javax.validation.ConstraintValidatorFactory constraintValidatorFactory) {
            doSetProperty("constraintValidatorFactory", constraintValidatorFactory);
            return this;
        }
        /**
         * To use a custom MessageInterpolator.
         * 
         * The option is a:
         * &lt;code&gt;javax.validation.MessageInterpolator&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param messageInterpolator the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder messageInterpolator(
                javax.validation.MessageInterpolator messageInterpolator) {
            doSetProperty("messageInterpolator", messageInterpolator);
            return this;
        }
        /**
         * To use a custom TraversableResolver.
         * 
         * The option is a:
         * &lt;code&gt;javax.validation.TraversableResolver&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param traversableResolver the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder traversableResolver(
                javax.validation.TraversableResolver traversableResolver) {
            doSetProperty("traversableResolver", traversableResolver);
            return this;
        }
        /**
         * To use a a custom ValidationProviderResolver.
         * 
         * The option is a:
         * &lt;code&gt;javax.validation.ValidationProviderResolver&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param validationProviderResolver the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder validationProviderResolver(
                javax.validation.ValidationProviderResolver validationProviderResolver) {
            doSetProperty("validationProviderResolver", validationProviderResolver);
            return this;
        }
        /**
         * To use a custom ValidatorFactory.
         * 
         * The option is a:
         * &lt;code&gt;javax.validation.ValidatorFactory&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param validatorFactory the value to set
         * @return the dsl builder
         */
        default BeanValidatorComponentBuilder validatorFactory(
                javax.validation.ValidatorFactory validatorFactory) {
            doSetProperty("validatorFactory", validatorFactory);
            return this;
        }
    }

    class BeanValidatorComponentBuilderImpl
            extends
                AbstractComponentBuilder<BeanValidatorComponent>
            implements
                BeanValidatorComponentBuilder {
        @Override
        protected BeanValidatorComponent buildConcreteComponent() {
            return new BeanValidatorComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "ignoreXmlConfiguration": ((BeanValidatorComponent) component).setIgnoreXmlConfiguration((boolean) value); return true;
            case "lazyStartProducer": ((BeanValidatorComponent) component).setLazyStartProducer((boolean) value); return true;
            case "autowiredEnabled": ((BeanValidatorComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "constraintValidatorFactory": ((BeanValidatorComponent) component).setConstraintValidatorFactory((javax.validation.ConstraintValidatorFactory) value); return true;
            case "messageInterpolator": ((BeanValidatorComponent) component).setMessageInterpolator((javax.validation.MessageInterpolator) value); return true;
            case "traversableResolver": ((BeanValidatorComponent) component).setTraversableResolver((javax.validation.TraversableResolver) value); return true;
            case "validationProviderResolver": ((BeanValidatorComponent) component).setValidationProviderResolver((javax.validation.ValidationProviderResolver) value); return true;
            case "validatorFactory": ((BeanValidatorComponent) component).setValidatorFactory((javax.validation.ValidatorFactory) value); return true;
            default: return false;
            }
        }
    }
}