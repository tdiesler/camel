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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A representation of configuration options for creating and loading a
 * {@link KeyStore} instance.
 */
public class KeyStoreParameters {

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreParameters.class);

    /**
     * The optional type of the key store to load. See Appendix A in the 
     * <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#KeyStore">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a> for more information on standard names.
     */
    protected String type;
    
    /**
     * The optional password for reading/opening/verifying the key store.
     */
    protected String password;
    
    /**
     * The optional provider identifier for instantiating the key store.
     */
    protected String provider;
    
    /**
     * The optional file path, class path resource, or URL of the resource
     * used to load the key store.
     */
    protected String resource;

    /**
     * @see #setType(String)
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the key store to create and load. See Appendix A in the
     * <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#KeyStore"
     * >Java Cryptography Architecture Standard Algorithm Name
     * Documentation</a> for more information on standard names.
     * 
     * @param value the key store type identifier (may be {@code null})
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * @see #getPassword()
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the optional password for reading/opening/verifying the key store.
     * 
     * @param value the password value (may be {@code null})
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * @see #setProvider(String)
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the optional provider identifier for instantiating the key store.
     *
     * @param value the provider identifier (may be {@code null})
     *
     * @see Security#getProviders()
     */
    public void setProvider(String value) {
        this.provider = value;
    }

    /**
     * @see #getResource()
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the optional file path, class path resource, or URL of the resource
     * used to load the key store.
     * 
     * @param value the resource (may be {@code null})
     */
    public void setResource(String value) {
        this.resource = value;
    }

    /**
     * Creates a {@link KeyStoreParameters} instance based off of the configuration state
     * of this instance. If {@link #getType()} returns {@code null}, the default
     * key store type is loaded, otherwise the type will be of that specified.
     * <p/>
     * The created instance will always be loaded, but if the type requires an
     * input stream and {@link #getResource()} returns {@code null}, the
     * instance will be empty. The loading of the resource, if not {@code null},
     * is attempted by treating the resource as a file path, a class path
     * resource, and a URL in that order. An exception is thrown if the resource
     * cannot be resolved to readable input stream using any of the above
     * methods.
     * 
     * @return a configured and loaded key store
     * @throws GeneralSecurityException if there is an error creating an instance
     *             with the given configuration
     * @throws IOException if there is an error resolving the configured
     *             resource to an input stream
     */
    public KeyStore createKeyStore() throws GeneralSecurityException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating KeyStore instance from KeyStoreParameters: " + this);
        }

        String ksType = this.type;
        if (ksType == null) {
            ksType = KeyStore.getDefaultType();
        }

        char[] ksPassword = null;
        if (this.password != null) {
            ksPassword = this.password.toCharArray();
        }

        KeyStore ks = null;
        if (this.provider == null) {
            ks = KeyStore.getInstance(ksType);
        } else {
            ks = KeyStore.getInstance(ksType, this.provider);
        }

        if (this.resource == null) {
            ks.load(null, ksPassword);
        } else {
            InputStream is = null;

            try {
                is = new FileInputStream(this.resource);
            } catch (FileNotFoundException e) {
                LOG.debug("Could not open resource as a file, trying as class path resource.", e);
            }

            if (is == null) {
                is = this.getClass().getResourceAsStream(this.resource);
                if (is == null) {
                    LOG.debug("Could not open resource as a class path resource using the classloader "
                              + this.getClass().getClassLoader() 
                              + ". Trying as a class path resource with the TCCL (if set).");
                }
            }
            
            if (is == null && Thread.currentThread().getContextClassLoader() != null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.resource);
                if (is == null) {
                    LOG.debug("Could not open resource as a class path resource using the TCCL "
                              + Thread.currentThread().getContextClassLoader() + ".  Trying as a URL.");
                }
            }

            if (is == null) {
                try {
                    is = new URL(this.resource).openStream();
                } catch (IOException e) {
                    LOG.debug("Could not open resource as a URL.", e);
                }
            }

            if (is == null) {
                throw new IOException("Could not open " + this.resource + " as a file, class path resource, or URL.");
            }

            ks.load(is, ksPassword);
        }

        return ks;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("KeyStoreParameters [type=");
        builder.append(type);
        builder.append(", password=");
        builder.append(password);
        builder.append(", provider=");
        builder.append(provider);
        builder.append(", resource=");
        builder.append(resource);
        builder.append("]");
        return builder.toString();
    }
}
