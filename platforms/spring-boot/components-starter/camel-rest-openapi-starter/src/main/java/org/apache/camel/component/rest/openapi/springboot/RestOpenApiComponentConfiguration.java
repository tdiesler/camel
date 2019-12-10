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
package org.apache.camel.component.rest.openapi.springboot;

import java.net.URI;
import javax.annotation.Generated;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * An awesome REST endpoint backed by OpenApi specifications.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.rest-openapi")
public class RestOpenApiComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * API basePath, for example /v2. Default is unset, if set overrides the
     * value present in OpenApi specification.
     */
    private String basePath;
    /**
     * Name of the Camel component that will perform the requests. The component
     * must be present in Camel registry and it must implement
     * RestProducerFactory service provider interface. If not set CLASSPATH is
     * searched for single component that implements RestProducerFactory SPI.
     * Can be overridden in endpoint configuration.
     */
    private String componentName;
    /**
     * What payload type this component capable of consuming. Could be one type,
     * like application/json or multiple types as application/json,
     * application/xml; q=0.5 according to the RFC7231. This equates to the
     * value of Accept HTTP header. If set overrides any value found in the
     * OpenApi specification. Can be overridden in endpoint configuration
     */
    private String consumes;
    /**
     * Scheme hostname and port to direct the HTTP requests to in the form of
     * https://hostname:port. Can be configured at the endpoint, component or in
     * the corresponding REST configuration in the Camel Context. If you give
     * this component a name (e.g. petstore) that REST configuration is
     * consulted first, rest-openapi next, and global configuration last. If set
     * overrides any value found in the OpenApi specification,
     * RestConfiguration. Can be overridden in endpoint configuration.
     */
    private String host;
    /**
     * What payload type this component is producing. For example
     * application/json according to the RFC7231. This equates to the value of
     * Content-Type HTTP header. If set overrides any value present in the
     * OpenApi specification. Can be overridden in endpoint configuration.
     */
    private String produces;
    /**
     * Path to the OpenApi specification file. The scheme, host base path are
     * taken from this specification, but these can be overridden with
     * properties on the component or endpoint level. If not given the component
     * tries to load openapi.json resource. Note that the host defined on the
     * component and endpoint of this Component should contain the scheme,
     * hostname and optionally the port in the URI syntax (i.e.
     * https://api.example.com:8080). Can be overridden in endpoint
     * configuration.
     */
    private URI specificationUri;
    /**
     * Customize TLS parameters used by the component. If not set defaults to
     * the TLS parameters set in the Camel context
     */
    @NestedConfigurationProperty
    private SSLContextParameters sslContextParameters;
    /**
     * Enable usage of global SSL context parameters.
     */
    private Boolean useGlobalSslContextParameters = false;
    /**
     * Whether the component should resolve property placeholders on itself when
     * starting. Only properties which are of String type can use property
     * placeholders.
     */
    private Boolean resolvePropertyPlaceholders = true;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(String consumes) {
        this.consumes = consumes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public URI getSpecificationUri() {
        return specificationUri;
    }

    public void setSpecificationUri(URI specificationUri) {
        this.specificationUri = specificationUri;
    }

    public SSLContextParameters getSslContextParameters() {
        return sslContextParameters;
    }

    public void setSslContextParameters(
            SSLContextParameters sslContextParameters) {
        this.sslContextParameters = sslContextParameters;
    }

    public Boolean getUseGlobalSslContextParameters() {
        return useGlobalSslContextParameters;
    }

    public void setUseGlobalSslContextParameters(
            Boolean useGlobalSslContextParameters) {
        this.useGlobalSslContextParameters = useGlobalSslContextParameters;
    }

    public Boolean getResolvePropertyPlaceholders() {
        return resolvePropertyPlaceholders;
    }

    public void setResolvePropertyPlaceholders(
            Boolean resolvePropertyPlaceholders) {
        this.resolvePropertyPlaceholders = resolvePropertyPlaceholders;
    }
}