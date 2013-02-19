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
package org.apache.camel.processor.validation;

import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;

/**
 * An exception thrown if the XML header is not available on the inbound message
 *
 * @version 
 */
public class NoXmlHeaderValidationException extends ValidationException {
    private static final long serialVersionUID = 4502520681354358599L;

    public NoXmlHeaderValidationException(Exchange exchange, String header) {
        super(exchange, "XML header \"" + header + "\" could not be found on the input message");
    }

    public NoXmlHeaderValidationException(Exchange exchange, String header, Throwable cause) {
        super("XML header \"" + header + "\"  could not found on the input message", exchange, cause);
    }
}
