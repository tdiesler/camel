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
package org.apache.camel.management;

import javax.management.ObjectName;

/**
 * @version $Revision$
 */
public class JmxInstrumentationUsingPlatformMBSTest extends JmxInstrumentationUsingPropertiesTest {

    @Override
    protected void setUp() throws Exception {
        System.setProperty(InstrumentationAgentImpl.SYSTEM_PROPERTY_JMX_USE_PLATFORM_MBS, "True");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        // restore environment to original state
        System.setProperty(InstrumentationAgentImpl.SYSTEM_PROPERTY_JMX_USE_PLATFORM_MBS, "");
        super.tearDown();
    }

    @Override
    public void testMBeanServerType() throws Exception {
        assertNotNull(iAgent.getMBeanServer().getMBeanInfo(
                new ObjectName("java.lang:type=OperatingSystem")));
    }

}
