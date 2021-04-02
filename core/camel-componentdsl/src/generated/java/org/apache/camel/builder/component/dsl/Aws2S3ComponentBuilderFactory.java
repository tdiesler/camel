/*
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
package org.apache.camel.builder.component.dsl;

import javax.annotation.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Component;

/**
 * Store and retrieve objects from AWS S3 Storage Service using AWS SDK version
 * 2.x.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface Aws2S3ComponentBuilderFactory {

    /**
     * AWS 2 S3 Storage Service (camel-aws2-s3)
     * Store and retrieve objects from AWS S3 Storage Service using AWS SDK
     * version 2.x.
     * 
     * Category: cloud,file
     * Since: 3.2
     * Maven coordinates: org.apache.camel:camel-aws2-s3
     * 
     * @return the dsl builder
     */
    static Aws2S3ComponentBuilder aws2S3() {
        return new Aws2S3ComponentBuilderImpl();
    }

    /**
     * Builder for the AWS 2 S3 Storage Service component.
     */
    interface Aws2S3ComponentBuilder
            extends
                ComponentBuilder<AWS2S3Component> {
        /**
         * Reference to a com.amazonaws.services.s3.AmazonS3 in the registry.
         * 
         * The option is a:
         * &lt;code&gt;software.amazon.awssdk.services.s3.S3Client&lt;/code&gt;
         * type.
         * 
         * Group: common
         * 
         * @param amazonS3Client the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder amazonS3Client(
                software.amazon.awssdk.services.s3.S3Client amazonS3Client) {
            doSetProperty("amazonS3Client", amazonS3Client);
            return this;
        }
        /**
         * An S3 Presigner for Request, used mainly in createDownloadLink
         * operation.
         * 
         * The option is a:
         * &lt;code&gt;software.amazon.awssdk.services.s3.presigner.S3Presigner&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param amazonS3Presigner the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder amazonS3Presigner(
                software.amazon.awssdk.services.s3.presigner.S3Presigner amazonS3Presigner) {
            doSetProperty("amazonS3Presigner", amazonS3Presigner);
            return this;
        }
        /**
         * Setting the autocreation of the S3 bucket bucketName. This will apply
         * also in case of moveAfterRead option enabled and it will create the
         * destinationBucket if it doesn't exist already.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param autoCreateBucket the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder autoCreateBucket(boolean autoCreateBucket) {
            doSetProperty("autoCreateBucket", autoCreateBucket);
            return this;
        }
        /**
         * The component configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.aws2.s3.AWS2S3Configuration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder configuration(
                org.apache.camel.component.aws2.s3.AWS2S3Configuration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
        /**
         * Set the need for overidding the endpoint. This option needs to be
         * used in combination with uriEndpointOverride option.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param overrideEndpoint the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder overrideEndpoint(boolean overrideEndpoint) {
            doSetProperty("overrideEndpoint", overrideEndpoint);
            return this;
        }
        /**
         * If we want to use a POJO request as body or not.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param pojoRequest the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder pojoRequest(boolean pojoRequest) {
            doSetProperty("pojoRequest", pojoRequest);
            return this;
        }
        /**
         * The policy for this queue to set in the
         * com.amazonaws.services.s3.AmazonS3#setBucketPolicy() method.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param policy the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder policy(java.lang.String policy) {
            doSetProperty("policy", policy);
            return this;
        }
        /**
         * To define a proxy host when instantiating the SQS client.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param proxyHost the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder proxyHost(java.lang.String proxyHost) {
            doSetProperty("proxyHost", proxyHost);
            return this;
        }
        /**
         * Specify a proxy port to be used inside the client definition.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param proxyPort the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder proxyPort(java.lang.Integer proxyPort) {
            doSetProperty("proxyPort", proxyPort);
            return this;
        }
        /**
         * To define a proxy protocol when instantiating the S3 client.
         * 
         * The option is a:
         * &lt;code&gt;software.amazon.awssdk.core.Protocol&lt;/code&gt; type.
         * 
         * Default: HTTPS
         * Group: common
         * 
         * @param proxyProtocol the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder proxyProtocol(
                software.amazon.awssdk.core.Protocol proxyProtocol) {
            doSetProperty("proxyProtocol", proxyProtocol);
            return this;
        }
        /**
         * The region in which S3 client needs to work. When using this
         * parameter, the configuration will expect the lowercase name of the
         * region (for example ap-east-1) You'll need to use the name
         * Region.EU_WEST_1.id().
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param region the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder region(java.lang.String region) {
            doSetProperty("region", region);
            return this;
        }
        /**
         * If we want to trust all certificates in case of overriding the
         * endpoint.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param trustAllCertificates the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder trustAllCertificates(
                boolean trustAllCertificates) {
            doSetProperty("trustAllCertificates", trustAllCertificates);
            return this;
        }
        /**
         * Set the overriding uri endpoint. This option needs to be used in
         * combination with overrideEndpoint option.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param uriEndpointOverride the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder uriEndpointOverride(
                java.lang.String uriEndpointOverride) {
            doSetProperty("uriEndpointOverride", uriEndpointOverride);
            return this;
        }
        /**
         * Set whether the S3 client should expect to load credentials through a
         * default credentials provider or to expect static credentials to be
         * passed in.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param useDefaultCredentialsProvider the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder useDefaultCredentialsProvider(
                boolean useDefaultCredentialsProvider) {
            doSetProperty("useDefaultCredentialsProvider", useDefaultCredentialsProvider);
            return this;
        }
        /**
         * Define the customer algorithm to use in case CustomerKey is enabled.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common (advanced)
         * 
         * @param customerAlgorithm the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder customerAlgorithm(
                java.lang.String customerAlgorithm) {
            doSetProperty("customerAlgorithm", customerAlgorithm);
            return this;
        }
        /**
         * Define the id of Customer key to use in case CustomerKey is enabled.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common (advanced)
         * 
         * @param customerKeyId the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder customerKeyId(
                java.lang.String customerKeyId) {
            doSetProperty("customerKeyId", customerKeyId);
            return this;
        }
        /**
         * Define the MD5 of Customer key to use in case CustomerKey is enabled.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common (advanced)
         * 
         * @param customerKeyMD5 the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder customerKeyMD5(
                java.lang.String customerKeyMD5) {
            doSetProperty("customerKeyMD5", customerKeyMD5);
            return this;
        }
        /**
         * Allows for bridging the consumer to the Camel routing Error Handler,
         * which mean any exceptions occurred while the consumer is trying to
         * pickup incoming messages, or the likes, will now be processed as a
         * message and handled by the routing Error Handler. By default the
         * consumer will use the org.apache.camel.spi.ExceptionHandler to deal
         * with exceptions, that will be logged at WARN or ERROR level and
         * ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param bridgeErrorHandler the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder bridgeErrorHandler(
                boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
        /**
         * Delete objects from S3 after they have been retrieved. The delete is
         * only performed if the Exchange is committed. If a rollback occurs,
         * the object is not deleted. If this option is false, then the same
         * objects will be retrieve over and over again on the polls. Therefore
         * you need to use the Idempotent Consumer EIP in the route to filter
         * out duplicates. You can filter using the AWS2S3Constants#BUCKET_NAME
         * and AWS2S3Constants#KEY headers, or only the AWS2S3Constants#KEY
         * header.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: consumer
         * 
         * @param deleteAfterRead the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder deleteAfterRead(boolean deleteAfterRead) {
            doSetProperty("deleteAfterRead", deleteAfterRead);
            return this;
        }
        /**
         * The delimiter which is used in the
         * com.amazonaws.services.s3.model.ListObjectsRequest to only consume
         * objects we are interested in.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param delimiter the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder delimiter(java.lang.String delimiter) {
            doSetProperty("delimiter", delimiter);
            return this;
        }
        /**
         * Define the destination bucket where an object must be moved when
         * moveAfterRead is set to true.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param destinationBucket the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder destinationBucket(
                java.lang.String destinationBucket) {
            doSetProperty("destinationBucket", destinationBucket);
            return this;
        }
        /**
         * Define the destination bucket prefix to use when an object must be
         * moved and moveAfterRead is set to true.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param destinationBucketPrefix the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder destinationBucketPrefix(
                java.lang.String destinationBucketPrefix) {
            doSetProperty("destinationBucketPrefix", destinationBucketPrefix);
            return this;
        }
        /**
         * Define the destination bucket suffix to use when an object must be
         * moved and moveAfterRead is set to true.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param destinationBucketSuffix the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder destinationBucketSuffix(
                java.lang.String destinationBucketSuffix) {
            doSetProperty("destinationBucketSuffix", destinationBucketSuffix);
            return this;
        }
        /**
         * If provided, Camel will only consume files if a done file exists.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param doneFileName the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder doneFileName(
                java.lang.String doneFileName) {
            doSetProperty("doneFileName", doneFileName);
            return this;
        }
        /**
         * To get the object from the bucket with the given file name.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param fileName the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder fileName(java.lang.String fileName) {
            doSetProperty("fileName", fileName);
            return this;
        }
        /**
         * If it is true, the S3Object exchange will be consumed and put into
         * the body and closed. If false the S3Object stream will be put raw
         * into the body and the headers will be set with the S3 object
         * metadata. This option is strongly related to autocloseBody option. In
         * case of setting includeBody to true because the S3Object stream will
         * be consumed then it will also be closed in case of includeBody false
         * then it will be up to the caller to close the S3Object stream.
         * However setting autocloseBody to true when includeBody is false it
         * will schedule to close the S3Object stream automatically on exchange
         * completion.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: consumer
         * 
         * @param includeBody the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder includeBody(boolean includeBody) {
            doSetProperty("includeBody", includeBody);
            return this;
        }
        /**
         * If it is true, the folders/directories will be consumed. If it is
         * false, they will be ignored, and Exchanges will not be created for
         * those.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: consumer
         * 
         * @param includeFolders the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder includeFolders(boolean includeFolders) {
            doSetProperty("includeFolders", includeFolders);
            return this;
        }
        /**
         * Move objects from S3 bucket to a different bucket after they have
         * been retrieved. To accomplish the operation the destinationBucket
         * option must be set. The copy bucket operation is only performed if
         * the Exchange is committed. If a rollback occurs, the object is not
         * moved.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param moveAfterRead the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder moveAfterRead(boolean moveAfterRead) {
            doSetProperty("moveAfterRead", moveAfterRead);
            return this;
        }
        /**
         * The prefix which is used in the
         * com.amazonaws.services.s3.model.ListObjectsRequest to only consume
         * objects we are interested in.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: consumer
         * 
         * @param prefix the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder prefix(java.lang.String prefix) {
            doSetProperty("prefix", prefix);
            return this;
        }
        /**
         * If this option is true and includeBody is false, then the
         * S3Object.close() method will be called on exchange completion. This
         * option is strongly related to includeBody option. In case of setting
         * includeBody to false and autocloseBody to false, it will be up to the
         * caller to close the S3Object stream. Setting autocloseBody to true,
         * will close the S3Object stream automatically.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: consumer (advanced)
         * 
         * @param autocloseBody the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder autocloseBody(boolean autocloseBody) {
            doSetProperty("autocloseBody", autocloseBody);
            return this;
        }
        /**
         * The number of messages composing a batch in stream mode.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 10
         * Group: producer
         * 
         * @param batchMessageNumber the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder batchMessageNumber(int batchMessageNumber) {
            doSetProperty("batchMessageNumber", batchMessageNumber);
            return this;
        }
        /**
         * The batch size (in bytes) in stream mode.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1000000
         * Group: producer
         * 
         * @param batchSize the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder batchSize(int batchSize) {
            doSetProperty("batchSize", batchSize);
            return this;
        }
        /**
         * Delete file object after the S3 file has been uploaded.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param deleteAfterWrite the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder deleteAfterWrite(boolean deleteAfterWrite) {
            doSetProperty("deleteAfterWrite", deleteAfterWrite);
            return this;
        }
        /**
         * Setting the key name for an element in the bucket through endpoint
         * parameter.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param keyName the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder keyName(java.lang.String keyName) {
            doSetProperty("keyName", keyName);
            return this;
        }
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder lazyStartProducer(
                boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * If it is true, camel will upload the file with multi part format, the
         * part size is decided by the option of partSize.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param multiPartUpload the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder multiPartUpload(boolean multiPartUpload) {
            doSetProperty("multiPartUpload", multiPartUpload);
            return this;
        }
        /**
         * The naming strategy to use in stream mode.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.aws2.s3.stream.AWSS3NamingStrategyEnum&lt;/code&gt; type.
         * 
         * Default: progressive
         * Group: producer
         * 
         * @param namingStrategy the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder namingStrategy(
                org.apache.camel.component.aws2.s3.stream.AWSS3NamingStrategyEnum namingStrategy) {
            doSetProperty("namingStrategy", namingStrategy);
            return this;
        }
        /**
         * The operation to do in case the user don't want to do only an upload.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.aws2.s3.AWS2S3Operations&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param operation the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder operation(
                org.apache.camel.component.aws2.s3.AWS2S3Operations operation) {
            doSetProperty("operation", operation);
            return this;
        }
        /**
         * Setup the partSize which is used in multi part upload, the default
         * size is 25M.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 26214400
         * Group: producer
         * 
         * @param partSize the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder partSize(long partSize) {
            doSetProperty("partSize", partSize);
            return this;
        }
        /**
         * The storage class to set in the
         * com.amazonaws.services.s3.model.PutObjectRequest request.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param storageClass the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder storageClass(
                java.lang.String storageClass) {
            doSetProperty("storageClass", storageClass);
            return this;
        }
        /**
         * When stream mode is true the upload to bucket will be done in
         * streaming.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param streamMode the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder streamMode(boolean streamMode) {
            doSetProperty("streamMode", streamMode);
            return this;
        }
        /**
         * Define the id of KMS key to use in case KMS is enabled.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer (advanced)
         * 
         * @param awsKMSKeyId the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder awsKMSKeyId(java.lang.String awsKMSKeyId) {
            doSetProperty("awsKMSKeyId", awsKMSKeyId);
            return this;
        }
        /**
         * Define if KMS must be used or not.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param useAwsKMS the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder useAwsKMS(boolean useAwsKMS) {
            doSetProperty("useAwsKMS", useAwsKMS);
            return this;
        }
        /**
         * Define if Customer Key must be used or not.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param useCustomerKey the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder useCustomerKey(boolean useCustomerKey) {
            doSetProperty("useCustomerKey", useCustomerKey);
            return this;
        }
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * Amazon AWS Access Key.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param accessKey the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder accessKey(java.lang.String accessKey) {
            doSetProperty("accessKey", accessKey);
            return this;
        }
        /**
         * Amazon AWS Secret Key.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param secretKey the value to set
         * @return the dsl builder
         */
        default Aws2S3ComponentBuilder secretKey(java.lang.String secretKey) {
            doSetProperty("secretKey", secretKey);
            return this;
        }
    }

    class Aws2S3ComponentBuilderImpl
            extends
                AbstractComponentBuilder<AWS2S3Component>
            implements
                Aws2S3ComponentBuilder {
        @Override
        protected AWS2S3Component buildConcreteComponent() {
            return new AWS2S3Component();
        }
        private org.apache.camel.component.aws2.s3.AWS2S3Configuration getOrCreateConfiguration(
                org.apache.camel.component.aws2.s3.AWS2S3Component component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.aws2.s3.AWS2S3Configuration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "amazonS3Client": getOrCreateConfiguration((AWS2S3Component) component).setAmazonS3Client((software.amazon.awssdk.services.s3.S3Client) value); return true;
            case "amazonS3Presigner": getOrCreateConfiguration((AWS2S3Component) component).setAmazonS3Presigner((software.amazon.awssdk.services.s3.presigner.S3Presigner) value); return true;
            case "autoCreateBucket": getOrCreateConfiguration((AWS2S3Component) component).setAutoCreateBucket((boolean) value); return true;
            case "configuration": ((AWS2S3Component) component).setConfiguration((org.apache.camel.component.aws2.s3.AWS2S3Configuration) value); return true;
            case "overrideEndpoint": getOrCreateConfiguration((AWS2S3Component) component).setOverrideEndpoint((boolean) value); return true;
            case "pojoRequest": getOrCreateConfiguration((AWS2S3Component) component).setPojoRequest((boolean) value); return true;
            case "policy": getOrCreateConfiguration((AWS2S3Component) component).setPolicy((java.lang.String) value); return true;
            case "proxyHost": getOrCreateConfiguration((AWS2S3Component) component).setProxyHost((java.lang.String) value); return true;
            case "proxyPort": getOrCreateConfiguration((AWS2S3Component) component).setProxyPort((java.lang.Integer) value); return true;
            case "proxyProtocol": getOrCreateConfiguration((AWS2S3Component) component).setProxyProtocol((software.amazon.awssdk.core.Protocol) value); return true;
            case "region": getOrCreateConfiguration((AWS2S3Component) component).setRegion((java.lang.String) value); return true;
            case "trustAllCertificates": getOrCreateConfiguration((AWS2S3Component) component).setTrustAllCertificates((boolean) value); return true;
            case "uriEndpointOverride": getOrCreateConfiguration((AWS2S3Component) component).setUriEndpointOverride((java.lang.String) value); return true;
            case "useDefaultCredentialsProvider": getOrCreateConfiguration((AWS2S3Component) component).setUseDefaultCredentialsProvider((boolean) value); return true;
            case "customerAlgorithm": getOrCreateConfiguration((AWS2S3Component) component).setCustomerAlgorithm((java.lang.String) value); return true;
            case "customerKeyId": getOrCreateConfiguration((AWS2S3Component) component).setCustomerKeyId((java.lang.String) value); return true;
            case "customerKeyMD5": getOrCreateConfiguration((AWS2S3Component) component).setCustomerKeyMD5((java.lang.String) value); return true;
            case "bridgeErrorHandler": ((AWS2S3Component) component).setBridgeErrorHandler((boolean) value); return true;
            case "deleteAfterRead": getOrCreateConfiguration((AWS2S3Component) component).setDeleteAfterRead((boolean) value); return true;
            case "delimiter": getOrCreateConfiguration((AWS2S3Component) component).setDelimiter((java.lang.String) value); return true;
            case "destinationBucket": getOrCreateConfiguration((AWS2S3Component) component).setDestinationBucket((java.lang.String) value); return true;
            case "destinationBucketPrefix": getOrCreateConfiguration((AWS2S3Component) component).setDestinationBucketPrefix((java.lang.String) value); return true;
            case "destinationBucketSuffix": getOrCreateConfiguration((AWS2S3Component) component).setDestinationBucketSuffix((java.lang.String) value); return true;
            case "doneFileName": getOrCreateConfiguration((AWS2S3Component) component).setDoneFileName((java.lang.String) value); return true;
            case "fileName": getOrCreateConfiguration((AWS2S3Component) component).setFileName((java.lang.String) value); return true;
            case "includeBody": getOrCreateConfiguration((AWS2S3Component) component).setIncludeBody((boolean) value); return true;
            case "includeFolders": getOrCreateConfiguration((AWS2S3Component) component).setIncludeFolders((boolean) value); return true;
            case "moveAfterRead": getOrCreateConfiguration((AWS2S3Component) component).setMoveAfterRead((boolean) value); return true;
            case "prefix": getOrCreateConfiguration((AWS2S3Component) component).setPrefix((java.lang.String) value); return true;
            case "autocloseBody": getOrCreateConfiguration((AWS2S3Component) component).setAutocloseBody((boolean) value); return true;
            case "batchMessageNumber": getOrCreateConfiguration((AWS2S3Component) component).setBatchMessageNumber((int) value); return true;
            case "batchSize": getOrCreateConfiguration((AWS2S3Component) component).setBatchSize((int) value); return true;
            case "deleteAfterWrite": getOrCreateConfiguration((AWS2S3Component) component).setDeleteAfterWrite((boolean) value); return true;
            case "keyName": getOrCreateConfiguration((AWS2S3Component) component).setKeyName((java.lang.String) value); return true;
            case "lazyStartProducer": ((AWS2S3Component) component).setLazyStartProducer((boolean) value); return true;
            case "multiPartUpload": getOrCreateConfiguration((AWS2S3Component) component).setMultiPartUpload((boolean) value); return true;
            case "namingStrategy": getOrCreateConfiguration((AWS2S3Component) component).setNamingStrategy((org.apache.camel.component.aws2.s3.stream.AWSS3NamingStrategyEnum) value); return true;
            case "operation": getOrCreateConfiguration((AWS2S3Component) component).setOperation((org.apache.camel.component.aws2.s3.AWS2S3Operations) value); return true;
            case "partSize": getOrCreateConfiguration((AWS2S3Component) component).setPartSize((long) value); return true;
            case "storageClass": getOrCreateConfiguration((AWS2S3Component) component).setStorageClass((java.lang.String) value); return true;
            case "streamMode": getOrCreateConfiguration((AWS2S3Component) component).setStreamMode((boolean) value); return true;
            case "awsKMSKeyId": getOrCreateConfiguration((AWS2S3Component) component).setAwsKMSKeyId((java.lang.String) value); return true;
            case "useAwsKMS": getOrCreateConfiguration((AWS2S3Component) component).setUseAwsKMS((boolean) value); return true;
            case "useCustomerKey": getOrCreateConfiguration((AWS2S3Component) component).setUseCustomerKey((boolean) value); return true;
            case "autowiredEnabled": ((AWS2S3Component) component).setAutowiredEnabled((boolean) value); return true;
            case "accessKey": getOrCreateConfiguration((AWS2S3Component) component).setAccessKey((java.lang.String) value); return true;
            case "secretKey": getOrCreateConfiguration((AWS2S3Component) component).setSecretKey((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}