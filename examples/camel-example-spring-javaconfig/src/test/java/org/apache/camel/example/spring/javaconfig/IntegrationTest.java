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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.javaconfig.Main;

/**
 * @version $Revision$
 */
public class IntegrationTest extends TestCase {
    
    public void testCamelRulesDeployCorrectlyInSpring() throws Exception {
        // let's boot up the Spring application context for 2 seconds to check that it works OK
        Main.main("-duration", "2s", "-bp", "org.apache.camel.example.spring.javaconfig");
    }
    
    public void testStartApplicationContext() throws Exception {
        // test to boot up the application context from spring configuration
        ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
        String[] names = context.getBeanNamesForType(CamelContext.class);
        assertEquals("There should be a camel context ", 1, names.length);
        CamelContext camelContext = (CamelContext) context.getBean(names[0]);
        assertNotNull(camelContext);
        Thread.sleep(2000);
    }
}
