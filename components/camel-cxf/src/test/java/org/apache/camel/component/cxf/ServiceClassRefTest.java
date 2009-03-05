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
package org.apache.camel.component.cxf;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

/**
 * Unit test to verify using '#' notation to reference serviceClass.
 * 
 * @version $Revision$
 */
@ContextConfiguration
public class ServiceClassRefTest extends AbstractJUnit38SpringContextTests {

    @Autowired
    protected CamelContext context;
    
    public void testServiceClassNameCreatedByRefNotation() throws Exception {
        // verify the '#' notation works
        CxfEndpoint endpoint = context.getEndpoint("cxf:bean:fromEndpoint", CxfEndpoint.class);
        assertEquals("org.apache.camel.component.cxf.HelloServiceImpl", endpoint.getServiceClass());
        assertEquals(DataFormat.POJO, endpoint.getDataFormat());
        
        // verify values in bean properties are ok
        endpoint = context.getEndpoint("cxf:bean:fromEndpointWithProps", CxfEndpoint.class);
        assertEquals(DataFormat.PAYLOAD, endpoint.getDataFormat());
    }


}
