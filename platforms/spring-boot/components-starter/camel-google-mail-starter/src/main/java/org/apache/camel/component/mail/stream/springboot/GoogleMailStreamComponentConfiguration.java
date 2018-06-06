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
package org.apache.camel.component.mail.stream.springboot;

import java.util.List;
import javax.annotation.Generated;
import org.apache.camel.component.google.mail.GoogleMailClientFactory;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The google-mail component provides access to Google Mail.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.component.google-mail-stream")
public class GoogleMailStreamComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * The configuration
     */
    private GoogleMailStreamConfigurationNestedConfiguration configuration;
    /**
     * The client Factory
     */
    @NestedConfigurationProperty
    private GoogleMailClientFactory clientFactory;
    /**
     * Whether the component should resolve property placeholders on itself when
     * starting. Only properties which are of String type can use property
     * placeholders.
     */
    private Boolean resolvePropertyPlaceholders = true;

    public GoogleMailStreamConfigurationNestedConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(
            GoogleMailStreamConfigurationNestedConfiguration configuration) {
        this.configuration = configuration;
    }

    public GoogleMailClientFactory getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(GoogleMailClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public Boolean getResolvePropertyPlaceholders() {
        return resolvePropertyPlaceholders;
    }

    public void setResolvePropertyPlaceholders(
            Boolean resolvePropertyPlaceholders) {
        this.resolvePropertyPlaceholders = resolvePropertyPlaceholders;
    }

    public static class GoogleMailStreamConfigurationNestedConfiguration {
        public static final Class CAMEL_NESTED_CLASS = org.apache.camel.component.mail.stream.GoogleMailStreamConfiguration.class;
        /**
         * Client ID of the mail application
         */
        private String clientId;
        /**
         * Client secret of the mail application
         */
        private String clientSecret;
        /**
         * OAuth 2 access token. This typically expires after an hour so
         * refreshToken is recommended for long term usage.
         */
        private String accessToken;
        /**
         * OAuth 2 refresh token. Using this, the Google Calendar component can
         * obtain a new accessToken whenever the current one expires - a
         * necessity if the application is long-lived.
         */
        private String refreshToken;
        /**
         * Google mail application name. Example would be
         * "camel-google-mail/1.0"
         */
        private String applicationName;
        /**
         * Specifies the level of permissions you want a mail application to
         * have to a user account. See
         * https://developers.google.com/gmail/api/auth/scopes for more info.
         */
        private List scopes;
        /**
         * Specifies an index for the endpoint
         */
        private String index;
        /**
         * The query to execute on gmail box
         */
        private String query = "is:unread";
        /**
         * Max results to be returned
         */
        private Long maxResults = 10L;
        /**
         * Comma separated list of labels to take into account
         */
        private String labels;
        /**
         * Mark the message as read once it has been consumed
         */
        private Boolean markAsRead = false;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public List getScopes() {
            return scopes;
        }

        public void setScopes(List scopes) {
            this.scopes = scopes;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Long getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(Long maxResults) {
            this.maxResults = maxResults;
        }

        public String getLabels() {
            return labels;
        }

        public void setLabels(String labels) {
            this.labels = labels;
        }

        public Boolean getMarkAsRead() {
            return markAsRead;
        }

        public void setMarkAsRead(Boolean markAsRead) {
            this.markAsRead = markAsRead;
        }
    }
}