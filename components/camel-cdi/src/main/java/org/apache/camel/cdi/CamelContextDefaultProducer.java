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
package org.apache.camel.cdi;

import java.util.Collections;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.camel.impl.DefaultCamelContext;

@Vetoed
final class CamelContextDefaultProducer implements InjectionTarget<DefaultCamelContext> {

    @Override
    public DefaultCamelContext produce(CreationalContext<DefaultCamelContext> ctx) {
        return new DefaultCamelContext();
    }

    @Override
    public void inject(DefaultCamelContext instance, CreationalContext<DefaultCamelContext> ctx) {
    }

    @Override
    public void postConstruct(DefaultCamelContext instance) {
    }

    @Override
    public void preDestroy(DefaultCamelContext instance) {
    }

    @Override
    public void dispose(DefaultCamelContext instance) {
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }
}
