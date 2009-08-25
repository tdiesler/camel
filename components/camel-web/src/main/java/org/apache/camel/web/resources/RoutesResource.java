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

import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.view.RouteDotGenerator;

/**
 * The active routes in Camel which are used to implement one or more <a
 * href="http://camel.apache.org/enterprise-integration-patterns.html"
 * >Enterprise Integration Paterns</a>
 *
 * @version $Revision$
 */
public class RoutesResource extends CamelChildResourceSupport {

    public RoutesResource(CamelContextResource contextResource) {
        super(contextResource);
    }

    /**
     * Returns the routes currently active within this context
     */
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public RoutesDefinition getRouteDefinitions() {
        RoutesDefinition answer = new RoutesDefinition();
        CamelContext camelContext = getCamelContext();
        if (camelContext != null) {
            List<RouteDefinition> list = camelContext.getRouteDefinitions();
            answer.setRoutes(list);
        }
        return answer;
    }

    /**
     * Returns the Graphviz DOT <a
     * href="http://camel.apache.org/visualisation.html">Visualisation</a> of
     * the current Camel routes
     */
    @GET
    @Produces(Constants.DOT_MIMETYPE)
    public String getDot() throws IOException {
        RouteDotGenerator generator = new RouteDotGenerator("/tmp/camel");
        return generator.getRoutesText(getCamelContext());
    }

    /**
     * Looks up an individual route
     */
    @Path("{id}")
    public RouteResource getRoute(@PathParam("id") String id) {
        List<RouteDefinition> list = getRoutes();
        for (RouteDefinition routeType : list) {
            if (routeType.getId().equals(id)) {
                return new RouteResource(this, routeType);
            }
        }
        return null;
    }

    /**
     * Looks up an individual route with specified language
     */
    @Path("{id}/{language}")
    public RouteResource getRoute(@PathParam("id") String id, @PathParam("language") String language) {
        RouteResource routeResource = getRoute(id);
        if (routeResource != null) {
            routeResource.setLanguage(language);
        }
        return routeResource;
    }

    @Path("{id}/status")
    public RouteStatusResource getRouteStatus(@PathParam("id") String id) {
        RouteResource routeResource = getRoute(id);
        return routeResource.getRouteStatus();
    }    
    
    // Properties
    // -------------------------------------------------------------------------
    public List<RouteDefinition> getRoutes() {
        return getRouteDefinitions().getRoutes();
    }
}
