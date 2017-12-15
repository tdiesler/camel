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
package org.apache.camel.component.undertow.springboot;

import javax.annotation.Generated;
import org.apache.camel.component.undertow.UndertowHttpBinding;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The undertow component provides HTTP-based endpoints for consuming and
 * producing HTTP requests.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.undertow")
public class UndertowComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * To use a custom HttpBinding to control the mapping between Camel message
     * and HttpClient.
     */
    @NestedConfigurationProperty
    private UndertowHttpBinding undertowHttpBinding;
    /**
     * To configure security using SSLContextParameters
     */
    @NestedConfigurationProperty
    private SSLContextParameters sslContextParameters;
    /**
     * Enable usage of global SSL context parameters.
     */
    private Boolean useGlobalSslContextParameters = false;
    /**
     * To configure common options such as thread pools
     */
    private UndertowHostOptionsNestedConfiguration hostOptions;
    /**
     * Whether the component should resolve property placeholders on itself when
     * starting. Only properties which are of String type can use property
     * placeholders.
     */
    private Boolean resolvePropertyPlaceholders = true;

    public UndertowHttpBinding getUndertowHttpBinding() {
        return undertowHttpBinding;
    }

    public void setUndertowHttpBinding(UndertowHttpBinding undertowHttpBinding) {
        this.undertowHttpBinding = undertowHttpBinding;
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

    public UndertowHostOptionsNestedConfiguration getHostOptions() {
        return hostOptions;
    }

    public void setHostOptions(
            UndertowHostOptionsNestedConfiguration hostOptions) {
        this.hostOptions = hostOptions;
    }

    public Boolean getResolvePropertyPlaceholders() {
        return resolvePropertyPlaceholders;
    }

    public void setResolvePropertyPlaceholders(
            Boolean resolvePropertyPlaceholders) {
        this.resolvePropertyPlaceholders = resolvePropertyPlaceholders;
    }

    public static class UndertowHostOptionsNestedConfiguration {
        public static final Class CAMEL_NESTED_CLASS = org.apache.camel.component.undertow.UndertowHostOptions.class;
        /**
         * The number of worker threads to use in a Undertow host.
         */
        private Integer workerThreads;
        /**
         * The number of io threads to use in a Undertow host.
         */
        private Integer ioThreads;
        /**
         * The buffer size of the Undertow host.
         */
        private Integer bufferSize;
        /**
         * Set if the Undertow host should use direct buffers.
         */
        private Boolean directBuffers;
        /**
         * Set if the Undertow host should use http2 protocol.
         */
        private Boolean http2Enabled;

        public Integer getWorkerThreads() {
            return workerThreads;
        }

        public void setWorkerThreads(Integer workerThreads) {
            this.workerThreads = workerThreads;
        }

        public Integer getIoThreads() {
            return ioThreads;
        }

        public void setIoThreads(Integer ioThreads) {
            this.ioThreads = ioThreads;
        }

        public Integer getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(Integer bufferSize) {
            this.bufferSize = bufferSize;
        }

        public Boolean getDirectBuffers() {
            return directBuffers;
        }

        public void setDirectBuffers(Boolean directBuffers) {
            this.directBuffers = directBuffers;
        }

        public Boolean getHttp2Enabled() {
            return http2Enabled;
        }

        public void setHttp2Enabled(Boolean http2Enabled) {
            this.http2Enabled = http2Enabled;
        }
    }
}