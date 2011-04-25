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
package org.apache.camel.core.xml.util.jsse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.util.jsse.ClientAuthentication;
import org.apache.camel.util.jsse.SSLContextServerParameters;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
public abstract class AbstractSSLContextServerParametersFactoryBean extends AbstractBaseSSLContextParametersFactoryBean<SSLContextServerParameters> {

    @XmlAttribute
    protected ClientAuthenticationDefinition clientAuthentication;

    public ClientAuthenticationDefinition getClientAuthentication() {
        return clientAuthentication;
    }

    public void setClientAuthentication(ClientAuthenticationDefinition clientAuthentication) {
        this.clientAuthentication = clientAuthentication;
    }

    @Override
    protected SSLContextServerParameters createInstance() {
        SSLContextServerParameters newInstance = new SSLContextServerParameters();
        
        if (clientAuthentication != null) {
            ClientAuthentication clientAuthenticationInstance = ClientAuthentication.valueOf(clientAuthentication.name());
            newInstance.setClientAuthentication(clientAuthenticationInstance);
        }
        
        return newInstance;
    }

    @Override
    public Class<SSLContextServerParameters> getObjectType() {
        return SSLContextServerParameters.class;
    }
}
