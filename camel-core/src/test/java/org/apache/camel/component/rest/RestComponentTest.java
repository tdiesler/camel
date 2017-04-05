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
package org.apache.camel.component.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestComponentTest {

    private CamelContext context;

    private RestComponent rest;

    @Before
    public void createSubjects() {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("host-ref", "http://localhost:8080");
        context = new DefaultCamelContext(registry);

        rest = new RestComponent();
        rest.setCamelContext(context);

    }

    @Test
    public void shouldResolveHostParameterAsReference() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("host", "#host-ref");

        final RestEndpoint endpoint = (RestEndpoint) rest.createEndpoint("rest://GET:/path:?host=#host-ref",
            "GET:/path", parameters);

        Assert.assertEquals("http://localhost:8080", endpoint.getHost());
    }

    @Test
    public void shouldResolveHostParameterAsGivenValue() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("host", "http://localhost:8080");

        final RestEndpoint endpoint = (RestEndpoint) rest
            .createEndpoint("rest://GET:/path:?host=http%3A%2F%2Flocalhost%3A8080", "GET:/path", parameters);

        Assert.assertEquals("http://localhost:8080", endpoint.getHost());
    }
}
