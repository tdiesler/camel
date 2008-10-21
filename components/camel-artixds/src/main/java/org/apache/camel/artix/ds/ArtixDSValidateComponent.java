/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.artix.ds;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;
import biz.c24.io.api.data.ValidationManager;
import biz.c24.io.api.data.ValidationConstraints;

/**
 * @version $Revision$
 */
public class ArtixDSValidateComponent extends DefaultComponent {
    protected Endpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {


        ArtixDSValidator validator = new ArtixDSValidator();
        Object bean = getCamelContext().getRegistry().lookup(remaining);
        if (bean instanceof ValidationManager) {
            ValidationManager validationManager = (ValidationManager) bean;
            validator.setValidationManager(validationManager);
        }
        else if (bean instanceof ValidationConstraints) {
            ValidationConstraints validationConstraints = (ValidationConstraints) bean;
            ValidationManager validationManager = new ValidationManager(validationConstraints);
            validator.setValidationManager(validationManager);
        }
        else if (bean != null) {
            throw new IllegalArgumentException("Bean " + remaining + " not an instance of ValidationManager or ValidationContraints");
        }
        return new ProcessorEndpoint(uri, this, validator);
    }
}
