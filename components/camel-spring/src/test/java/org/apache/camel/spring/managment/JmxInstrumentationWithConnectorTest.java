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
package org.apache.camel.spring.managment;

import org.apache.camel.management.DefaultInstrumentationAgent;
import org.apache.camel.spi.InstrumentationAgent;
import org.apache.camel.spring.EndpointReferenceTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JmxInstrumentationWithConnectorTest extends EndpointReferenceTest {

    public void testJmxConfiguration() throws Exception {
        InstrumentationAgent agent = getMandatoryBean(DefaultInstrumentationAgent.class, "agent");
        assertNotNull("SpringInstrumentationAgent must be configured for JMX support", agent);
        assertNotNull("MBeanServer must be configured for JMX support", agent.getMBeanServer());
        assertEquals("org.apache.camel.test", agent.getMBeanServer().getDefaultDomain());
    }

    @Override
    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/management/jmxInstrumentationWithConnector.xml");
    }

    public void testGetContext() {
        assertNotNull(this.applicationContext.getBean("camel"));
    }
}
