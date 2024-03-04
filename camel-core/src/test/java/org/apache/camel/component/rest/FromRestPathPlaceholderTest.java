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
package org.apache.camel.component.rest;

import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.CollectionFormat;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.junit.Test;

public class FromRestPathPlaceholderTest extends ContextTestSupport {
    
    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("dummy-rest", new DummyRestConsumerFactory());
        return jndi;
    }
   

    protected int getExpectedNumberOfRoutes() {
        // routes are inlined
        return 2;
    }

    @Test
    public void testPlaceholder() throws Exception {
        assertEquals(getExpectedNumberOfRoutes(), context.getRoutes().size());

        RestDefinition rest = context.getRestDefinitions().get(0);
        assertNotNull(rest);
        assertEquals("/say/{{mypath}}", rest.getPath());

        // placeholder should be resolved, so we can find the rest endpoint that is a dummy (via seda)
        assertNotNull(context.hasEndpoint("seda://get-say-hello"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                PropertiesComponent pc = new PropertiesComponent();
                pc.setLocation("classpath:org/apache/camel/component/rest/test.properties");
                context.addComponent("properties", pc);

                restConfiguration().host("localhost");

                rest("/say/{{mypath}}").get().to("direct:hello");

                from("direct:hello").log("Hello");

            }
        };
    }
}
