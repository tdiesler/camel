/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.aws2.kinesis;

import javax.annotation.processing.Generated;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.EndpointSchemaGeneratorMojo")
@SuppressWarnings("unchecked")
public class Kinesis2ComponentConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    private org.apache.camel.component.aws2.kinesis.Kinesis2Configuration getOrCreateConfiguration(Kinesis2Component target) {
        if (target.getConfiguration() == null) {
            target.setConfiguration(new org.apache.camel.component.aws2.kinesis.Kinesis2Configuration());
        }
        return target.getConfiguration();
    }

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        Kinesis2Component target = (Kinesis2Component) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "accessKey": getOrCreateConfiguration(target).setAccessKey(property(camelContext, java.lang.String.class, value)); return true;
        case "amazonkinesisclient":
        case "amazonKinesisClient": getOrCreateConfiguration(target).setAmazonKinesisClient(property(camelContext, software.amazon.awssdk.services.kinesis.KinesisClient.class, value)); return true;
        case "asyncclient":
        case "asyncClient": getOrCreateConfiguration(target).setAsyncClient(property(camelContext, boolean.class, value)); return true;
        case "autowiredenabled":
        case "autowiredEnabled": target.setAutowiredEnabled(property(camelContext, boolean.class, value)); return true;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": target.setBridgeErrorHandler(property(camelContext, boolean.class, value)); return true;
        case "cborenabled":
        case "cborEnabled": getOrCreateConfiguration(target).setCborEnabled(property(camelContext, boolean.class, value)); return true;
        case "cloudwatchasyncclient":
        case "cloudWatchAsyncClient": getOrCreateConfiguration(target).setCloudWatchAsyncClient(property(camelContext, software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient.class, value)); return true;
        case "configuration": target.setConfiguration(property(camelContext, org.apache.camel.component.aws2.kinesis.Kinesis2Configuration.class, value)); return true;
        case "dynamodbasyncclient":
        case "dynamoDbAsyncClient": getOrCreateConfiguration(target).setDynamoDbAsyncClient(property(camelContext, software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient.class, value)); return true;
        case "healthcheckconsumerenabled":
        case "healthCheckConsumerEnabled": target.setHealthCheckConsumerEnabled(property(camelContext, boolean.class, value)); return true;
        case "healthcheckproducerenabled":
        case "healthCheckProducerEnabled": target.setHealthCheckProducerEnabled(property(camelContext, boolean.class, value)); return true;
        case "iteratortype":
        case "iteratorType": getOrCreateConfiguration(target).setIteratorType(property(camelContext, software.amazon.awssdk.services.kinesis.model.ShardIteratorType.class, value)); return true;
        case "kcldisablecloudwatchmetricsexport":
        case "kclDisableCloudwatchMetricsExport": getOrCreateConfiguration(target).setKclDisableCloudwatchMetricsExport(property(camelContext, boolean.class, value)); return true;
        case "lazystartproducer":
        case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
        case "maxresultsperrequest":
        case "maxResultsPerRequest": getOrCreateConfiguration(target).setMaxResultsPerRequest(property(camelContext, int.class, value)); return true;
        case "messagetimestamp":
        case "messageTimestamp": getOrCreateConfiguration(target).setMessageTimestamp(property(camelContext, java.lang.String.class, value)); return true;
        case "overrideendpoint":
        case "overrideEndpoint": getOrCreateConfiguration(target).setOverrideEndpoint(property(camelContext, boolean.class, value)); return true;
        case "profilecredentialsname":
        case "profileCredentialsName": getOrCreateConfiguration(target).setProfileCredentialsName(property(camelContext, java.lang.String.class, value)); return true;
        case "proxyhost":
        case "proxyHost": getOrCreateConfiguration(target).setProxyHost(property(camelContext, java.lang.String.class, value)); return true;
        case "proxyport":
        case "proxyPort": getOrCreateConfiguration(target).setProxyPort(property(camelContext, java.lang.Integer.class, value)); return true;
        case "proxyprotocol":
        case "proxyProtocol": getOrCreateConfiguration(target).setProxyProtocol(property(camelContext, software.amazon.awssdk.core.Protocol.class, value)); return true;
        case "region": getOrCreateConfiguration(target).setRegion(property(camelContext, java.lang.String.class, value)); return true;
        case "secretkey":
        case "secretKey": getOrCreateConfiguration(target).setSecretKey(property(camelContext, java.lang.String.class, value)); return true;
        case "sequencenumber":
        case "sequenceNumber": getOrCreateConfiguration(target).setSequenceNumber(property(camelContext, java.lang.String.class, value)); return true;
        case "sessiontoken":
        case "sessionToken": getOrCreateConfiguration(target).setSessionToken(property(camelContext, java.lang.String.class, value)); return true;
        case "shardclosed":
        case "shardClosed": getOrCreateConfiguration(target).setShardClosed(property(camelContext, org.apache.camel.component.aws2.kinesis.Kinesis2ShardClosedStrategyEnum.class, value)); return true;
        case "shardid":
        case "shardId": getOrCreateConfiguration(target).setShardId(property(camelContext, java.lang.String.class, value)); return true;
        case "shardmonitorinterval":
        case "shardMonitorInterval": getOrCreateConfiguration(target).setShardMonitorInterval(property(camelContext, long.class, value)); return true;
        case "trustallcertificates":
        case "trustAllCertificates": getOrCreateConfiguration(target).setTrustAllCertificates(property(camelContext, boolean.class, value)); return true;
        case "uriendpointoverride":
        case "uriEndpointOverride": getOrCreateConfiguration(target).setUriEndpointOverride(property(camelContext, java.lang.String.class, value)); return true;
        case "usedefaultcredentialsprovider":
        case "useDefaultCredentialsProvider": getOrCreateConfiguration(target).setUseDefaultCredentialsProvider(property(camelContext, boolean.class, value)); return true;
        case "usekclconsumers":
        case "useKclConsumers": getOrCreateConfiguration(target).setUseKclConsumers(property(camelContext, boolean.class, value)); return true;
        case "useprofilecredentialsprovider":
        case "useProfileCredentialsProvider": getOrCreateConfiguration(target).setUseProfileCredentialsProvider(property(camelContext, boolean.class, value)); return true;
        case "usesessioncredentials":
        case "useSessionCredentials": getOrCreateConfiguration(target).setUseSessionCredentials(property(camelContext, boolean.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public String[] getAutowiredNames() {
        return new String[]{"amazonKinesisClient"};
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "accessKey": return java.lang.String.class;
        case "amazonkinesisclient":
        case "amazonKinesisClient": return software.amazon.awssdk.services.kinesis.KinesisClient.class;
        case "asyncclient":
        case "asyncClient": return boolean.class;
        case "autowiredenabled":
        case "autowiredEnabled": return boolean.class;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return boolean.class;
        case "cborenabled":
        case "cborEnabled": return boolean.class;
        case "cloudwatchasyncclient":
        case "cloudWatchAsyncClient": return software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient.class;
        case "configuration": return org.apache.camel.component.aws2.kinesis.Kinesis2Configuration.class;
        case "dynamodbasyncclient":
        case "dynamoDbAsyncClient": return software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient.class;
        case "healthcheckconsumerenabled":
        case "healthCheckConsumerEnabled": return boolean.class;
        case "healthcheckproducerenabled":
        case "healthCheckProducerEnabled": return boolean.class;
        case "iteratortype":
        case "iteratorType": return software.amazon.awssdk.services.kinesis.model.ShardIteratorType.class;
        case "kcldisablecloudwatchmetricsexport":
        case "kclDisableCloudwatchMetricsExport": return boolean.class;
        case "lazystartproducer":
        case "lazyStartProducer": return boolean.class;
        case "maxresultsperrequest":
        case "maxResultsPerRequest": return int.class;
        case "messagetimestamp":
        case "messageTimestamp": return java.lang.String.class;
        case "overrideendpoint":
        case "overrideEndpoint": return boolean.class;
        case "profilecredentialsname":
        case "profileCredentialsName": return java.lang.String.class;
        case "proxyhost":
        case "proxyHost": return java.lang.String.class;
        case "proxyport":
        case "proxyPort": return java.lang.Integer.class;
        case "proxyprotocol":
        case "proxyProtocol": return software.amazon.awssdk.core.Protocol.class;
        case "region": return java.lang.String.class;
        case "secretkey":
        case "secretKey": return java.lang.String.class;
        case "sequencenumber":
        case "sequenceNumber": return java.lang.String.class;
        case "sessiontoken":
        case "sessionToken": return java.lang.String.class;
        case "shardclosed":
        case "shardClosed": return org.apache.camel.component.aws2.kinesis.Kinesis2ShardClosedStrategyEnum.class;
        case "shardid":
        case "shardId": return java.lang.String.class;
        case "shardmonitorinterval":
        case "shardMonitorInterval": return long.class;
        case "trustallcertificates":
        case "trustAllCertificates": return boolean.class;
        case "uriendpointoverride":
        case "uriEndpointOverride": return java.lang.String.class;
        case "usedefaultcredentialsprovider":
        case "useDefaultCredentialsProvider": return boolean.class;
        case "usekclconsumers":
        case "useKclConsumers": return boolean.class;
        case "useprofilecredentialsprovider":
        case "useProfileCredentialsProvider": return boolean.class;
        case "usesessioncredentials":
        case "useSessionCredentials": return boolean.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        Kinesis2Component target = (Kinesis2Component) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "accessKey": return getOrCreateConfiguration(target).getAccessKey();
        case "amazonkinesisclient":
        case "amazonKinesisClient": return getOrCreateConfiguration(target).getAmazonKinesisClient();
        case "asyncclient":
        case "asyncClient": return getOrCreateConfiguration(target).isAsyncClient();
        case "autowiredenabled":
        case "autowiredEnabled": return target.isAutowiredEnabled();
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return target.isBridgeErrorHandler();
        case "cborenabled":
        case "cborEnabled": return getOrCreateConfiguration(target).isCborEnabled();
        case "cloudwatchasyncclient":
        case "cloudWatchAsyncClient": return getOrCreateConfiguration(target).getCloudWatchAsyncClient();
        case "configuration": return target.getConfiguration();
        case "dynamodbasyncclient":
        case "dynamoDbAsyncClient": return getOrCreateConfiguration(target).getDynamoDbAsyncClient();
        case "healthcheckconsumerenabled":
        case "healthCheckConsumerEnabled": return target.isHealthCheckConsumerEnabled();
        case "healthcheckproducerenabled":
        case "healthCheckProducerEnabled": return target.isHealthCheckProducerEnabled();
        case "iteratortype":
        case "iteratorType": return getOrCreateConfiguration(target).getIteratorType();
        case "kcldisablecloudwatchmetricsexport":
        case "kclDisableCloudwatchMetricsExport": return getOrCreateConfiguration(target).isKclDisableCloudwatchMetricsExport();
        case "lazystartproducer":
        case "lazyStartProducer": return target.isLazyStartProducer();
        case "maxresultsperrequest":
        case "maxResultsPerRequest": return getOrCreateConfiguration(target).getMaxResultsPerRequest();
        case "messagetimestamp":
        case "messageTimestamp": return getOrCreateConfiguration(target).getMessageTimestamp();
        case "overrideendpoint":
        case "overrideEndpoint": return getOrCreateConfiguration(target).isOverrideEndpoint();
        case "profilecredentialsname":
        case "profileCredentialsName": return getOrCreateConfiguration(target).getProfileCredentialsName();
        case "proxyhost":
        case "proxyHost": return getOrCreateConfiguration(target).getProxyHost();
        case "proxyport":
        case "proxyPort": return getOrCreateConfiguration(target).getProxyPort();
        case "proxyprotocol":
        case "proxyProtocol": return getOrCreateConfiguration(target).getProxyProtocol();
        case "region": return getOrCreateConfiguration(target).getRegion();
        case "secretkey":
        case "secretKey": return getOrCreateConfiguration(target).getSecretKey();
        case "sequencenumber":
        case "sequenceNumber": return getOrCreateConfiguration(target).getSequenceNumber();
        case "sessiontoken":
        case "sessionToken": return getOrCreateConfiguration(target).getSessionToken();
        case "shardclosed":
        case "shardClosed": return getOrCreateConfiguration(target).getShardClosed();
        case "shardid":
        case "shardId": return getOrCreateConfiguration(target).getShardId();
        case "shardmonitorinterval":
        case "shardMonitorInterval": return getOrCreateConfiguration(target).getShardMonitorInterval();
        case "trustallcertificates":
        case "trustAllCertificates": return getOrCreateConfiguration(target).isTrustAllCertificates();
        case "uriendpointoverride":
        case "uriEndpointOverride": return getOrCreateConfiguration(target).getUriEndpointOverride();
        case "usedefaultcredentialsprovider":
        case "useDefaultCredentialsProvider": return getOrCreateConfiguration(target).isUseDefaultCredentialsProvider();
        case "usekclconsumers":
        case "useKclConsumers": return getOrCreateConfiguration(target).isUseKclConsumers();
        case "useprofilecredentialsprovider":
        case "useProfileCredentialsProvider": return getOrCreateConfiguration(target).isUseProfileCredentialsProvider();
        case "usesessioncredentials":
        case "useSessionCredentials": return getOrCreateConfiguration(target).isUseSessionCredentials();
        default: return null;
        }
    }
}

