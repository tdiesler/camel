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
package org.apache.camel.component.netty.http;

import java.io.IOException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAAS based {@link SecurityAuthenticator} implementation.
 */
public class JAASSecurityAuthenticator implements SecurityAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(JAASSecurityAuthenticator.class);
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Subject login(HttpPrincipal principal) throws LoginException {
        if (ObjectHelper.isEmpty(getName())) {
            throw new IllegalArgumentException("Realm has not been configured on this SecurityAuthenticator: " + this);
        }

        LOG.debug("Login username: {} using realm: {}", principal.getName(), getName());
        LoginContext context = new LoginContext(getName(), new HttpPrincipalCallbackHandler(principal));
        context.login();
        Subject subject = context.getSubject();
        LOG.debug("Login username: {} successful returning Subject: {}", principal.getName(), subject);
        return subject;
    }

    @Override
    public void logout(Subject subject) throws LoginException {
        if (ObjectHelper.isEmpty(getName())) {
            throw new LoginException("Realm has not been configured on this SecurityAuthenticator: " + this);
        }

        String username = "";
        if (!subject.getPrincipals().isEmpty()) {
            username = subject.getPrincipals().iterator().next().getName();
        }
        LOG.debug("Logging out username: {} using realm: {}", username, getName());
        LoginContext context = new LoginContext(getName(), subject);
        context.logout();
        LOG.debug("Logout username: {} successful", username);
    }

    /**
     * {@link CallbackHandler} that provides the username and password.
     */
    private final class HttpPrincipalCallbackHandler implements CallbackHandler {

        private final HttpPrincipal principal;

        private HttpPrincipalCallbackHandler(HttpPrincipal principal) {
            this.principal = principal;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                LOG.trace("Callback {}", callback);
                if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    LOG.trace("Setting password on callback {}", pc);
                    pc.setPassword(principal.getPassword().toCharArray());
                } else if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    LOG.trace("Setting username on callback {}", nc);
                    nc.setName(principal.getName());
                }
            }
        }
    }

}
