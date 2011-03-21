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
package org.apache.camel.itest.osgi.core.dataset;

import javax.naming.Context;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;


@RunWith(JUnit4TestRunner.class)
public class DataSetRouteTest extends OSGiIntegrationTestSupport {

    protected SimpleDataSet dataSet = new SimpleDataSet(20);

    @Test
    public void testDataSet() throws Exception {
        // data set will itself set its assertions so we should just
        // assert that all mocks is ok
        assertMockEndpointsSatisfied();
    }

    @Override
    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();
        context.bind("foo", dataSet);
        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                // start this first to make sure the "direct:foo" consumer is ready
                from("direct:foo").to("dataset:foo?minRate=50");
                from("dataset:foo?minRate=50").to("direct:foo");
            }
        };
    }

}
