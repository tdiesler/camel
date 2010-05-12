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
package org.apache.camel.component.http;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
public class CamelServlet extends HttpServlet {

    private static final long serialVersionUID = -7061982839117697829L;
    private static final transient Log LOG = LogFactory.getLog(CamelServlet.class);

    private ConcurrentHashMap<String, HttpConsumer> consumers = new ConcurrentHashMap<String, HttpConsumer>();
   
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            // Is there a consumer registered for the request.
            HttpConsumer consumer = resolve(request);
            if (consumer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // are we suspended?
            if (consumer.isSuspended()) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            // create exchange and set data on it
            Exchange exchange = new DefaultExchange(consumer.getEndpoint(), ExchangePattern.InOut);
            if ((consumer.getEndpoint()).isBridgeEndpoint()) {
                exchange.setProperty(Exchange.SKIP_GZIP_ENCODING, Boolean.TRUE);
            }
            exchange.setIn(new HttpMessage(exchange, request, response));

            // Have the camel process the HTTP exchange.
            consumer.getProcessor().process(exchange);

            // now lets output to the response
            consumer.getBinding().writeResponse(exchange, response);

        } catch (Exception e) {
            LOG.error("Error processing request", e);
            throw new ServletException(e);
        }
    }

    protected HttpConsumer resolve(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null) {
            return null;
        }
        HttpConsumer answer = consumers.get(path);
               
        if (answer == null) {
            for (String key : consumers.keySet()) {
                if (consumers.get(key).getEndpoint().isMatchOnUriPrefix() && path.startsWith(key)) {
                    answer = consumers.get(key);
                    break;
                }
            }
        }
        return answer;
    }

    public void connect(HttpConsumer consumer) {
        consumers.put(consumer.getPath(), consumer);
    }

    public void disconnect(HttpConsumer consumer) {
        consumers.remove(consumer.getPath());
    }
    
}
