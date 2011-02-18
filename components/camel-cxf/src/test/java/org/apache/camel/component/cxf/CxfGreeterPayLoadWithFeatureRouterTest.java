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

import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A unit test for testing a CXF client invoking a CXF server via route 
 * in PAYLOAD mode and with CXF features specified in the Spring config.
 * 
 * @version 
 */
public class CxfGreeterPayLoadWithFeatureRouterTest extends CXFGreeterRouterTest {
    
    @Before
    @Override
    public void setUp() throws Exception {
        setUseRouteBuilder(false);
        super.setUp();
        
        CxfEndpoint endpoint = getMandatoryEndpoint("cxf:bean:serviceEndpoint?dataFormat=PAYLOAD", 
                CxfEndpoint.class);
        
        assertEquals(TestCxfFeature.class, ((CxfSpringEndpoint)endpoint).getBean()
                .getFeatures().get(0).getClass());
        
        assertEquals(DataFormat.PAYLOAD, endpoint.getDataFormat());
    }   

    @Override
    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(
                "org/apache/camel/component/cxf/GreeterEndpointWithFeatureBeans.xml");
    }


}
