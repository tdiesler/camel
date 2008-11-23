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
package org.apache.camel.artix.ds;

import biz.c24.io.api.data.ComplexDataObject;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;

/**
 * A Camel {@link ValidationException} thrown if validation fails using
 * <a href="http://activemq.apache.org/camel/artix-data-services.html">Artix Data Services</a>.
 *
 * @version $Revision$
 */
public class ArtixDSValidationException extends ValidationException {
    private final ComplexDataObject dataObject;
    private final biz.c24.io.api.data.ValidationException validationException;

    public ArtixDSValidationException(Exchange exchange, ComplexDataObject dataObject, biz.c24.io.api.data.ValidationException validationException) {
        super(validationException.toString(), exchange, validationException);
        this.dataObject = dataObject;
        this.validationException = validationException;
    }

    public ComplexDataObject getDataObject() {
        return dataObject;
    }

    /**
     * The underlying <a href="http://activemq.apache.org/camel/artix-data-services.html">Artix Data Services</a> exception
     *
     * @return
     */
    public biz.c24.io.api.data.ValidationException getValidationException() {
        return validationException;
    }
}
