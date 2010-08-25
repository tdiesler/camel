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
package org.apache.camel.spring.util;

import org.apache.camel.Endpoint;
import org.apache.camel.spring.SpringTestSupport;
import org.apache.camel.util.EndpointHelper;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class EndpointHelperTest extends SpringTestSupport {

    @Override
    protected int getExpectedRouteCount() {
        return 0;
    }

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/util/EndpointHelperTest.xml");
    }

    public void testLookupEndpointRegistryId() throws Exception {
        Endpoint foo = context.getEndpoint("ref:foo");
        Endpoint bar = context.getEndpoint("ref:coolbar");

        assertEquals("foo", EndpointHelper.lookupEndpointRegistryId(foo));
        assertEquals("coolbar", EndpointHelper.lookupEndpointRegistryId(bar));
        assertEquals(null, EndpointHelper.lookupEndpointRegistryId(context.getEndpoint("mock:cheese")));
    }

}
