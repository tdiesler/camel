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
import org.apache.camel.processor.FaultRouteTest;
import static org.apache.camel.spring.processor.SpringTestHelper.createSpringCamelContext;

/**
 * The spring context test for the FaultRoute
 */
public class SpringFaultRouteTest extends FaultRouteTest {
	@Override
    public void testWithThrowFaultMessageUnhandled() throws Exception {
		// TODO: disable test until support for errorHandler is added to xml dsl
    }

	@Override
    public void testWithHandleFaultMessage() throws Exception {
		// TODO: disable test until support for errorHandler is added to xml dsl
    }
    
    protected CamelContext createCamelContext() throws Exception {
        return createSpringCamelContext(this, "org/apache/camel/spring/processor/faultRoute.xml");
    }
}
