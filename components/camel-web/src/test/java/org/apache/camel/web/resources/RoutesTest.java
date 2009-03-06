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
package org.apache.camel.web.resources;

import java.util.List;

import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;


/**
 * @version $Revision$
 */
public class RoutesTest extends TestSupport {

    public void testRoutes() throws Exception {

        String text = resource.path("routes").accept("application/xml").get(String.class);
        System.out.println("Routes XML: " + text);
        assertNotNull("XML should not be null", text);

        RoutesDefinition routes = resource.path("routes").accept("application/xml").get(RoutesDefinition.class);
        assertNotNull("Should have found routes", routes);
        List<RouteDefinition> routeList = routes.getRoutes();
        assertNotNull("Should have more than one route", routeList.size() > 0);
        System.out.println("Have routes: " + routeList);
    }
}