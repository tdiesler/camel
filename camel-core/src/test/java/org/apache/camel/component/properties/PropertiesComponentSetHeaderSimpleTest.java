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
package org.apache.camel.component.properties;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class PropertiesComponentSetHeaderSimpleTest extends ContextTestSupport {
    
    public void testPropertiesAndSimple() throws Exception {
        getMockEndpoint("mock:result").expectedHeaderReceived("foo", "http://mycoolserver/myapp");

        template.sendBodyAndHeader("direct:start", "Hello World", "app", "myapp");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        PropertiesComponent pc = new PropertiesComponent();
        pc.setCamelContext(context);
        pc.setLocation("classpath:org/apache/camel/component/properties/cheese.properties");
        context.addComponent("properties", pc);

        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .setHeader("foo").simple("{{cheese.server}}/${header.app}")
                    .to("mock:result");
            }
        };
    }
}