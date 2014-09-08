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
package org.apache.camel.itest.osgi.core.log;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.apache.camel.osgi.CamelContextFactory;
import org.apache.karaf.tooling.exam.options.DoNotModifyLogOption;
import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFileReplacementOption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(JUnit4TestRunner.class)
public class LogRouteWithNonDefaultLoggerTest extends OSGiIntegrationTestSupport {

    @Test
    public void testSendMessageToProvidedLogggerWithSiftLogging() throws Exception {
        template.sendBody("log:irrelevant.logger.name?level=info&logger=#mylogger", "<level>INFO</level>");
        template.sendBody("log:irrelevant.logger.name?level=debug&logger=#mylogger", "<level>DEBUG</level>");
        template.sendBody("log:irrelevant.logger.name?level=error&logger=#mylogger", "<level>ERROR</level>");

        File logDir = new File(System.getProperty("karaf.base"), "data/log");
        File[] files = logDir.listFiles();
        assertThat(files.length, equalTo(1));
        assertThat(files[0].getName(), equalTo(bundleContext.getBundle().getSymbolicName() + ".log"));
    }

    @Test
    public void testSendMessageToRegistryDefaultLogggerWithSiftLogging() throws Exception {
        template.sendBody("log:irrelevant.logger.name?level=info", "<level>INFO</level>");
        template.sendBody("log:irrelevant.logger.name?level=debug", "<level>DEBUG</level>");
        template.sendBody("log:irrelevant.logger.name?level=error", "<level>ERROR</level>");

        File logDir = new File(System.getProperty("karaf.base"), "data/log");
        File[] files = logDir.listFiles();
        assertThat(files.length, equalTo(1));
        assertThat(files[0].getName(), equalTo(bundleContext.getBundle().getSymbolicName() + ".log"));
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        LOG.info("Get the bundleContext is " + bundleContext);
        LOG.info("Application installed as bundle id: " + bundleContext.getBundle().getBundleId());

        setThreadContextClassLoader();

        CamelContextFactory factory = new CamelContextFactory();
        factory.setBundleContext(bundleContext);
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("mylogger", LoggerFactory.getLogger("org.apache.camel.SIFT"));
        factory.setRegistry(registry);
        CamelContext camelContext = factory.createContext();
        camelContext.setApplicationContextClassLoader(getClass().getClassLoader());
        camelContext.setUseMDCLogging(true);
        return camelContext;
    }

    @Configuration
    public static Option[] configure() throws Exception {
        return combine(
            getDefaultCamelKarafOptions(),
            new DoNotModifyLogOption(),
            new KarafDistributionConfigurationFileReplacementOption("etc/org.ops4j.pax.logging.cfg", new File("src/test/resources/log4j.properties"))
        );
    }

}
