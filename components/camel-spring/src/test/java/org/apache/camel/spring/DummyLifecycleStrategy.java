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
package org.apache.camel.spring;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;

/**
 * Dummy LifecycleStrategy for LifecycleStrategy injection test.
 *
 * @version 
 */
public class DummyLifecycleStrategy implements LifecycleStrategy {

    public void onContextStart(CamelContext camelContext) {
    }

    public void onContextStop(CamelContext camelContext) {
    }

    public void onComponentAdd(String s, Component component) {
    }

    public void onComponentRemove(String s, Component component) {
    }

    public void onEndpointAdd(Endpoint endpoint) {
    }

    public void onEndpointRemove(Endpoint endpoint) {
    }

    public void onServiceAdd(CamelContext camelContext, Service service, Route route) {
    }

    public void onServiceRemove(CamelContext camelContext, Service service, Route route) {
    }

    public void onRouteContextCreate(RouteContext routeContext) {
    }

    public void onErrorHandlerAdd(RouteContext routeContext, Processor errorHandler, ErrorHandlerBuilder errorHandlerBuilder) {
    }

    public void onRoutesRemove(Collection<Route> routes) {
    }

    public void onRoutesAdd(Collection<Route> routes) {
    }

    public void onThreadPoolAdd(CamelContext camelContext, ThreadPoolExecutor threadPool, String id,
                                String sourceId, String routeId, String threadPoolProfileId) {
    }
}
