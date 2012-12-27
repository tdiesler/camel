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
package org.apache.camel.component.urlrewrite.http;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.urlrewrite.HttpUrlRewrite;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 *
 */
public class HttpUrlRewriteModFileTest extends CamelTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();

        HttpUrlRewrite myRewrite = new HttpUrlRewrite();
        myRewrite.setModRewriteConfFile("example/modrewrite.cfg");

        jndi.bind("myRewrite", myRewrite);

        return jndi;
    }

    @Test
    public void testHttpUriRewrite() throws Exception {
        String out = template.requestBody("http://localhost:8080/myapp/page/software/", null, String.class);
        assertEquals("http://localhost:8081/myapp2/index.php?page=software", out);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jetty:http://localhost:8080/myapp?matchOnUriPrefix=true")
                    .to("http://localhost:8081/myapp2?bridgeEndpoint=true&throwExceptionOnFailure=false&urlRewrite=#myRewrite");

                from("jetty:http://localhost:8081/myapp2?matchOnUriPrefix=true")
                    .transform().simple("${header.CamelHttpUrl}?${header.CamelHttpQuery}");
            }
        };
    }
}
