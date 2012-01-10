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
package org.apache.camel.component.file;

import java.io.File;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;

/**
 * Unit tests for {@link AntPathMatcherGenericFileFilter}.
 */
public class AntPathMatcherGenericFileFilterTest extends ContextTestSupport {

    @Override
    protected void setUp() throws Exception {
        deleteDirectory("target/files");
        super.setUp();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("filter", new AntPathMatcherGenericFileFilter<File>("**/c*"));
        return jndi;
    }

    public void testInclude() throws Exception {

        template.sendBodyAndHeader("file://target/files/ant-path-1/x/y/z", "Hello World", Exchange.FILE_NAME, "report.txt");

        MockEndpoint mock = getMockEndpoint("mock:result1");
        mock.expectedBodiesReceived("Hello World");

        assertMockEndpointsSatisfied();
        oneExchangeDone.matchesMockWaitTime();
    }

    public void testExclude() throws Exception {

        template.sendBodyAndHeader("file://target/files/ant-path-2/x/y/z", "Hello World 1", Exchange.FILE_NAME, "report.bak");
        template.sendBodyAndHeader("file://target/files/ant-path-2/x/y/z", "Hello World 2", Exchange.FILE_NAME, "report.txt");

        MockEndpoint mock = getMockEndpoint("mock:result2");
        mock.expectedBodiesReceived("Hello World 2");

        assertMockEndpointsSatisfied();
        oneExchangeDone.matchesMockWaitTime();
    }

    public void testIncludesAndExcludes() throws Exception {

        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 1", Exchange.FILE_NAME, "a.pdf");
        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 2", Exchange.FILE_NAME, "m.pdf");
        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 3", Exchange.FILE_NAME, "b.txt");
        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 4", Exchange.FILE_NAME, "m.txt");
        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 5", Exchange.FILE_NAME, "b.bak");
        template.sendBodyAndHeader("file://target/files/ant-path-3/x/y/z", "Hello World 6", Exchange.FILE_NAME, "m.bak");

        MockEndpoint mock = getMockEndpoint("mock:result3");
        mock.expectedBodiesReceived("Hello World 2", "Hello World 4");

        assertMockEndpointsSatisfied();
        oneExchangeDone.matchesMockWaitTime();
    }

    public void testIncludesAndExcludesAndFilter() throws Exception {

        template.sendBodyAndHeader("file://target/files/ant-path-4/x/y/z", "Hello World 1", Exchange.FILE_NAME, "a.txt");
        template.sendBodyAndHeader("file://target/files/ant-path-4/x/y/z", "Hello World 2", Exchange.FILE_NAME, "b.txt");
        template.sendBodyAndHeader("file://target/files/ant-path-4/x/y/z", "Hello World 3", Exchange.FILE_NAME, "c.txt");

        MockEndpoint mock = getMockEndpoint("mock:result4");
        mock.expectedBodiesReceived("Hello World 3");

        assertMockEndpointsSatisfied();
        oneExchangeDone.matchesMockWaitTime();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {

                from("file://target/files/ant-path-1?recursive=true&antInclude=**/*.txt").convertBodyTo(String.class).to("mock:result1");

                from("file://target/files/ant-path-2?recursive=true&antExclude=**/*.bak").convertBodyTo(String.class).to("mock:result2");

                from("file://target/files/ant-path-3?recursive=true&antInclude=**/*.pdf,**/*.txt&antExclude=**/a*,**/b*").convertBodyTo(String.class).to("mock:result3");

                from("file://target/files/ant-path-4?recursive=true&antInclude=**/*.txt&antExclude=**/a*&filter=#filter").convertBodyTo(String.class).to("mock:result4");
            }
        };
    }
}
