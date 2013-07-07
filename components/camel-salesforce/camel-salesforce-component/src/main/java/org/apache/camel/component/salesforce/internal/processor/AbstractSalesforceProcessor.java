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
package org.apache.camel.component.salesforce.internal.processor;

import java.util.Map;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.component.salesforce.SalesforceComponent;
import org.apache.camel.component.salesforce.SalesforceEndpoint;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.internal.OperationName;
import org.apache.camel.component.salesforce.internal.SalesforceSession;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSalesforceProcessor implements SalesforceProcessor {

    protected static final boolean NOT_OPTIONAL = false;
    protected static final boolean IS_OPTIONAL = true;
    protected static final boolean USE_BODY = true;
    protected static final boolean IGNORE_BODY = false;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final SalesforceEndpoint endpoint;
    protected final Map<String, String> endpointConfigMap;

    protected final OperationName operationName;
    protected final SalesforceSession session;
    protected final HttpClient httpClient;

    public AbstractSalesforceProcessor(SalesforceEndpoint endpoint) {
        this.endpoint = endpoint;
        this.operationName = endpoint.getOperationName();
        this.endpointConfigMap = endpoint.getConfiguration().toValueMap();

        final SalesforceComponent component = endpoint.getComponent();
        this.session = component.getSession();
        this.httpClient = endpoint.getConfiguration().getHttpClient();
    }

    @Override
    public abstract boolean process(Exchange exchange, AsyncCallback callback);

    /**
     * Gets value for a parameter from header, endpoint config, or exchange body (optional).
     *
     * @param exchange      exchange to inspect
     * @param convertInBody converts In body to String value if true
     * @param propName      name of property
     * @param optional      if {@code true} returns null, otherwise throws RestException
     * @return value of property, or {@code null} for optional parameters if not found.
     * @throws org.apache.camel.component.salesforce.api.SalesforceException
     *          if the property can't be found.
     */
    protected final String getParameter(String propName, Exchange exchange, boolean convertInBody, boolean optional) throws SalesforceException {
        String propValue = exchange.getIn().getHeader(propName, String.class);
        propValue = propValue == null ? endpointConfigMap.get(propName) : propValue;
        propValue = (propValue == null && convertInBody) ? exchange.getIn().getBody(String.class) : propValue;

        // error if property was not set
        if (propValue == null && !optional) {
            String msg = "Missing property " + propName;
            throw new SalesforceException(msg, null);
        }

        return propValue;
    }

}
