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
package org.apache.camel.example.cdi;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.Uri;

/**
 * Configures all our Camel components, endpoints and beans and create the Camel routes
 */
@ApplicationScoped
@Startup
public class MyRouteConfig {

    @Inject
    private CamelContext camelContext;

    @Inject
    //@Uri("activemq:test.MyQueue")
    @Uri("file://target/testdata/queue")
    private Endpoint queueEndpoint;

    @Inject
    @Uri("file://target/testdata/result?noop=true")
    private Endpoint resultEndpoint;

    @Produces
    public RouteBuilder createRoutes() {
        return new RouteBuilder() {
            public void configure() {
                // you can configure the route rule with Java DSL here

                // populate the message queue with some messages
                from("file:src/data?noop=true").
                        to(queueEndpoint);

                // consume from message queue to a result endpoint and process with a bean
                from(queueEndpoint).
                        to(resultEndpoint).
                        bean(new SomeBean());
            }
        };
    }

    /**
     * Configure ActiveMQ endpoints
     */
    @Named("activemq")
    public ActiveMQComponent createActiveMQComponent() {
        ActiveMQComponent answer = new ActiveMQComponent();
        answer.setBrokerURL("vm://localhost.cdi?marshal=false&broker.persistent=false&broker.useJmx=false");
        return answer;
    }

    /**
     * TODO can we avoid this bit and get CDI to automatically create a CamelContext and add its routes?
     */
    @PostConstruct
    public void start() throws Exception {
        camelContext.addRoutes(createRoutes());
    }

    public Endpoint getResultEndpoint() {
        return resultEndpoint;
    }
}
