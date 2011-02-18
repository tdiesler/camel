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
package org.apache.camel.component.cxf.mtom;

import java.net.URL;

import org.apache.camel.cxf.mtom_feature.Hello;
import org.apache.camel.cxf.mtom_feature.HelloService12;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertNotNull;


/**
 * Unit test for exercising MTOM enabled end-to-end router in PAYLOAD mode for SOAP 1.2
 * 
 * @version 
 */
@ContextConfiguration
public class CxfMtomRouterPayloadMode12Test extends CxfMtomRouterPayloadModeTest {
    
    @Override
    protected Object getImpl() {
        return new HelloImpl12();
    }
    
    @Override
    protected Hello getPort() {
        URL wsdl = getClass().getResource("/mtom.wsdl");
        assertNotNull("WSDL is null", wsdl);

        HelloService12 service = new HelloService12(wsdl, HelloService12.SERVICE);
        assertNotNull("Service is null ", service);
        return service.getHelloPort();
    }

}
