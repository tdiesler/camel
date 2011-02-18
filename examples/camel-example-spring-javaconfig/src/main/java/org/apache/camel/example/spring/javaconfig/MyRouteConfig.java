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
package org.apache.camel.example.spring.javaconfig;

import java.util.List;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;

import org.apache.camel.osgi.SpringCamelContextFactory;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.javaconfig.Main;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.osgi.context.BundleContextAware;

//START SNIPPET: RouteConfig
/**
 * A simple example router from a file system to an ActiveMQ queue and then to a file system
 *
 * @version 
 */
@Configuration
public class MyRouteConfig extends SingleRouteCamelConfiguration implements InitializingBean, BundleContextAware {
    
    private BundleContext bundleContext;
    
    /**
     * Allow this route to be run as an application
     *
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }
    
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) { 
        this.bundleContext = bundleContext;
    }
    
    
    /**
     * Returns the CamelContext which support OSGi
     */
    @Override
    protected CamelContext createCamelContext() throws Exception {
        SpringCamelContextFactory factory = new SpringCamelContextFactory();
        factory.setApplicationContext(getApplicationContext());
        factory.setBundleContext(getBundleContext());
        return factory.createContext();
    }
    
    @Override
    // setup the ActiveMQ component and register it into the camel context
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        JmsComponent answer = new JmsComponent();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("vm://localhost.spring.javaconfig?marshal=false&broker.persistent=false&broker.useJmx=false");
        answer.setConnectionFactory(connectionFactory);        
        camelContext.addComponent("jms", answer);        
    }

    
    public static class SomeBean {
        public void someMethod(String body) {
            System.out.println("Received: " + body);
        }
    }

    @Bean
    @Override
    // you can confige the route rule with Java DSL here
    public RouteBuilder route() {
        return new RouteBuilder() {
            public void configure() {
                // populate the message queue with some messages
                from("file:src/data?noop=true").
                        to("jms:test.MyQueue");

                from("jms:test.MyQueue").
                        to("file://target/test?noop=true");

                // set up a listener on the file component
                from("file://target/test?noop=true").
                        bean(new SomeBean());
            }
        };
    }

    public void afterPropertiesSet() throws Exception {
        // just to make SpringDM happy do nothing here
    }
}
//END SNIPPET: RouteConfig