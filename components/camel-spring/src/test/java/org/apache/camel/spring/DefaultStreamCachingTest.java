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
package org.apache.camel.spring;

import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DefaultStreamCachingTest extends TestCase {
    
    
    public void testStreamCaching() throws Exception {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] {"org/apache/camel/spring/streamCaching.xml"});
        CamelContext camelContext = appContext.getBean("camelContext", CamelContext.class);
        assertFalse("StreamCaching should not be enabled", camelContext.isStreamCaching());
    }

}
