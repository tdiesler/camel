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

package org.apache.camel.web.groovy;

import org.apache.camel.CamelException;

/**
 * a test case for aggregate DSL
 */
public class AggregateDSLTest extends GroovyRendererTestSupport {

    public void testAggragate() throws Exception {
        String DSL = "from(\"direct:start\").aggregate().header(\"cheese\").to(\"mock:result\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }

    public void testAggragateCommon() throws Exception {
        String DSL = "from(\"direct:start\").aggregate(header(\"cheese\")).to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").aggregate().header(\"cheese\").to(\"mock:result\")";

        assertEquals(expectedDSL, render(DSL));
    }

    public void testAggregateGroupedExchange() throws Exception {
        String DSL = "from(\"direct:start\").aggregate().simple(\"id\").batchTimeout(500L).groupExchanges().to(\"mock:result\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }

    public void testAggregateTimeoutOnly() throws Exception {
        String DSL = "from(\"direct:start\").aggregate(header(\"id\")).batchTimeout(3000).batchSize(0).to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").aggregate().header(\"id\").batchTimeout(3000).batchSize(0).to(\"mock:result\")";

        assertEquals(expectedDSL, render(DSL));
    }

    /**
     * a route involving a external class: CamelException
     * 
     * @throws Exception
     */
    public void _testAggregateAndOnException() throws Exception {
        String DSL = "errorHandler(deadLetterChannel(\"mock:error\"));onException(CamelException.class).maximumRedeliveries(2);from(\"direct:start\").aggregate(header(\"id\")).to(\"mock:result\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }

    /**
     * a set of routes that uses aggregate DSL
     * 
     * @throws Exception
     */
    public void _testAggregateTimerAndTracer() throws Exception {
        String DSL = "from(\"timer://kickoff?period=9999910000\").setHeader(\"id\").constant(\"foo\").setBody().constant(\"a b c\").split(body().tokenize(\" \")).to(\"seda:splitted\");"
                     + "from(\"seda:splitted\").aggregate(header(\"id\")).to(\"mock:result\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }
}
