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
package org.apache.camel.component.rmi;

import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;

import junit.framework.TestCase;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;

/**
 * @version $Revision$
 */
public class RmiRouteTest extends TestCase {

    public void testPojoRoutes() throws Exception {
        if (classPathHasSpaces()) {
            return;
        }

        // Boot up a local RMI registry
        LocateRegistry.createRegistry(37541);

        // START SNIPPET: register
        JndiContext context = new JndiContext();
        context.bind("bye", new SayService("Good Bye!"));

        CamelContext camelContext = new DefaultCamelContext(context);
        // END SNIPPET: register

        // START SNIPPET: route
        // lets add simple route
        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:hello").to("rmi://localhost:37541/bye");

                // When exposing an RMI endpoint, the interfaces it exposes must
                // be configured.
                RmiEndpoint bye = (RmiEndpoint)endpoint("rmi://localhost:37541/bye");
                bye.setRemoteInterfaces(ISay.class);
                from(bye).to("pojo:bye");
            }
        });
        // END SNIPPET: route

        camelContext.start();

        // START SNIPPET: invoke
        Endpoint endpoint = camelContext.getEndpoint("direct:hello");
        ISay proxy = ProxyHelper.createProxy(endpoint, ISay.class);
        String rc = proxy.say();
        assertEquals("Good Bye!", rc);
        // END SNIPPET: invoke

        camelContext.stop();
    }

    private boolean classPathHasSpaces() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader)cl;
            URL[] urls = ucl.getURLs();
            for (int i = 0; i < urls.length; i++) {
                if (urls[i].getPath().contains(" ")) {
                    System.err.println("=======================================================================");
                    System.err.println(" TEST Skipped: " + getName());
                    System.err.println("   Your probably on windows.  We detected that the classpath");
                    System.err.println("   has a space in it.  Try running maven with the following option: ");
                    System.err.println("   -Dmaven.repo.local=C:\\DOCUME~1\\userid\\.m2\\repository");
                    System.err.println("=======================================================================");
                    return true;
                }
            }
        }
        return false;
    }
}
