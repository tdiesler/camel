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
package org.apache.camel.component.netty4.http.springboot;

import javax.annotation.Generated;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.netty4.http.SecurityAuthenticator;
import org.apache.camel.component.netty4.http.SecurityConstraint;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty HTTP server and client using the Netty 4.x library.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.netty4-http")
public class NettyHttpComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * Whether to enable auto configuration of the netty4-http component. This
     * is enabled by default.
     */
    private Boolean enabled;
    /**
     * To use a custom org.apache.camel.component.netty4.http.NettyHttpBinding
     * for binding to/from Netty and Camel Message API. The option is a
     * org.apache.camel.component.netty4.http.NettyHttpBinding type.
     */
    private String nettyHttpBinding;
    /**
     * To use the NettyConfiguration as configuration when creating endpoints.
     */
    private NettyHttpConfigurationNestedConfiguration configuration;
    /**
     * To use a custom org.apache.camel.spi.HeaderFilterStrategy to filter
     * headers. The option is a org.apache.camel.spi.HeaderFilterStrategy type.
     */
    private String headerFilterStrategy;
    /**
     * Refers to a
     * org.apache.camel.component.netty4.http.NettyHttpSecurityConfiguration for
     * configuring secure web resources.
     */
    private NettyHttpSecurityConfigurationNestedConfiguration securityConfiguration;
    /**
     * Enable usage of global SSL context parameters.
     */
    private Boolean useGlobalSslContextParameters = false;
    /**
     * The thread pool size for the EventExecutorGroup if its in use. The
     * default value is 16.
     */
    private Integer maximumPoolSize = 16;
    /**
     * To use the given EventExecutorGroup. The option is a
     * io.netty.util.concurrent.EventExecutorGroup type.
     */
    private String executorService;
    /**
     * To configure security using SSLContextParameters. The option is a
     * org.apache.camel.util.jsse.SSLContextParameters type.
     */
    private String sslContextParameters;
    /**
     * Whether the component should resolve property placeholders on itself when
     * starting. Only properties which are of String type can use property
     * placeholders.
     */
    private Boolean resolvePropertyPlaceholders = true;

    public String getNettyHttpBinding() {
        return nettyHttpBinding;
    }

    public void setNettyHttpBinding(String nettyHttpBinding) {
        this.nettyHttpBinding = nettyHttpBinding;
    }

    public NettyHttpConfigurationNestedConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(
            NettyHttpConfigurationNestedConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getHeaderFilterStrategy() {
        return headerFilterStrategy;
    }

    public void setHeaderFilterStrategy(String headerFilterStrategy) {
        this.headerFilterStrategy = headerFilterStrategy;
    }

    public NettyHttpSecurityConfigurationNestedConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(
            NettyHttpSecurityConfigurationNestedConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public Boolean getUseGlobalSslContextParameters() {
        return useGlobalSslContextParameters;
    }

    public void setUseGlobalSslContextParameters(
            Boolean useGlobalSslContextParameters) {
        this.useGlobalSslContextParameters = useGlobalSslContextParameters;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public String getExecutorService() {
        return executorService;
    }

    public void setExecutorService(String executorService) {
        this.executorService = executorService;
    }

    public String getSslContextParameters() {
        return sslContextParameters;
    }

    public void setSslContextParameters(String sslContextParameters) {
        this.sslContextParameters = sslContextParameters;
    }

    public Boolean getResolvePropertyPlaceholders() {
        return resolvePropertyPlaceholders;
    }

    public void setResolvePropertyPlaceholders(
            Boolean resolvePropertyPlaceholders) {
        this.resolvePropertyPlaceholders = resolvePropertyPlaceholders;
    }

    public static class NettyHttpConfigurationNestedConfiguration {
        public static final Class CAMEL_NESTED_CLASS = org.apache.camel.component.netty4.http.NettyHttpConfiguration.class;
        /**
         * The protocol to use which is either http, https or proxy - a consumer
         * only option.
         */
        private String protocol;
        /**
         * The local hostname such as localhost, or 0.0.0.0 when being a
         * consumer. The remote HTTP server hostname when using producer.
         */
        private String host;
        /**
         * The port number. Is default 80 for http and 443 for https.
         */
        private Integer port;
        /**
         * Allow using gzip/deflate for compression on the Netty HTTP server if
         * the client supports it from the HTTP headers.
         */
        private Boolean compression = false;
        /**
         * Option to disable throwing the HttpOperationFailedException in case
         * of failed responses from the remote server. This allows you to get
         * all responses regardless of the HTTP status code.
         */
        private Boolean throwExceptionOnFailure = true;
        /**
         * If enabled and an Exchange failed processing on the consumer side,
         * and if the caused Exception was send back serialized in the response
         * as a application/x-java-serialized-object content type. On the
         * producer side the exception will be deserialized and thrown as is,
         * instead of the HttpOperationFailedException. The caused exception is
         * required to be serialized. This is by default turned off. If you
         * enable this then be aware that Java will deserialize the incoming
         * data from the request to Java and that can be a potential security
         * risk.
         */
        private Boolean transferException = false;
        /**
         * If this option is enabled, then during binding from Netty to Camel
         * Message then the header values will be URL decoded (eg %20 will be a
         * space character. Notice this option is used by the default
         * org.apache.camel.component.netty.http.NettyHttpBinding and therefore
         * if you implement a custom
         * org.apache.camel.component.netty4.http.NettyHttpBinding then you
         * would need to decode the headers accordingly to this option.
         */
        private Boolean urlDecodeHeaders = false;
        /**
         * If this option is enabled, then during binding from Netty to Camel
         * Message then the headers will be mapped as well (eg added as header
         * to the Camel Message as well). You can turn off this option to
         * disable this. The headers can still be accessed from the
         * org.apache.camel.component.netty.http.NettyHttpMessage message with
         * the method getHttpRequest() that returns the Netty HTTP request
         * io.netty.handler.codec.http.HttpRequest instance.
         */
        private Boolean mapHeaders = true;
        /**
         * Whether or not Camel should try to find a target consumer by matching
         * the URI prefix if no exact match is found.
         */
        private Boolean matchOnUriPrefix = false;
        /**
         * If the option is true, the producer will ignore the Exchange.HTTP_URI
         * header, and use the endpoint's URI for request. You may also set the
         * throwExceptionOnFailure to be false to let the producer send all the
         * fault response back. The consumer working in the bridge mode will
         * skip the gzip compression and WWW URL form encoding (by adding the
         * Exchange.SKIP_GZIP_ENCODING and Exchange.SKIP_WWW_FORM_URLENCODED
         * headers to the consumed exchange).
         */
        private Boolean bridgeEndpoint = false;
        /**
         * Resource path
         */
        private String path;
        /**
         * Determines whether or not the raw input stream from Netty
         * HttpRequest#getContent() or HttpResponset#getContent() is cached or
         * not (Camel will read the stream into a in light-weight memory based
         * Stream caching) cache. By default Camel will cache the Netty input
         * stream to support reading it multiple times to ensure it Camel can
         * retrieve all data from the stream. However you can set this option to
         * true when you for example need to access the raw stream, such as
         * streaming it directly to a file or other persistent store. Mind that
         * if you enable this option, then you cannot read the Netty stream
         * multiple times out of the box, and you would need manually to reset
         * the reader index on the Netty raw stream. Also Netty will auto-close
         * the Netty stream when the Netty HTTP server/HTTP client is done
         * processing, which means that if the asynchronous routing engine is in
         * use then any asynchronous thread that may continue routing the
         * org.apache.camel.Exchange may not be able to read the Netty stream,
         * because Netty has closed it.
         */
        private Boolean disableStreamCache = false;
        /**
         * Whether to send back HTTP status code 503 when the consumer has been
         * suspended. If the option is false then the Netty Acceptor is unbound
         * when the consumer is suspended, so clients cannot connect anymore.
         */
        private Boolean send503whenSuspended = true;
        /**
         * Value in bytes the max content length per chunked frame received on
         * the Netty HTTP server.
         */
        private Integer chunkedMaxContentLength = 1048576;
        /**
         * The maximum length of all headers. If the sum of the length of each
         * header exceeds this value, a
         * io.netty.handler.codec.TooLongFrameException will be raised.
         */
        private Integer maxHeaderSize = 8192;
        private Boolean allowDefaultCodec;
        /**
         * The status codes which are considered a success response. The values
         * are inclusive. Multiple ranges can be defined, separated by comma,
         * e.g. 200-204,209,301-304. Each range must be a single number or
         * from-to with the dash included. The default range is 200-299
         */
        private String okStatusCodeRange = "200-299";
        /**
         * Sets whether to use a relative path in HTTP requests.
         */
        private Boolean useRelativePath = false;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Boolean getCompression() {
            return compression;
        }

        public void setCompression(Boolean compression) {
            this.compression = compression;
        }

        public Boolean getThrowExceptionOnFailure() {
            return throwExceptionOnFailure;
        }

        public void setThrowExceptionOnFailure(Boolean throwExceptionOnFailure) {
            this.throwExceptionOnFailure = throwExceptionOnFailure;
        }

        public Boolean getTransferException() {
            return transferException;
        }

        public void setTransferException(Boolean transferException) {
            this.transferException = transferException;
        }

        public Boolean getUrlDecodeHeaders() {
            return urlDecodeHeaders;
        }

        public void setUrlDecodeHeaders(Boolean urlDecodeHeaders) {
            this.urlDecodeHeaders = urlDecodeHeaders;
        }

        public Boolean getMapHeaders() {
            return mapHeaders;
        }

        public void setMapHeaders(Boolean mapHeaders) {
            this.mapHeaders = mapHeaders;
        }

        public Boolean getMatchOnUriPrefix() {
            return matchOnUriPrefix;
        }

        public void setMatchOnUriPrefix(Boolean matchOnUriPrefix) {
            this.matchOnUriPrefix = matchOnUriPrefix;
        }

        public Boolean getBridgeEndpoint() {
            return bridgeEndpoint;
        }

        public void setBridgeEndpoint(Boolean bridgeEndpoint) {
            this.bridgeEndpoint = bridgeEndpoint;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Boolean getDisableStreamCache() {
            return disableStreamCache;
        }

        public void setDisableStreamCache(Boolean disableStreamCache) {
            this.disableStreamCache = disableStreamCache;
        }

        public Boolean getSend503whenSuspended() {
            return send503whenSuspended;
        }

        public void setSend503whenSuspended(Boolean send503whenSuspended) {
            this.send503whenSuspended = send503whenSuspended;
        }

        public Integer getChunkedMaxContentLength() {
            return chunkedMaxContentLength;
        }

        public void setChunkedMaxContentLength(Integer chunkedMaxContentLength) {
            this.chunkedMaxContentLength = chunkedMaxContentLength;
        }

        public Integer getMaxHeaderSize() {
            return maxHeaderSize;
        }

        public void setMaxHeaderSize(Integer maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
        }

        public Boolean getAllowDefaultCodec() {
            return allowDefaultCodec;
        }

        public void setAllowDefaultCodec(Boolean allowDefaultCodec) {
            this.allowDefaultCodec = allowDefaultCodec;
        }

        public String getOkStatusCodeRange() {
            return okStatusCodeRange;
        }

        public void setOkStatusCodeRange(String okStatusCodeRange) {
            this.okStatusCodeRange = okStatusCodeRange;
        }

        public Boolean getUseRelativePath() {
            return useRelativePath;
        }

        public void setUseRelativePath(Boolean useRelativePath) {
            this.useRelativePath = useRelativePath;
        }
    }

    public static class NettyHttpSecurityConfigurationNestedConfiguration {
        public static final Class CAMEL_NESTED_CLASS = org.apache.camel.component.netty4.http.NettyHttpSecurityConfiguration.class;
        /**
         * Whether to enable authentication
         * <p/>
         * This is by default enabled.
         */
        private Boolean authenticate;
        /**
         * The supported restricted.
         * <p/>
         * Currently only Basic is supported.
         */
        private String constraint;
        /**
         * Sets the name of the realm to use.
         */
        private String realm;
        /**
         * Sets a {@link SecurityConstraint} to use for checking if a web
         * resource is restricted or not
         * <p/>
         * By default this is <tt>null</tt>, which means all resources is
         * restricted.
         */
        private SecurityConstraint securityConstraint;
        /**
         * Sets the {@link SecurityAuthenticator} to use for authenticating the
         * {@link HttpPrincipal} .
         */
        private SecurityAuthenticator securityAuthenticator;
        /**
         * Sets a logging level to use for logging denied login attempts (incl
         * stacktraces)
         * <p/>
         * This level is by default DEBUG.
         */
        private LoggingLevel loginDeniedLoggingLevel;
        private String roleClassName;

        public Boolean getAuthenticate() {
            return authenticate;
        }

        public void setAuthenticate(Boolean authenticate) {
            this.authenticate = authenticate;
        }

        public String getConstraint() {
            return constraint;
        }

        public void setConstraint(String constraint) {
            this.constraint = constraint;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public SecurityConstraint getSecurityConstraint() {
            return securityConstraint;
        }

        public void setSecurityConstraint(SecurityConstraint securityConstraint) {
            this.securityConstraint = securityConstraint;
        }

        public SecurityAuthenticator getSecurityAuthenticator() {
            return securityAuthenticator;
        }

        public void setSecurityAuthenticator(
                SecurityAuthenticator securityAuthenticator) {
            this.securityAuthenticator = securityAuthenticator;
        }

        public LoggingLevel getLoginDeniedLoggingLevel() {
            return loginDeniedLoggingLevel;
        }

        public void setLoginDeniedLoggingLevel(
                LoggingLevel loginDeniedLoggingLevel) {
            this.loginDeniedLoggingLevel = loginDeniedLoggingLevel;
        }

        public String getRoleClassName() {
            return roleClassName;
        }

        public void setRoleClassName(String roleClassName) {
            this.roleClassName = roleClassName;
        }
    }
}