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
package org.apache.camel.component.mustache;

import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link MustacheEndpoint}.
 * URI pattern: {@code mustache://template_name.mustache}
 * Supports parameters:
 * <ul>
 * <li>encoding: default platform one </li>
 * <li>startDelimiter: default "{{" </li>
 * <li>endDelimiter: default "}}" </li>
 * </li>
 */
public class MustacheComponent extends UriEndpointComponent {

    @Metadata(defaultValue = "false")
    private boolean allowTemplateFromHeader;
    @Metadata(defaultValue = "false")
    private boolean allowContextMapAll;
    private MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public MustacheComponent() {
        super(MustacheEndpoint.class);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        MustacheEndpoint endpoint = new MustacheEndpoint(uri, this, remaining);
        endpoint.setMustacheFactory(getMustacheFactory());
        endpoint.setAllowTemplateFromHeader(allowTemplateFromHeader);
        endpoint.setAllowContextMapAll(allowContextMapAll);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    public MustacheFactory getMustacheFactory() {
        return mustacheFactory;
    }

    /**
     * To use a custom {@link MustacheFactory}
     */
    public void setMustacheFactory(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
    }

    public boolean isAllowTemplateFromHeader() {
        return allowTemplateFromHeader;
    }

    /**
     * Whether to allow to use resource template from header or not (default false).
     *
     * Enabling this allows to specify dynamic templates via message header. However this can
     * be seen as a potential security vulnerability if the header is coming from a malicious user, so use this with care.
     */
    public void setAllowTemplateFromHeader(boolean allowTemplateFromHeader) {
        this.allowTemplateFromHeader = allowTemplateFromHeader;
    }

    public boolean isAllowContextMapAll() {
        return allowContextMapAll;
    }

    /**
     * Sets whether the context map should allow access to all details.
     * By default only the message body and headers can be accessed.
     * This option can be enabled for full access to the current Exchange and CamelContext.
     * Doing so impose a potential security risk as this opens access to the full power of CamelContext API.
     */
    public void setAllowContextMapAll(boolean allowContextMapAll) {
        this.allowContextMapAll = allowContextMapAll;
    }


}
