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
package org.apache.camel.management;

import java.util.Collection;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

/**
 * @version $Revision$
 */
public class CommonManagementLifecycleStrategy implements LifecycleStrategy {

    private CamelManagementStrategy strategy = new CamelManagementStrategy();
    // user for unit testing
    private int endpointCounter;

    public void onContextStart(CamelContext camelContext) {
    }

    public void onEndpointAdd(Endpoint endpoint) {
        endpointCounter++;
        try {
            strategy.addManagedObject(endpoint);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }

    public void onServiceAdd(CamelContext camelContext, Service service) {
    }

    public void onRouteContextCreate(RouteContext routeContext) {
    }

    public void onRoutesAdd(Collection<Route> routes) {
    }

    public int getEndpointCounter() {
        return endpointCounter;
    }
}
