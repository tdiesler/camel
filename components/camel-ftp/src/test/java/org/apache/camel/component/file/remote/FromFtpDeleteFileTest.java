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

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Unit test to test delete option.
 */
public class FromFtpDeleteFileTest extends FtpServerTestSupport {

    private String getFtpUrl() {
        return "ftp://admin@localhost:" + getPort() + "/deletefile?password=admin&binary=false&delete=true";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        prepareFtpServer();
    }

    private void prepareFtpServer() throws Exception {
        // prepares the FTP Server by creating a file on the server that we want to unit
        // test that we can pool and store as a local file
        Endpoint endpoint = context.getEndpoint(getFtpUrl());
        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setBody("Hello World this file will be deleted");
        exchange.getIn().setHeader(FileComponent.HEADER_FILE_NAME, "hello.txt");
        Producer producer = endpoint.createProducer();
        producer.start();
        producer.process(exchange);
        producer.stop();

        // assert file is created
        File file = new File("./res/home/deletefile/hello.txt");
        file = file.getAbsoluteFile();
        assertTrue("The file should exists", file.exists());
    }

    public void testPollFileAndShouldBeDeleted() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello World this file will be deleted");

        mock.assertIsSatisfied();

        Thread.sleep(500);

        // assert the file is deleted
        File file = new File("./res/home/deletefile/hello.txt");
        file = file.getAbsoluteFile();
        assertFalse("The file should have been deleted", file.exists());
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(getFtpUrl()).to("mock:result");
            }
        };
    }

}
