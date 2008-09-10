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
package org.apache.camel.component.jms.tx;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringTestSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * To demonstrate the TransactionErrorHandlerBuilder configured in Spring XML.
 */
public class TransactionErrorHandlerBuilderAsSpringBeanTest extends SpringTestSupport {

    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(
            "/org/apache/camel/component/jms/tx/TransactionErrorHandlerBuilderAsSpringBeanTest.xml");
    }

    protected int getExpectedRouteCount() {
        return 1;
    }

    public void testTransactionSuccess() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Bye World");
        // success at 3rd attempt
        mock.message(0).header("count").isEqualTo(3);

        template.sendBody("activemq:queue:okay", "Hello World");

        mock.assertIsSatisfied();
    }

    public static class MyProcessor implements Processor {
        private int count;

        public void process(Exchange exchange) throws Exception {
            if (++count <= 2) {
                throw new IllegalArgumentException("Forced Exception number " + count + ", please retry");
            }
            exchange.getIn().setBody("Bye World");
            exchange.getIn().setHeader("count", count);
        }
    }

}