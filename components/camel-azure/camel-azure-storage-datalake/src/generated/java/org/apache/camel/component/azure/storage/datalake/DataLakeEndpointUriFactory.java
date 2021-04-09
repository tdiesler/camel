/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.azure.storage.datalake;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.camel.spi.EndpointUriFactory;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
public class DataLakeEndpointUriFactory extends org.apache.camel.support.component.EndpointUriFactorySupport implements EndpointUriFactory {

    private static final String BASE = ":accountName/fileSystemName";

    private static final Set<String> PROPERTY_NAMES;
    private static final Set<String> SECRET_PROPERTY_NAMES;
    static {
        Set<String> props = new HashSet<>(52);
        props.add("fileName");
        props.add("initialDelay");
        props.add("path");
        props.add("bridgeErrorHandler");
        props.add("maxResults");
        props.add("closeStreamAfterRead");
        props.add("greedy");
        props.add("clientSecret");
        props.add("scheduledExecutorService");
        props.add("fileSystemName");
        props.add("directoryName");
        props.add("repeatCount");
        props.add("sendEmptyMessageWhenIdle");
        props.add("schedulerProperties");
        props.add("dataLakeServiceClient");
        props.add("backoffIdleThreshold");
        props.add("regex");
        props.add("lazyStartProducer");
        props.add("delay");
        props.add("startScheduler");
        props.add("position");
        props.add("exceptionHandler");
        props.add("openOptions");
        props.add("backoffMultiplier");
        props.add("umask");
        props.add("accountName");
        props.add("sharedKeyCredential");
        props.add("recursive");
        props.add("timeout");
        props.add("dataCount");
        props.add("scheduler");
        props.add("maxRetryRequests");
        props.add("useFixedDelay");
        props.add("clientSecretCredential");
        props.add("runLoggingLevel");
        props.add("backoffErrorThreshold");
        props.add("close");
        props.add("timeUnit");
        props.add("retainUncommitedData");
        props.add("clientId");
        props.add("expression");
        props.add("downloadLinkExpiration");
        props.add("exchangePattern");
        props.add("fileOffset");
        props.add("permission");
        props.add("accountKey");
        props.add("pollStrategy");
        props.add("serviceClient");
        props.add("fileDir");
        props.add("tenantId");
        props.add("operation");
        props.add("userPrincipalNameReturned");
        PROPERTY_NAMES = Collections.unmodifiableSet(props);
        SECRET_PROPERTY_NAMES = Collections.emptySet();
    }

    @Override
    public boolean isEnabled(String scheme) {
        return "azure-storage-datalake".equals(scheme);
    }

    @Override
    public String buildUri(String scheme, Map<String, Object> properties, boolean encode) throws URISyntaxException {
        String syntax = scheme + BASE;
        String uri = syntax;

        Map<String, Object> copy = new HashMap<>(properties);

        uri = buildPathParameter(syntax, uri, "accountName", null, false, copy);
        uri = buildPathParameter(syntax, uri, "fileSystemName", null, false, copy);
        uri = buildQueryParameters(uri, copy, encode);
        return uri;
    }

    @Override
    public Set<String> propertyNames() {
        return PROPERTY_NAMES;
    }

    @Override
    public Set<String> secretPropertyNames() {
        return SECRET_PROPERTY_NAMES;
    }

    @Override
    public boolean isLenientProperties() {
        return false;
    }
}

