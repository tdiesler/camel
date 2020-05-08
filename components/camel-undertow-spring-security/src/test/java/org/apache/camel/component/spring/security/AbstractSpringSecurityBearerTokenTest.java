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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.spring.security.keycloak.KeycloakJwtAuthenticationConverter;
import org.apache.camel.component.spring.security.keycloak.KeycloakUsernameSubClaimAdapter;
import org.apache.camel.component.undertow.UndertowComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.BeforeClass;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class AbstractSpringSecurityBearerTokenTest extends CamelTestSupport {

    private static volatile int port;

    private final MockFilter mockFilter =  new MockFilter();

    public MockFilter getMockFilter() {
        return mockFilter;
    }

    @BeforeClass
    public static void initPort() throws Exception {
        port = AvailablePortFinder.getNextAvailable();
    }

    protected static int getPort() {
        return port;
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        context.addComponent("properties", new PropertiesComponent("ref:prop"));

        context.getComponent("undertow", UndertowComponent.class).setSecurityConfiguration(new SpringSecurityConfiguration() {
            @Override
            public Filter getSecurityFilter() {
                return mockFilter;
            }
        });

        return context;
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();

        Properties prop = new Properties();
        prop.setProperty("port", "" + getPort());
        jndi.bind("prop", prop);
        return jndi;
    }

    Jwt createToken(String userName, String role) {
        JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();

        claimsSet.subject("123445667");
        claimsSet.claim("preffered_name", userName);
        claimsSet.audience("resource-server");
        claimsSet.issuer("came-spring-security");

        PlainJWT plainJWT = new PlainJWT(claimsSet.build());

        Map<String, Object> headers = new HashMap();
        headers.put("type", "JWT");
        headers.put("alg", "RS256");
        Map<String, Object> claims = new KeycloakUsernameSubClaimAdapter("preffered_name").convert(claimsSet.getClaims());

        JSONArray roles = new JSONArray();
        roles.appendElement(role);
        JSONObject r = new JSONObject();
        r.put(KeycloakJwtAuthenticationConverter.ROLES, roles);
        claims.put(KeycloakJwtAuthenticationConverter.REALM_ACCESS, new JSONObject(r));

        Jwt retVal = new Jwt(plainJWT.serialize(), Instant.now(), Instant.now().plusSeconds(10), headers, claims);
        return retVal;
    }
}
