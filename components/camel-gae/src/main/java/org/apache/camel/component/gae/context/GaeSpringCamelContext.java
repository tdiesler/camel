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
package org.apache.camel.component.gae.context;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ActiveMQUuidGenerator;
import org.apache.camel.impl.JavaUuidGenerator;
import org.apache.camel.spring.SpringCamelContext;

public class GaeSpringCamelContext extends SpringCamelContext {

    @Override
    protected void doStart() throws Exception {
        // JMX not allowed on GAE
        disableJMX();

        if (getUuidGenerator() instanceof ActiveMQUuidGenerator) {
            // use java uuid generator as ActiveMQ uses JDK API which is not allowed on GAE
            setUuidGenerator(new JavaUuidGenerator());
        }

        super.doStart();
    }

    public void setRouteBuilders(List<RouteBuilder> routeBuilders) throws Exception {
        for (RouteBuilder routebuilder : routeBuilders) {
            addRoutes(routebuilder);
        }
    }

    public void setRouteBuilder(RouteBuilder routeBuilder) throws Exception {
        addRoutes(routeBuilder);
    }
    
}
