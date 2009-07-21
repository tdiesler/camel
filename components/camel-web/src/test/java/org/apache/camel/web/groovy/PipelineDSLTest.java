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
 * a test case for pipeline DSL
 */
public class PipelineDSLTest extends GroovyRendererTestSupport {

    public void testPipeline1() throws Exception {
        String DSL = "from(\"direct:start\").pipeline(\"direct:x\", \"direct:y\", \"direct:z\", \"mock:result\")";
        String expectedDSL = "from(\"direct:start\").to(\"direct:x\").to(\"direct:y\").to(\"direct:z\").to(\"mock:result\")";

        assertEquals(expectedDSL, render(DSL));
    }

    public void testPipeline2() throws Exception {
        String DSL = "from(\"direct:start\").pipeline(\"bean:foo?method=hi\", \"bean:foo?method=kabom\").to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").to(\"bean:foo?method=hi\").to(\"bean:foo?method=kabom\").to(\"mock:result\")";

        assertEquals(expectedDSL, render(DSL));
    }
}
