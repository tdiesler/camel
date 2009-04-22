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
package org.apache.camel.impl;

import junit.framework.TestCase;
import org.apache.camel.Component;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.BeanComponent;

/**
 * @version $Revision$
 */
public class DefaultCamelContextTest extends TestCase {

    public void testAutoCreateComponentsOn() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        Component component = ctx.getComponent("bean");
        assertNotNull(component);
        assertEquals(component.getClass(), BeanComponent.class);
    }

    public void testAutoCreateComponentsOff() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.setAutoCreateComponents(false);
        Component component = ctx.getComponent("bean");
        assertNull(component);
    }
    
    public void testRestartCamelContext() throws Exception {
        DefaultCamelContext ctx = new DefaultCamelContext();
        ctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:endpointA").to("mock:endpointB");                
            }            
        });
        ctx.start();
        assertEquals("Should have one RouteService", ctx.getRoutes().size(), 1);
        String routeString = ctx.getRoutes().toString();
        ctx.stop();
        assertEquals("Should have one RouteService", ctx.getRoutes().size(), 1);        
        ctx.start();
        assertEquals("Should have one RouteService", ctx.getRoutes().size(), 1);
        assertEquals("The RouteString should be same", routeString, ctx.getRoutes().toString());
        ctx.stop();
        assertEquals("Should have one RouteService", ctx.getRoutes().size(), 1);       
                
    }


}
