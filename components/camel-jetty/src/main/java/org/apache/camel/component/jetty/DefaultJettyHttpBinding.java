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
package org.apache.camel.component.jetty;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http.HttpHeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
public class DefaultJettyHttpBinding implements JettyHttpBinding {

    private static final transient Log LOG = LogFactory.getLog(DefaultJettyHttpBinding.class);
    private HeaderFilterStrategy headerFilterStrategy = new HttpHeaderFilterStrategy();
    private boolean throwExceptionOnFailure;

    public void populateResponse(Exchange exchange, JettyContentExchange httpExchange) throws Exception {
        int responseCode = httpExchange.getResponseStatus();

        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP responseCode: " + responseCode);
        }

        Message in = exchange.getIn();
        if (!isThrowExceptionOnFailure()) {
            // if we do not use failed exception then populate response for all response codes
            populateResponse(exchange, httpExchange, exchange.getIn(), getHeaderFilterStrategy(), responseCode);
        } else {
            if (responseCode >= 100 && responseCode < 300) {
                // only populate response for OK response
                populateResponse(exchange, httpExchange, in, getHeaderFilterStrategy(), responseCode);
            } else {
                // operation failed so populate exception to throw
                throw populateHttpOperationFailedException(exchange, httpExchange, responseCode);
            }
        }
    }

    public HeaderFilterStrategy getHeaderFilterStrategy() {
        return headerFilterStrategy;
    }

    public void setHeaderFilterStrategy(HeaderFilterStrategy headerFilterStrategy) {
        this.headerFilterStrategy = headerFilterStrategy;
    }

    public boolean isThrowExceptionOnFailure() {
        return throwExceptionOnFailure;
    }

    public void setThrowExceptionOnFailure(boolean throwExceptionOnFailure) {
        this.throwExceptionOnFailure = throwExceptionOnFailure;
    }

    protected void populateResponse(Exchange exchange, JettyContentExchange httpExchange,
                                    Message in, HeaderFilterStrategy strategy, int responseCode) throws IOException {
        Message answer = exchange.getOut();

        answer.setHeaders(in.getHeaders());
        answer.setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
        answer.setBody(httpExchange.getBody());

        // propagate HTTP response headers
        for (Map.Entry<String, Object> entry : httpExchange.getHeaders().entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (name.toLowerCase().equals("content-type")) {
                name = Exchange.CONTENT_TYPE;
            }
            if (strategy != null && !strategy.applyFilterToExternalHeaders(name, value, exchange)) {
                answer.setHeader(name, value);
            }
        }
    }

    protected JettyHttpOperationFailedException populateHttpOperationFailedException(Exchange exchange, JettyContentExchange httpExchange,
                                                                                     int responseCode) throws IOException {
        JettyHttpOperationFailedException exception;
        String uri = httpExchange.getUrl();
        Map<String, Object> headers = httpExchange.getHeaders();
        String body = httpExchange.getBody();

        if (responseCode >= 300 && responseCode < 400) {
            String locationHeader = httpExchange.getResponseFields().getStringField("location");
            if (locationHeader != null) {
                exception = new JettyHttpOperationFailedException(uri, responseCode, locationHeader, headers, body);
            } else {
                // no redirect location
                exception = new JettyHttpOperationFailedException(uri, responseCode, headers, body);
            }
        } else {
            // internal server error (error code 500)
            exception = new JettyHttpOperationFailedException(uri, responseCode, headers, body);
        }

        return exception;
    }


}
