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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.processor.idempotent.MessageIdRepository;

/**
 * Unit test for the idempotentRepositoryRef option.
 */
public class FileConsumerIdempotentRefTest extends ContextTestSupport {

    private static boolean invoked;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("myRepo", new MyIdempotentRepository());
        return jndi;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteDirectory("target/idempotent");
        template.sendBodyAndHeader("file://target/idempotent/", "Hello World", FileComponent.HEADER_FILE_NAME, "report.txt");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("file://target/idempotent/?idempotent=true&idempotentRepositoryRef=myRepo&moveNamePrefix=done/").to("mock:result");
            }
        };
    }

    public void testIdempotentRef() throws Exception {
        // consume the file the first time
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        Thread.sleep(1000);

        // reset mock and set new expectations
        mock.reset();
        mock.expectedMessageCount(0);

        // move file back
        File file = new File("target/idempotent/done/report.txt");
        File renamed = new File("target/idempotent/report.txt");
        file = file.getAbsoluteFile();
        file.renameTo(renamed.getAbsoluteFile());

        // should NOT consume the file again, let 2 secs pass to let the consumer try to consume it but it should not
        Thread.sleep(2000);
        assertMockEndpointsSatisfied();

        assertTrue("MyIdempotentRepository should have been invoked", invoked);
    }

    public class MyIdempotentRepository implements MessageIdRepository {

        public boolean contains(String messageId) {
            // will return false 1st time, and true 2nd time
            boolean result = invoked;
            invoked = true;
            assertEquals("report.txt", messageId);
            return result;
        }
    }
    
}