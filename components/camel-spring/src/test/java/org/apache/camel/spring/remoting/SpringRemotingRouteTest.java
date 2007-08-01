/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.spring.remoting;

import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision: 520220 $
 */
public class SpringRemotingRouteTest extends TestCase {
    public void testPojoRoutes() throws Exception {

        ClassPathXmlApplicationContext spring = new ClassPathXmlApplicationContext("org/apache/camel/spring/remoting/spring.xml");
        spring.getBean("serviceExporter");
        CamelContext camelContext = SpringCamelContext.springCamelContext(spring);

        // START SNIPPET: invoke
        ISay proxy = (ISay) spring.getBean("sayProxy");
        String rc = proxy.say();
        assertEquals("Hello", rc);
        // END SNIPPET: invoke

        camelContext.stop();
        spring.destroy();
    }
}
