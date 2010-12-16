/**
 * Copyright (C) 2010 FuseSource, Corp. All rights reserved.
 * http://fusesource.com
 *
 * The software in this package is published under the terms of the CDDL license
 * a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.camel.mytest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.language.XPathExpression;
import org.fusesource.camel.test.osgi.OSGiCamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * @version $Revision$
 */
@RunWith(JUnit4TestRunner.class)
public class MyOsgiTest extends OSGiCamelTestSupport {

    @Override
    public void setupSettings() {
        setLoggingLevel("TRACE");
        setUseEquinox(false);
    }

    @Test
    public void testXPathAssertion() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(1);

        XPathExpression xpath = new XPathExpression("/foo = 'bar'");
        xpath.setResultType(Boolean.class);
        result.allMessages().body().matches(xpath);

        template.sendBody("direct:start", "<foo>bar</foo>");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:result");
            }
        };
    }

}
