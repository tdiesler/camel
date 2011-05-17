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
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLContextServerParameters extends BaseSSLContextParameters {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLContextServerParameters.class);

    /**
     * The optional configuration options for server-side client-authentication
     * requirements.
     */
    protected ClientAuthentication clientAuthentication;
    
    /**
     * @see #setClientAuthentication(ClientAuthenticationParameters)   
     */
    public ClientAuthentication getClientAuthentication() {
        return clientAuthentication;
    }

    /**
     * Sets the configuration options for server-side client-authentication requirements.
     * 
     * @param value the desired configuration options or {@code null} to use the defaults
     */
    public void setClientAuthentication(ClientAuthentication value) {
        this.clientAuthentication = value;
    }
    
    @Override
    protected boolean getAllowPassthrough() {
        return true;
    }
    
    @Override
    protected void configureSSLContext(SSLContext context) throws GeneralSecurityException {
        LOG.debug("Configuring server-side SSLContext parameters...");
        if (this.getSessionTimeout() != null) {
            LOG.debug("Configuring server-side SSLContext session timeout: " + this.getSessionTimeout());
            this.configureSessionContext(context.getServerSessionContext(), this.getSessionTimeout());
        }
        LOG.debug("Configured server-side SSLContext parameters.");   
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation allows for configuration of the need and want settings
     * for client authentication, but ignores the enabled cipher suites
     * and protocols as they are not client and server side specific in an
     * {@code SSLEngine}. Consequently, overriding them here would be a bit odd
     * as the server side specific configuration shouldn't really override a
     * shared client/server configuration option.
     */
    @Override
    protected List<Configurer<SSLEngine>> getSSLEngineConfigurers(SSLContext context) {
        // NOTE: if the super class gets additional shared configuration options beyond
        // cipher suites and protocols, this method needs to address that.
        // As is, we do NOT pass the configurers along for those two settings.
        
        List<Configurer<SSLEngine>> sslEngineConfigurers = new LinkedList<Configurer<SSLEngine>>();
        
        if (this.getClientAuthentication() != null) { 
            
            final ClientAuthentication clientAuthValue = this.getClientAuthentication();
        
            Configurer<SSLEngine> sslEngineConfigurer = new Configurer<SSLEngine>() {
                
                @Override
                public SSLEngine configure(SSLEngine engine) {
                
                    switch (clientAuthValue) {
                    case NONE:
                        engine.setWantClientAuth(false);
                        engine.setNeedClientAuth(false);
                        break;
                    case WANT:
                        engine.setWantClientAuth(true);
                        break;
                    case REQUIRE:
                        engine.setNeedClientAuth(true);
                        break;
                    default:
                        throw new RuntimeCamelException("Unhandled ClientAuthentication enumeration value: " + clientAuthValue);
                    }
                    
                    return engine;
                }
            };
            
            sslEngineConfigurers.add(sslEngineConfigurer);
        }
        
        return sslEngineConfigurers;
    }
    
    @Override
    protected List<Configurer<SSLServerSocket>> getSSLServerSocketFactorySSLServerSocketConfigurers(SSLContext context) {
        List<Configurer<SSLServerSocket>> sslServerSocketConfigurers = 
            super.getSSLServerSocketFactorySSLServerSocketConfigurers(context);
        

        if (this.getClientAuthentication() != null) { 
            
            final ClientAuthentication clientAuthValue = this.getClientAuthentication();
        
            Configurer<SSLServerSocket> sslServerSocketConfigurer = new Configurer<SSLServerSocket>() {
                
                @Override
                public SSLServerSocket configure(SSLServerSocket socket) {
                    
                    switch (clientAuthValue) {
                    case NONE:
                        socket.setWantClientAuth(false);
                        socket.setNeedClientAuth(false);
                        break;
                    case WANT:
                        socket.setWantClientAuth(true);
                        break;
                    case REQUIRE:
                        socket.setNeedClientAuth(true);
                        break;
                    default:
                        throw new RuntimeCamelException("Unhandled ClientAuthentication enumeration value: " + clientAuthValue);
                    }
                    
                    return socket;
                }
            };
            
            sslServerSocketConfigurers.add(sslServerSocketConfigurer);
        }
        
        
        return sslServerSocketConfigurers;
    }

    /**
     * This class has no bearing on {@code SSLSocketFactory} instances and therefore provides no
     * configurers for that purpose.
     */
    @Override
    protected List<Configurer<SSLSocketFactory>> getSSLSocketFactoryConfigurers(SSLContext context) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SSLContextServerParameters [clientAuthentication=");
        builder.append(clientAuthentication);
        builder.append(", getCipherSuites()=");
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
