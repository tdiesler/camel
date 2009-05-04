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
package org.apache.camel.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Intercept;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.DataFormatDefinition;
import org.apache.camel.processor.Interceptor;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.ProceedProcessor;
import org.apache.camel.processor.UnitOfWorkProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.RouteContext;

/**
 * The context used to activate new routing rules
 *
 * @version $Revision$
 */
public class DefaultRouteContext implements RouteContext {
    private final RouteDefinition route;
    private FromDefinition from;
    private final Collection<Route> routes;
    private Endpoint endpoint;
    private final List<Processor> eventDrivenProcessors = new ArrayList<Processor>();
    private Interceptor lastInterceptor;
    private CamelContext camelContext;
    private List<InterceptStrategy> interceptStrategies = new ArrayList<InterceptStrategy>();
    private boolean routeAdded;

    public DefaultRouteContext(RouteDefinition route, FromDefinition from, Collection<Route> routes) {
        this.route = route;
        this.from = from;
        this.routes = routes;
    }

    /**
     * Only used for lazy construction from inside ExpressionType
     */
    public DefaultRouteContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        routes = new ArrayList<Route>();
        route = new RouteDefinition("temporary");
    }

    public Endpoint getEndpoint() {
        if (endpoint == null) {
            endpoint = from.resolveEndpoint(this);
        }
        return endpoint;
    }

    public FromDefinition getFrom() {
        return from;
    }

    public RouteDefinition getRoute() {
        return route;
    }

    public CamelContext getCamelContext() {
        if (camelContext == null) {
            camelContext = getRoute().getCamelContext();
        }
        return camelContext;
    }

    public Processor createProcessor(ProcessorDefinition node) throws Exception {
        return node.createOutputsProcessor(this);
    }

    public Endpoint resolveEndpoint(String uri) {
        return route.resolveEndpoint(uri);
    }

    public Endpoint resolveEndpoint(String uri, String ref) {
        Endpoint endpoint = null;
        if (uri != null) {
            endpoint = resolveEndpoint(uri);
            if (endpoint == null) {
                throw new NoSuchEndpointException(uri);
            }
        }
        if (ref != null) {
            endpoint = lookup(ref, Endpoint.class);
            if (endpoint == null) {
                throw new NoSuchEndpointException("ref:" + ref);
            }
        }
        if (endpoint == null) {
            throw new IllegalArgumentException("Either 'uri' or 'ref' must be specified on: " + this);
        } else {
            return endpoint;
        }
    }

    public <T> T lookup(String name, Class<T> type) {
        return getCamelContext().getRegistry().lookup(name, type);
    }

    public <T> Map<String, T> lookupByType(Class<T> type) {
        return getCamelContext().getRegistry().lookupByType(type);
    }

    @SuppressWarnings("unchecked")
    public void commit() {
        // now lets turn all of the event driven consumer processors into a single route
        if (!eventDrivenProcessors.isEmpty()) {
            Processor processor = Pipeline.newInstance(eventDrivenProcessors);

            // and wrap it in a unit of work so the UoW is on the top, so the entire route will be in the same UoW
            Processor unitOfWorkProcessor = new UnitOfWorkProcessor(processor);

            // and create the route that wraps the UoW
            Route edcr = new EventDrivenConsumerRoute(getEndpoint(), unitOfWorkProcessor);
            edcr.getProperties().put(Route.ID_PROPERTY, route.idOrCreate());
            edcr.getProperties().put(Route.PARENT_PROPERTY, Integer.toHexString(route.hashCode()));
            if (route.getGroup() != null) {
                edcr.getProperties().put(Route.GROUP_PROPERTY, route.getGroup());
            }

            routes.add(edcr);
        }
    }

    public void addEventDrivenProcessor(Processor processor) {
        eventDrivenProcessors.add(processor);
    }

    public void intercept(Intercept interceptor) {
        lastInterceptor = (Interceptor)interceptor;
    }

    public Processor createProceedProcessor() {
        if (lastInterceptor == null) {
            throw new IllegalArgumentException("Cannot proceed() from outside of an interceptor!");
        } else {
            return new ProceedProcessor(lastInterceptor);
        }
    }

    public List<InterceptStrategy> getInterceptStrategies() {
        return interceptStrategies;
    }

    public void setInterceptStrategies(List<InterceptStrategy> interceptStrategies) {
        this.interceptStrategies = interceptStrategies;
    }

    public void addInterceptStrategy(InterceptStrategy interceptStrategy) {
        getInterceptStrategies().add(interceptStrategy);
    }

    public boolean isRouteAdded() {
        return routeAdded;
    }

    public void setIsRouteAdded(boolean routeAdded) {
        this.routeAdded = routeAdded;
    }

    public DataFormatDefinition getDataFormat(String ref) {
        Map<String, DataFormatDefinition> dataFormats = getCamelContext().getDataFormats();
        if (dataFormats != null) {
            return dataFormats.get(ref);
        } else {
            return null;
        }
    }

}
