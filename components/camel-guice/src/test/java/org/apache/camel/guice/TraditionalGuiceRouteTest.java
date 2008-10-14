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
package org.apache.camel.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.TestCase;
import org.apache.camel.CamelContext;

/**
 * Lets use a custom CamelModule to perform explicit binding of route builders
 *
 * @version $Revision$
 */
public class TraditionalGuiceRouteTest extends TestCase {

    public static class MyModule extends CamelModuleWithRouteTypes {

        public MyModule() {
            super(MyHardcodeRoute.class, MyRouteInstaller.class);
        }

    }


    public void testGuice() throws Exception {
        Injector injector = Guice.createInjector(new MyModule());
        CamelContext camelContext = injector.getInstance(CamelContext.class);
        camelContext.start();
        Thread.sleep(1000);
        camelContext.stop();
    }

}
