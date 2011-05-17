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
package org.apache.camel.util.jsse;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration model for client side JSSE options.
 */
public class SSLContextClientParameters extends BaseSSLContextParameters {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLContextClientParameters.class);

    @Override
    protected boolean getAllowPassthrough() {
        return true;
    }

    @Override
    protected void configureSSLContext(SSLContext context) throws GeneralSecurityException {
        LOG.trace("Configuring client-side SSLContext parameters...");
        if (this.getSessionTimeout() != null) {
            LOG.debug("Configuring client-side SSLContext session timeout: " + this.getSessionTimeout());
            this.configureSessionContext(context.getClientSessionContext(), this.getSessionTimeout());
        }
        LOG.trace("Configured client-side SSLContext parameters.");
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation returns the empty list as the enabled cipher suites
     * and protocols are not client and server side specific in an
     * {@code SSLEngine}. Consequently, overriding them here would be a bit odd
     * as the client side specific configuration shouldn't really override a
     * shared client/server configuration option.
     */
    @Override
    protected List<Configurer<SSLEngine>> getSSLEngineConfigurers(SSLContext context) {
        // NOTE: if the super class gets additional shared configuration options beyond
        // cipher suites and protocols, this method needs to address that.
        return Collections.emptyList();
    }
    
    /**
     * This class has no bearing on {@code SSLServerSocketFactory} instances and therefore provides no
     * configurers for that purpose.
     */
    @Override
    protected List<Configurer<SSLServerSocketFactory>> getSSLServerSocketFactoryConfigurers(SSLContext context) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SSLContextClientParameters [getCipherSuites()=");
        builder.append(getCipherSuites());
        builder.append(", getCipherSuitesFilter()=");
        builder.append(getCipherSuitesFilter());
        builder.append(", getSecureSocketProtocols()=");
        builder.append(getSecureSocketProtocols());
        builder.append(", getSecureSocketProtocolsFilter()=");
        builder.append(getSecureSocketProtocolsFilter());
        builder.append(", getSessionTimeout()=");
        builder.append(getSessionTimeout());
        builder.append("]");
        return builder.toString();
    }
}
