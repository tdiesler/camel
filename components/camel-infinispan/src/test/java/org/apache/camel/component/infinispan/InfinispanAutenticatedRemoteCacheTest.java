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

package org.apache.camel.component.infinispan;

import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assume.assumeTrue;

public class InfinispanAutenticatedRemoteCacheTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(InfinispanAutenticatedRemoteCacheTest.class);

    private static final String MESSAGE =
                      "This test requires a running infinispan server with authentication enabled.\n"
                    + "To enable this test run mvn with system property '-Dsecure=true'. It's possible\n"
                    + "to provide all the information required to succesfully authenticate to an infinispan\n"
                    + "server through the following  system properties:\n\n"
                    + "-Dusername       - (mandatory) the username to access the infinispan instance\n"
                    + "-Dpassword       - (mandatory) the password to access the infinispan instance\n"
                    + "-DserverName     - the security server name to access the infinispan instance. Default: 'infinispan'\n"
                    + "-Dhost           - the hostname/ip  of the infinispan instance. Default: 'localhost'\n"
                    + "-Dport           - the port of the infinispan instance. Default: '11222'\n"
                    + "-Dsecurity       - enable client authentication. Default: 'false'\n"
                    + "-DsaslMechanism  - the SASL Mechanism to access the infinispan instance. Default: 'DIGEST-MD5'\n"
                    + "-Drealm          -  the security realm to access the infinispan instance. Default: 'default'\n"
                    + "-DcacheName      -  the cache name to use, if doesn't exists, it will be created. Default: 'myCache'\n\n\n"
                    + "Example:\n\n"
                    + "mvn clean test -Dtest=InfinispanAutenticatedRemoteCacheTest -Dsecure=true -Dusername=user -Dpassword=MyPaSs04 ";


    private static final String COMMAND_VALUE = "commandValue";
    private static final String COMMAND_KEY = "commandKey1";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERY = "password";
    private static final String SERVER_NAME_PROPERTY = "servername";
    private static final String HOST_PROPERTY = "host";
    private static final String PORT_PROPERTY = "port";
    private static final String SASL_MECHANISM_PROPERTY = "saslMechanism";
    private static final String REALM_PROPERTY = "realm";
    private static final String CACHE_NAME_PROPERTY = "cacheName";
    private static final String SECURE_PROPERTY = "secure";

    private RemoteCacheManager remoteCacheManager;

    private String username;
    private String password;
    private String serverName;
    private String host;
    private int port;
    private String saslMechanism;
    private String realm;
    private String cacheName;
    private boolean secure;

    public String getInfispanUrl() {
        return String.format(
                "%s:%d",
                 host, port);
    }

    public RemoteCacheManager createAndGetDefaultCache() {
        ConfigurationBuilder clientBuilder = new ConfigurationBuilder();
        clientBuilder
                .addServer()
                .host(host)
                .port(port)
                .security()
                .authentication()
                .username(username)
                .password(password)
                .serverName(serverName)
                .saslMechanism(saslMechanism)
                .realm(realm);

        remoteCacheManager = new RemoteCacheManager(clientBuilder.build());

        remoteCacheManager.administration().getOrCreateCache(cacheName, "org.infinispan.DIST_SYNC");

        return remoteCacheManager;
    }


    @Before
    public void doPreSetup() {
        username      =   Optional.ofNullable(System.getProperty(USERNAME_PROPERTY))
                .orElseThrow(()->new RuntimeException("username cannot be null"));

        password      =   Optional.ofNullable(System.getProperty(PASSWORD_PROPERY))
                .orElseThrow(()->new RuntimeException("password cannot be null"));

        serverName    =   Optional.ofNullable(System.getProperty(SERVER_NAME_PROPERTY))
                .orElse("infinispan");

        host          =   Optional.ofNullable(System.getProperty(HOST_PROPERTY))
                .orElse("localhost");

        port          =  Integer.parseInt(Optional.ofNullable(System.getProperty(PORT_PROPERTY))
                .orElse("11222"));

        saslMechanism =   Optional.ofNullable(System.getProperty(SASL_MECHANISM_PROPERTY))
                .orElse("DIGEST-MD5");

        realm         =   Optional.ofNullable(System.getProperty(REALM_PROPERTY))
                .orElse("default");

        cacheName     =   Optional.ofNullable(System.getProperty(CACHE_NAME_PROPERTY))
                .orElse("myCache");

        secure        =  Boolean.parseBoolean(Optional.ofNullable(System.getProperty(SECURE_PROPERTY))
                .orElse("false"));

        remoteCacheManager = createAndGetDefaultCache();
    }

    @Test
    public void testUriCommandOption() {
        assumeTrue(MESSAGE, secure);

        template.send("direct:put", exchangePut -> {
            exchangePut.getIn().setHeader(InfinispanConstants.KEY, COMMAND_KEY);
            exchangePut.getIn().setHeader(InfinispanConstants.VALUE, COMMAND_VALUE);
        });

        Exchange exchange;
        exchange = template.send("direct:get", exchangeGet -> {
            exchangeGet.getIn().setHeader(InfinispanConstants.KEY, COMMAND_KEY);
        });
        String resultGet = exchange.getIn().getBody(String.class);
        assertEquals(COMMAND_VALUE, resultGet);
    }

    @Test
    public void testUriWrongPassword() {
        assumeTrue(MESSAGE, secure);
        Exchange exchange = template.send("direct:putWrongAuth", exchangePut -> {
            exchangePut.getIn().setHeader(InfinispanConstants.KEY, COMMAND_KEY);
            exchangePut.getIn().setHeader(InfinispanConstants.VALUE, COMMAND_VALUE);
        });

        assertNotNull(exchange.getException());
        assertNotNull(exchange.getException().getCause());
        assertNotNull(exchange.getException().getCause().getMessage());
        assertTrue(exchange.getException().getCause().getMessage().contains("ELY05055: Authentication rejected"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:put")
                        .to("infinispan:" + cacheName + "?hosts=" + getInfispanUrl()
                                + "&operation=PUT"
                                + "&username=" + username
                                + "&password=" + password
                                + "&secure=" + secure
                                + "&saslMechanism=RAW(" + saslMechanism + ")"
                                + "&securityRealm=" + realm
                                + "&securityServerName=" + serverName);

                from("direct:get")
                        .to("infinispan:" + cacheName + "?hosts=" + getInfispanUrl()
                                + "&operation=GET"
                                + "&username=" + username
                                + "&password=" + password
                                + "&secure=" + secure
                                + "&saslMechanism=RAW(" + saslMechanism + ")"
                                + "&securityRealm=" + realm
                                + "&securityServerName=" + serverName);

                from("direct:putWrongAuth")
                        .to("infinispan:default?hosts=" + getInfispanUrl()
                                + "&operation=PUT"
                                + "&username=" + username
                                // set a wrong random password
                                + "&password=" + randomAlphanumeric(10)
                                + "&secure=" + secure
                                + "&saslMechanism=RAW(" + saslMechanism + ")"
                                + "&securityRealm=" + realm
                                + "&securityServerName=" + serverName);
            }
        };
    }
}
