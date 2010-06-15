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
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * Test the ftps component over SSL (implicit) with client authentication
 * 
 * @version $Revision$
 * @author muellerc
 */
public class FileToFtpsImplicitSSLWithClientAuthTest extends FtpsServerImplicitSSLWithClientAuthTestSupport {
    
    private String getFtpUrl() {
        return "ftps://admin@localhost:" + getPort() + "/tmp2/camel?password=admin&consumer.initialDelay=5000&disableSecureDataChannelDefaults=true"
                + "&securityProtocol=SSL&isImplicit=true&ftpClient.keyStore.file=./src/test/resources/server.jks&ftpClient.keyStore.type=JKS"
                + "&ftpClient.keyStore.algorithm=SunX509&ftpClient.keyStore.password=password&ftpClient.keyStore.keyPassword=password";
    }
    
    @Test
    public void testFromFileToFtp() throws Exception {
        // some platforms cannot test SSL
        if (!canTest) {
            return;
        }

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(getFtpUrl()).to("mock:result");
                from("file:src/main/data?noop=true&consumer.delay=5000").to(getFtpUrl());
            }
        };
    }
}