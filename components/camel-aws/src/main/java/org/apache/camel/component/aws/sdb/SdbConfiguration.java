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
package org.apache.camel.component.aws.sdb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;

import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

@UriParams
public class SdbConfiguration {

    @UriPath @Metadata(required = "true")
    private String domainName;
    @UriParam
    private AmazonSimpleDB amazonSDBClient;
    @UriParam
    private String accessKey;
    @UriParam
    private String secretKey;
    @UriParam
    private String amazonSdbEndpoint;
    @UriParam
    private Integer maxNumberOfDomains;
    @UriParam
    private boolean consistentRead;
    @UriParam(defaultValue = "PutAttributes")
    private SdbOperations operation = SdbOperations.PutAttributes;
    @UriParam
    private String proxyHost;
    @UriParam
    private Integer proxyPort;
    @UriParam
    private String region;

    /**
     * The region with which the AWS-SDB client wants to work with.
     */
    public void setAmazonSdbEndpoint(String amazonSdbEndpoint) {
        this.amazonSdbEndpoint = amazonSdbEndpoint;
    }

    public String getAmazonSdbEndpoint() {
        return amazonSdbEndpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Amazon AWS Access Key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Amazon AWS Secret Key
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public AmazonSimpleDB getAmazonSDBClient() {
        return amazonSDBClient;
    }

    /**
     * To use the AmazonSimpleDB as the client
     */
    public void setAmazonSDBClient(AmazonSimpleDB amazonSDBClient) {
        this.amazonSDBClient = amazonSDBClient;
    }

    public String getDomainName() {
        return domainName;
    }

    /**
     * The name of the domain currently worked with.
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public SdbOperations getOperation() {
        return operation;
    }

    /**
     * Operation to perform
     */
    public void setOperation(SdbOperations operation) {
        this.operation = operation;
    }

    public Integer getMaxNumberOfDomains() {
        return maxNumberOfDomains;
    }

    /**
     * The maximum number of domain names you want returned. The range is 1 to 100.
     */
    public void setMaxNumberOfDomains(Integer maxNumberOfDomains) {
        this.maxNumberOfDomains = maxNumberOfDomains;
    }

    public boolean isConsistentRead() {
        return consistentRead;
    }

    /**
     * Determines whether or not strong consistency should be enforced when data is read.
     */
    public void setConsistentRead(boolean consistentRead) {
        this.consistentRead = consistentRead;
    }
    
    /**
     * To define a proxy host when instantiating the SDB client
     */
    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * To define a proxy port when instantiating the SDB client
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }
    
    /**
     * The region in which SDB client needs to work
     */
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
