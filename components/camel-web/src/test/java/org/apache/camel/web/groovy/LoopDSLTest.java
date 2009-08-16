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

/**
 * a test case for loop DSL
 */
public class LoopDSLTest extends GroovyRendererTestSupport {

    public void testSimpleLoop() throws Exception {
        String dsl = "from(\"direct:start\").loop(8).to(\"mock:result\")";
        assertEquals(dsl, render(dsl));
    }

    public void testLoopWithHeader() throws Exception {
        String dsl = "from(\"direct:start\").loop(header(\"loop\")).to(\"mock:result\")";
        String expected = "from(\"direct:start\").loop().header(\"loop\").to(\"mock:result\")";

        assertEquals(expected, render(dsl));
    }

    public void testLoopWithXPath() throws Exception {
        String dsl = "from(\"direct:start\").loop().xpath(\"/hello/@times\").to(\"mock:result\")";
        assertEquals(dsl, render(dsl));
    }

    public void testLoopWithEnd() throws Exception {
        String dsl = "from(\"direct:start\").loop(2).to(\"mock:result\").end().to(\"mock:last\")";
        String expected = "from(\"direct:start\").loop(2).to(\"mock:result\").to(\"mock:last\")";

        assertEquals(expected, render(dsl));
    }
}
