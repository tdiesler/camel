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
package org.apache.camel.component.file.remote;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFileExchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class FtpConsumerMultipleDirectoriesTest extends FtpServerTestSupport {

    private String getFtpUrl() {
        return "ftp://admin@localhost:" + getPort() + "/multidir/?password=admin&recursive=true&consumer.delay=5000&sortBy=file:path";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deleteDirectory(FTP_ROOT_DIR + "multidir");
        prepareFtpServer();
    }

    private void prepareFtpServer() throws Exception {
        sendFile(getFtpUrl(), "Bye World", "bye.txt");
        sendFile(getFtpUrl(), "Hello World", "sub/hello.txt");
        sendFile(getFtpUrl(), "Godday World", "sub/sub2/godday.txt");
    }

    @Test
    public void testMultiDir() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Bye World", "Hello World", "Godday World");

        assertMockEndpointsSatisfied();

        GenericFileExchange exchange = (GenericFileExchange) mock.getExchanges().get(0);
        RemoteFile file = (RemoteFile) exchange.getGenericFile();
        assertDirectoryEquals("multidir/bye.txt", file.getAbsoluteFilePath());
        assertDirectoryEquals("bye.txt", file.getRelativeFilePath());
        assertEquals("bye.txt", file.getFileName());

        exchange = (GenericFileExchange) mock.getExchanges().get(1);
        file = (RemoteFile) exchange.getGenericFile();
        assertDirectoryEquals("multidir/sub/hello.txt", file.getAbsoluteFilePath());
        assertDirectoryEquals("sub/hello.txt", file.getRelativeFilePath());
        assertEquals("hello.txt", file.getFileName());

        exchange = (GenericFileExchange) mock.getExchanges().get(2);
        file = (RemoteFile) exchange.getGenericFile();
        assertDirectoryEquals("multidir/sub/sub2/godday.txt", file.getAbsoluteFilePath());
        assertDirectoryEquals("sub/sub2/godday.txt", file.getRelativeFilePath());
        assertEquals("godday.txt", file.getFileName());
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(getFtpUrl()).to("mock:result");
            }
        };
    }
}