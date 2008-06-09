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
package org.apache.camel.example.client;

import org.apache.camel.example.server.Multiplier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Requires that the JMS broker is running, as well as CamelServer
 *
 * @author martin.gilday
 */
public final class CamelClientRemoting {

    // START SNIPPET: e1
    public static void main(final String[] args) {
        System.out.println("Notice this client requires that the CamelServer is already running!");

        ApplicationContext context = new ClassPathXmlApplicationContext("camel-client-remoting.xml");
        // just get the proxy to the service and we as the client can use the "proxy" as it was
        // a local object we are invocing. Camel will under the covers do the remote communication
        // to the remote ActiveMQ server and fetch the response.
        Multiplier multiplier = (Multiplier)context.getBean("multiplierProxy");

        System.out.println("Invoking the multiply with 33");
        int response = multiplier.multiply(33);
        System.out.println("... the result is: " + response);

        System.exit(0);
    }
    // END SNIPPET: e1

}
