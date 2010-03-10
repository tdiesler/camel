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
package org.apache.camel.component.jetty;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.jetty.server.ssl.SslSocketConnector;

public class ExplicitHttpsRouteTest extends HttpsRouteTest {
    
    private SslSocketConnector createSslSocketConnector() throws URISyntaxException {
        SslSocketConnector sslSocketConnector = new SslSocketConnector();
        sslSocketConnector.setKeyPassword(pwd);
        sslSocketConnector.setPassword(pwd);
        URL keyStoreUrl = this.getClass().getClassLoader().getResource("jsse/localhost.ks");
        sslSocketConnector.setKeystore(keyStoreUrl.toURI().getPath());
        sslSocketConnector.setTruststoreType("JKS");
        return sslSocketConnector;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws URISyntaxException {
                
                JettyHttpComponent componentJetty = (JettyHttpComponent) context.getComponent("jetty");
                Map<Integer, SslSocketConnector> connectors = new HashMap<Integer, SslSocketConnector>();
                connectors.put(9080, createSslSocketConnector());
                connectors.put(9090, createSslSocketConnector());
                
                componentJetty.setSslSocketConnectors(connectors);
                
                from("jetty:https://localhost:9080/test").to("mock:a");

                Processor proc = new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setBody("<b>Hello World</b>");
                    }
                };
                from("jetty:https://localhost:9080/hello").process(proc);
                
                from("jetty:https://localhost:9090/test").to("mock:b");
            }
        };
    }
}