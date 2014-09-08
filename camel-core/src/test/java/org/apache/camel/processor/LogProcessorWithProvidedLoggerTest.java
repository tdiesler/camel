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
package org.apache.camel.processor;

import java.io.StringWriter;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @version
 */
public class LogProcessorWithProvidedLoggerTest extends ContextTestSupport {

    // to capture the logs
    private static StringWriter sw;
    // to capture the warnings from LogComponent
    private static StringWriter sw2;

    private static final class CapturingAppender extends AppenderSkeleton {
        private StringWriter sw;

        private CapturingAppender(StringWriter sw) {
            this.sw = sw;
        }

        @Override
        protected void append(LoggingEvent event) {
            this.sw.append(event.getLoggerName() + " " + event.getLevel().toString() + " " + event.getMessage());
        }

        @Override
        public void close() {
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        sw = new StringWriter();
        Logger.getLogger("org.apache.camel.customlogger").removeAllAppenders();
        Logger.getLogger("org.apache.camel.customlogger").addAppender(new CapturingAppender(sw));
        Logger.getLogger("org.apache.camel.customlogger").setLevel(Level.TRACE);
    }

    public void testLogProcessorWithRegistryLogger() throws Exception {
        getMockEndpoint("mock:foo").expectedMessageCount(1);

        template.sendBody("direct:foo", "Bye World");

        assertMockEndpointsSatisfied();

        assertThat(sw.toString(), equalTo("org.apache.camel.customlogger INFO Got Bye World"));
    }

    public void testLogProcessorWithProvidedLogger() throws Exception {
        getMockEndpoint("mock:bar").expectedMessageCount(1);

        template.sendBody("direct:bar", "Bye World");

        assertMockEndpointsSatisfied();

        assertThat(sw.toString(), equalTo("org.apache.camel.customlogger INFO Also got Bye World"));
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("mylogger1", LoggerFactory.getLogger("org.apache.camel.customlogger"));
        CamelContext context = new DefaultCamelContext(registry);
        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foo").routeId("foo").log(LoggingLevel.INFO, "Got ${body}").to("mock:foo");
                from("direct:bar").routeId("bar").log(LoggingLevel.INFO, LoggerFactory.getLogger("org.apache.camel.customlogger"), "Also got ${body}").to("mock:bar");
            }
        };
    }

}
