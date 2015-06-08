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

import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * @version 
 */
public class FtpSimpleConsumeStreamingWithMultipleFilesTest extends FtpServerTestSupport {

    @Test
    public void testFtpSimpleConsumeAbsolute() throws Exception {
        if (!canTest()) {
            return;
        }

        String expected = "Hello World";
        String expected2 = "Goodbye World";

        // create file using regular file

        // FTP Server does not support absolute path, so lets simulate it
        String path = FTP_ROOT_DIR + "/tmp/mytemp";
        template.sendBodyAndHeader("file:" + path, expected, Exchange.FILE_NAME, "hello.txt");
        template.sendBodyAndHeader("file:" + path, expected2, Exchange.FILE_NAME, "goodbye.txt");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.expectedBodiesReceivedInAnyOrder(expected, expected2);

        context.startRoute("foo");

        assertMockEndpointsSatisfied();

        GenericFile<?> remoteFile1 = mock.getExchanges().get(0).getIn().getBody(GenericFile.class);
        GenericFile<?> remoteFile2 = mock.getExchanges().get(1).getIn().getBody(GenericFile.class);
        assertTrue(remoteFile1.getBody() instanceof InputStream);
        assertTrue(remoteFile2.getBody() instanceof InputStream);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("ftp://localhost:" + getPort() + "/tmp/mytemp?username=admin&password=admin&delay=10s&disconnect=true&streamDownload=true")
                    .routeId("foo").noAutoStartup()
                    .to("mock:result");
            }
        };
    }
}
