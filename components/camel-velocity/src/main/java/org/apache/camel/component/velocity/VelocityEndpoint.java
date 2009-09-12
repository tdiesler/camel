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
package org.apache.camel.component.velocity;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceBasedEndpoint;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.springframework.core.io.Resource;

/**
 * @version $Revision$
 */
public class VelocityEndpoint extends ResourceBasedEndpoint {
    private VelocityEngine velocityEngine;
    private boolean loaderCache = true;
    private String encoding;

    public VelocityEndpoint() {
    }

    public VelocityEndpoint(String uri, VelocityComponent component, String resourceUri) {
        super(uri, component, resourceUri, null);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public ExchangePattern getExchangePattern() {
        return ExchangePattern.InOut;
    }

    @Override
    protected String createEndpointUri() {
        return "velocity:" + getResourceUri();
    }

    private synchronized VelocityEngine getVelocityEngine() throws Exception {
        if (velocityEngine == null) {
            velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, isLoaderCache() ? Boolean.TRUE : Boolean.FALSE);
            velocityEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, CommonsLogLogChute.class.getName());
            velocityEngine.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME, VelocityEndpoint.class.getName());
            velocityEngine.init();
        }
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;

    }

    public boolean isLoaderCache() {
        return loaderCache;
    }

    /**
     * Enables / disables the velocity resource loader cache which is enabled by default
     *
     * @param loaderCache a flag to enable/disable the cache
     */
    public void setLoaderCache(boolean loaderCache) {
        this.loaderCache = loaderCache;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public VelocityEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(getResourceUri(), newResourceUri);
        if (log.isDebugEnabled()) {
            log.debug("Getting endpoint with URI: " + newUri);
        }
        return (VelocityEndpoint) getCamelContext().getEndpoint(newUri);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        String path = getResourceUri();
        ObjectHelper.notNull(path, "resourceUri");

        String newResourceUri = exchange.getIn().getHeader(VelocityConstants.VELOCITY_RESOURCE_URI, String.class);
        if (newResourceUri != null) {
            exchange.getIn().removeHeader(VelocityConstants.VELOCITY_RESOURCE_URI);

            if (log.isDebugEnabled()) {
                log.debug(VelocityConstants.VELOCITY_RESOURCE_URI + " set to " + newResourceUri + " creating new endpoint to handle exchange");
            }
            VelocityEndpoint newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri);
            newEndpoint.onExchange(exchange);
            return;
        }

        Resource resource = getResource();
        ObjectHelper.notNull(resource, "resource");

        if (log.isDebugEnabled()) {
            log.debug("Using resource: " + resource + " with resourceUri: " + path + " for endpoint " + getEndpointUri());
        }

        // getResourceAsInputStream also considers the content cache
        Reader reader = encoding != null ? new InputStreamReader(getResourceAsInputStream(), encoding) : new InputStreamReader(getResourceAsInputStream());
        StringWriter buffer = new StringWriter();
        String logTag = getClass().getName();
        Map variableMap = ExchangeHelper.createVariableMap(exchange);
        Context velocityContext = new VelocityContext(variableMap);

        // let velocity parse and generate the result in buffer
        VelocityEngine engine = getVelocityEngine();
        if (log.isDebugEnabled()) {
            log.debug("Velocity is evaluating using velocity context: " + variableMap);
        }
        engine.evaluate(velocityContext, buffer, logTag, reader);

        // now lets output the results to the exchange
        Message out = exchange.getOut();
        out.setBody(buffer.toString());
        out.setHeader(VelocityConstants.VELOCITY_RESOURCE, resource);
        out.setHeader(VelocityConstants.VELOCITY_RESOURCE_URI, path);
        Map<String, Object> headers = (Map<String, Object>) velocityContext.get("headers");
        for (String key : headers.keySet()) {
            out.setHeader(key, headers.get(key));
        }
    }

}
