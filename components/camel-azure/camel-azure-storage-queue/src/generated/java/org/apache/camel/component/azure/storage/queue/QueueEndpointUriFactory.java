/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.azure.storage.queue;

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
public class QueueEndpointUriFactory extends org.apache.camel.support.component.EndpointUriFactorySupport implements EndpointUriFactory {

    private static final String BASE = ":accountName/queueName";

    private static final Set<String> PROPERTY_NAMES;
    private static final Set<String> SECRET_PROPERTY_NAMES;
    static {
        Set<String> props = new HashSet<>(34);
        props.add("backoffMultiplier");
        props.add("accountName");
        props.add("credentials");
        props.add("maxMessages");
        props.add("initialDelay");
        props.add("timeout");
        props.add("visibilityTimeout");
        props.add("scheduler");
        props.add("timeToLive");
        props.add("bridgeErrorHandler");
        props.add("useFixedDelay");
        props.add("runLoggingLevel");
        props.add("backoffErrorThreshold");
        props.add("greedy");
        props.add("scheduledExecutorService");
        props.add("repeatCount");
        props.add("timeUnit");
        props.add("autoDiscoverClient");
        props.add("sendEmptyMessageWhenIdle");
        props.add("schedulerProperties");
        props.add("exchangePattern");
        props.add("messageId");
        props.add("popReceipt");
        props.add("backoffIdleThreshold");
        props.add("lazyStartProducer");
        props.add("queueName");
        props.add("delay");
        props.add("pollStrategy");
        props.add("startScheduler");
        props.add("accessKey");
        props.add("serviceClient");
        props.add("createQueue");
        props.add("exceptionHandler");
        props.add("operation");
        PROPERTY_NAMES = Collections.unmodifiableSet(props);
        Set<String> secretProps = new HashSet<>(1);
        secretProps.add("accessKey");
        SECRET_PROPERTY_NAMES = Collections.unmodifiableSet(secretProps);
    }

    @Override
    public boolean isEnabled(String scheme) {
        return "azure-storage-queue".equals(scheme);
    }

    @Override
    public String buildUri(String scheme, Map<String, Object> properties, boolean encode) throws URISyntaxException {
        String syntax = scheme + BASE;
        String uri = syntax;

        Map<String, Object> copy = new HashMap<>(properties);

        uri = buildPathParameter(syntax, uri, "accountName", null, false, copy);
        uri = buildPathParameter(syntax, uri, "queueName", null, false, copy);
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

