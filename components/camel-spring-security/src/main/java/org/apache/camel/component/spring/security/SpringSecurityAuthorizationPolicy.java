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
package org.apache.camel.component.spring.security;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.spi.AuthorizationPolicy;
import org.apache.camel.spi.RouteContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.SpringSecurityException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.event.authorization.AuthorizationFailureEvent;
import org.springframework.security.event.authorization.AuthorizedEvent;
import org.springframework.util.Assert;

public class SpringSecurityAuthorizationPolicy implements AuthorizationPolicy, InitializingBean, ApplicationEventPublisherAware {
    private static final transient Log LOG = LogFactory.getLog(SpringSecurityAuthorizationPolicy.class);
    private AccessDecisionManager accessDecisionManager;
    private AuthenticationManager authenticationManager;
    private ApplicationEventPublisher eventPublisher;
    private SpringSecurityAccessPolicy accessPolicy;
    
    private boolean alwaysReauthenticate;
    private boolean useThreadSecurityContext = true;
    

    public Processor wrap(RouteContext routeContext, Processor processor) {
        // wrap the processor with authorizeDelegateProcessor
        return new AuthorizeDelegateProcess(processor); 
    }
    
    protected void beforeProcess(Exchange exchange) throws Exception {
        ConfigAttributeDefinition attributes = accessPolicy.getConfigAttributeDefinition();
        
        try {
            
            Authentication authenticated = authenticateIfRequired(getAuthentication(exchange));
            
            // Attempt authorization with exchange
            try {
                this.accessDecisionManager.decide(authenticated, exchange, attributes);
            } catch (AccessDeniedException accessDeniedException) {
                AuthorizationFailureEvent event = new AuthorizationFailureEvent(exchange, attributes, authenticated,
                        accessDeniedException);
                publishEvent(event);
                throw accessDeniedException;
            }
            publishEvent(new AuthorizedEvent(exchange, attributes, authenticated));
            
        } catch (SpringSecurityException exception) {
            CamelAuthorizationException authorizationException =
                new CamelAuthorizationException("Cannot access the below process", exchange, exception);
            throw authorizationException;
        }
    }
    
    protected Authentication getAuthentication(Exchange exchange) {
        Authentication answer = exchange.getProperty(Exchange.AUTHENTICATION, Authentication.class);
        // try to get it from thread context as a fallback
        if (answer == null && useThreadSecurityContext) {
            answer = SecurityContextHolder.getContext().getAuthentication();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get the authentication from SecurityContextHolder");
            }
        }
        
        return answer;
    }

    private class AuthorizeDelegateProcess extends DelegateProcessor {
        
        AuthorizeDelegateProcess(Processor processor) {
            super(processor);
        }
        
        public void process(Exchange exchange) throws Exception {
            beforeProcess(exchange);
            processNext(exchange);
        }
        
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
        Assert.notNull(this.accessDecisionManager, "An AccessDecisionManager is required");
        Assert.notNull(this.accessPolicy, "The accessPolicy is required");
        
    }
    
    private Authentication authenticateIfRequired(Authentication authentication) {
        
        if (authentication.isAuthenticated() && !alwaysReauthenticate) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Previously Authenticated: " + authentication);
            }
            return authentication;
        }

        authentication = authenticationManager.authenticate(authentication);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully Authenticated: " + authentication);
        }

        return authentication;
    }
    
    private void publishEvent(ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
    
    public AccessDecisionManager getAccessDecisionManager() {
        return accessDecisionManager;
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
        
    }
    
    public void setSpringSecurityAccessPolicy(SpringSecurityAccessPolicy policy) {
        this.accessPolicy = policy;
    }
    
    public SpringSecurityAccessPolicy getSpringSecurityAccessPolicy() {
        return accessPolicy;
    }
    
    public boolean isAlwaysReauthenticate() {
        return alwaysReauthenticate;
    }
    
    public void setAlwaysReauthenticate(boolean alwaysReauthenticate) {
        this.alwaysReauthenticate = alwaysReauthenticate;
    }
    
    public boolean isUseThreadSecurityContext() {
        return useThreadSecurityContext;
    }
    
    public void setUseThreadSecurityContext(boolean useThreadSecurityContext) {
        this.useThreadSecurityContext = useThreadSecurityContext;
    }

    public void setAuthenticationManager(AuthenticationManager newManager) {
        this.authenticationManager = newManager;
    }
    
    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

}
