/*
 * © 2001-2009, Progress Software Corporation and/or its subsidiaries or affiliates.  All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            ManagedEndpoint me = new ManagedEndpoint(endpoint);
            strategy.addManagedObject(me);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }

    public void onServiceAdd(CamelContext camelContext, Service service) {
    }

    public void onRouteContextCreate(RouteContext routeContext) {
    }

    public void onRoutesAdd(Collection<Route> routes) {
        for (Route route : routes) {

        }
    }

    public int getEndpointCounter() {
        return endpointCounter;
    }

    public CamelManagementStrategy getStrategy() {
        return strategy;
    }
}
