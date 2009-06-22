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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.component.http.CamelServlet;
import org.apache.camel.component.http.HttpConsumer;
import org.apache.camel.component.http.HttpMessage;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

/**
 * @version $Revision$
 */
public class CamelContinuationServlet extends CamelServlet {

    // TODO: should use the new Async API and allow end users to define if they want Jetty continuation support or not

    private static final long serialVersionUID = 1L;

    public CamelContinuationServlet(boolean matchOnUriPrefix) {
        super(matchOnUriPrefix);
    }
        
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Is there a consumer registered for the request.
            HttpConsumer consumer = resolve(request);
            if (consumer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            final Continuation continuation = ContinuationSupport.getContinuation(request, null);
            if (continuation.isNew()) {
                // Have the camel process the HTTP exchange.
                // final DefaultExchange exchange = new DefaultExchange(consumer.getEndpoint(), ExchangePattern.InOut);
                // exchange.setProperty(HttpConstants.SERVLET_REQUEST, request);
                // exchange.setProperty(HttpConstants.SERVLET_RESPONSE, response);
                // exchange.setIn(new HttpMessage(exchange, request));
                // boolean sync = consumer.getAsyncProcessor().process(exchange, new AsyncCallback() {
                //     public void done(boolean sync) {
                //        if (sync) {
                //            return;
                //        }
                //        continuation.setObject(exchange);
                //        continuation.resume();
                //    }
                //});

                //if (!sync) {
                    // Wait for the exchange to get processed.
                    // This might block until it completes or it might return via an exception and
                    // then this method is re-invoked once the the exchange has finished processing
                //    continuation.suspend(0);
                //}

                // HC: The getBinding() is interesting because it illustrates the
                // impedance miss-match between HTTP's stream oriented protocol, and
                // Camels more message oriented protocol exchanges.

                // now lets output to the response
                //consumer.getBinding().writeResponse(exchange, response);
                return;
            }

            if (continuation.isResumed()) {
                Exchange exchange = (Exchange)continuation.getObject();
                // now lets output to the response
                consumer.getBinding().writeResponse(exchange, response);
                return;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
