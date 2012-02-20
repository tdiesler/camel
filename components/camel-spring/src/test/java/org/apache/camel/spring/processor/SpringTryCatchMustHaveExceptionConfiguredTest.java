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
package org.apache.camel.spring.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.FailedToCreateRouteException;

import static org.apache.camel.spring.processor.SpringTestHelper.createSpringCamelContext;

public class SpringTryCatchMustHaveExceptionConfiguredTest extends ContextTestSupport {

    protected CamelContext createCamelContext() throws Exception {
        try {
            createSpringCamelContext(this, "org/apache/camel/spring/processor/SpringTryCatchMustHaveExceptionConfiguredTest.xml");
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertIsInstanceOf(FailedToCreateRouteException.class, e.getCause());
            assertIsInstanceOf(IllegalArgumentException.class, e.getCause().getCause());
            assertEquals("At least one Exception must be configured to catch", e.getCause().getCause().getMessage());
        }

        // return a working context instead, to let this test pass
        return createSpringCamelContext(this, "org/apache/camel/spring/processor/SpringTryProcessorHandledTest.xml");
    }

    public void testTryCatchMustHaveExceptionConfigured() {
        // noop
    }

}