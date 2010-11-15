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
package org.apache.camel.component.ldap;

import java.util.Collection;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.ldap.LdapServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.directory.server.integ.ServerIntegrationUtils.getWiredContext;

@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP")})
@ApplyLdifFiles("org/apache/camel/component/ldap/LdapRouteTest.ldif")
public class LdapRouteTest extends AbstractLdapTestUnit {

    public static LdapServer ldapServer;
    private CamelContext camel;
    private ProducerTemplate template;
    private int port;

    @Before
    public void setup() throws Exception {
        // you can assign port number in the @CreateTransport annotation
        port = ldapServer.getPort();

        LdapContext ctx = getWiredContext(ldapServer);

        SimpleRegistry reg = new SimpleRegistry();
        reg.put("localhost:" + port, ctx);
        camel = new DefaultCamelContext(reg);
        template = camel.createProducerTemplate();
    }

    @After
    public void tearDown() throws Exception {
        camel.stop();
    }

    @Test
    public void testLdapRouteStandard() throws Exception {
        camel.addRoutes(createRouteBuilder("ldap:localhost:" + port + "?base=ou=system"));
        camel.start();

        // START SNIPPET: invoke
        Endpoint endpoint = camel.getEndpoint("direct:start");
        Exchange exchange = endpoint.createExchange();
        // then we set the LDAP filter on the in body
        exchange.getIn().setBody("(!(ou=test1))");

        // now we send the exchange to the endpoint, and receives the response from Camel
        Exchange out = template.send(endpoint, exchange);
        Collection<SearchResult> searchResults = defaultLdapModuleOutAssertions(out);

        Assert.assertFalse(contains("uid=test1,ou=test,ou=system", searchResults));
        Assert.assertTrue(contains("uid=test2,ou=test,ou=system", searchResults));
        Assert.assertTrue(contains("uid=testNoOU,ou=test,ou=system", searchResults));
        Assert.assertTrue(contains("uid=tcruise,ou=actors,ou=system", searchResults));
        // START SNIPPET: invoke
    }

    @Test
    public void testLdapRouteWithPaging() throws Exception {
        camel.addRoutes(createRouteBuilder("ldap:localhost:" + port + "?base=ou=system&pageSize=5"));
        camel.start();

        Endpoint endpoint = camel.getEndpoint("direct:start");
        Exchange exchange = endpoint.createExchange();
        // then we set the LDAP filter on the in body
        exchange.getIn().setBody("(objectClass=*)");

        // now we send the exchange to the endpoint, and receives the response from Camel
        Exchange out = template.send(endpoint, exchange);

        Collection<SearchResult> searchResults = defaultLdapModuleOutAssertions(out);
        Assert.assertEquals(16, searchResults.size());
    }

    @Test
    public void testLdapRouteReturnedAttributes() throws Exception {
        // LDAP servers behave differently when it comes to what attributes are returned
        // by default (Apache Directory server returns all attributes by default)
        // Using the returnedAttributes parameter this behavior can be controlled

        camel.addRoutes(createRouteBuilder("ldap:localhost:" + port + "?base=ou=system&returnedAttributes=uid,cn"));
        camel.start();

        Endpoint endpoint = camel.getEndpoint("direct:start");
        Exchange exchange = endpoint.createExchange();
        // then we set the LDAP filter on the in body
        exchange.getIn().setBody("(uid=tcruise)");

        // now we send the exchange to the endpoint, and receives the response from Camel
        Exchange out = template.send(endpoint, exchange);
        Collection<SearchResult> searchResults = defaultLdapModuleOutAssertions(out);

        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(contains("uid=tcruise,ou=actors,ou=system", searchResults));
        Attributes theOneResultAtts = searchResults.iterator().next().getAttributes();
        Assert.assertEquals("tcruise", theOneResultAtts.get("uid").get());
        Assert.assertEquals("Tom Cruise", theOneResultAtts.get("cn").get());

        // make sure this att is NOT returned anymore 
        Assert.assertNull(theOneResultAtts.get("sn"));
    }

    @SuppressWarnings("unchecked")
    private Collection<SearchResult> defaultLdapModuleOutAssertions(Exchange out) {
        // assertions of the response
        Assert.assertNotNull(out);
        Assert.assertNotNull(out.getOut());
        Collection<SearchResult> data = out.getOut().getBody(Collection.class);
        Assert.assertNotNull("out body could not be converted to a Collection - was: " + out.getOut().getBody(), data);
        return data;
    }

    protected boolean contains(String dn, Collection<SearchResult> results) {
        for (SearchResult result : results) {
            if (result.getNameInNamespace().equals(dn)) {
                return true;
            }
        }

        return false;
    }

    protected RouteBuilder createRouteBuilder(final String ldapEndpointUrl) throws Exception {
        return new RouteBuilder() {
            // START SNIPPET: route
            public void configure() throws Exception {
                from("direct:start").to(ldapEndpointUrl);
            }
            // END SNIPPET: route
        };
    }
}
