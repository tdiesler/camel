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
package org.apache.camel.itest.osgi;

import org.apache.camel.CamelContext;
import org.apache.camel.osgi.CamelContextFactory;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;

public class OSGiIntegrationTestSupport extends CamelTestSupport {
    private static final transient Log LOG = LogFactory.getLog(OSGiIntegrationTestSupport.class);
    @Inject
    protected BundleContext bundleContext;
            
    @Before
    public void setUp() throws Exception {
        super.setUp();        
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected CamelContext createCamelContext() throws Exception {
        CamelContextFactory factory = new CamelContextFactory();
        factory.setBundleContext(bundleContext);
        LOG.info("Get the bundleContext is " + bundleContext);
        return factory.createContext();
    }
    
    @Configuration
    public static Option[] configure() {
        Option[] options = options(
            // install the spring dm profile            
            profile("spring.dm").version("1.2.0"),    
            // this is how you set the default log level when using pax logging (logProfile)
            org.ops4j.pax.exam.CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
            
            // using the features to install the camel components             
            scanFeatures(mavenBundle().groupId("org.apache.camel.karaf").
                         artifactId("apache-camel").versionAsInProject().type("xml/features"),                         
                          "camel-core", "camel-spring-osgi", "camel-test"),
            
            knopflerfish(), felix(), equinox());
        
        return options;
    }

}
