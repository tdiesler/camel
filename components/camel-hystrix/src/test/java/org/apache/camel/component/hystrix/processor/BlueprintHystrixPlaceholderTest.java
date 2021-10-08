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
package org.apache.camel.component.hystrix.processor;

import org.apache.camel.model.HystrixConfigurationDefinition;
import org.apache.camel.model.HystrixDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

import org.junit.Assert;
import org.junit.Test;

public class BlueprintHystrixPlaceholderTest extends CamelBlueprintTestSupport {

    @Override
    protected String getBlueprintDescriptor() {
        return "org/apache/camel/component/hystrix/processor/BlueprintHystrixPlaceholderTest.xml";
    }

    @Test
    public void testHystrix() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");
        getMockEndpoint("mock:result").expectedPropertyReceived(HystrixConstants.HYSTRIX_RESPONSE_SUCCESSFUL_EXECUTION, true);
        getMockEndpoint("mock:result").expectedPropertyReceived(HystrixConstants.HYSTRIX_RESPONSE_FROM_FALLBACK, false);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        RouteDefinition routeDefinition = context.getRouteDefinition("hystrix-route");
        HystrixDefinition hystrixDefinition = HystrixHelper.findHystrixDefinition(routeDefinition);

        Assert.assertNotNull(hystrixDefinition);

        HystrixProcessorFactory factory = new HystrixProcessorFactory();
        HystrixConfigurationDefinition config = factory.buildHystrixConfiguration(context, hystrixDefinition);

        Assert.assertEquals("4", config.getCorePoolSize());
        Assert.assertEquals("9999", config.getCircuitBreakerSleepWindowInMilliseconds());
        Assert.assertEquals("4999", config.getExecutionTimeoutInMilliseconds());
    }
}
