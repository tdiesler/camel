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
package org.apache.camel.component.salesforce.internal.streaming;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.component.salesforce.SalesforceComponent;
import org.apache.camel.component.salesforce.SalesforceEndpoint;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.junit.Test;

import static org.apache.camel.component.salesforce.internal.streaming.SubscriptionHelper.determineReplayIdFor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionHelperTest {

    @Test
    public void shouldSupportInitialConfigMapWithTwoKeySyntaxes() throws Exception {
        final Map<String, Long> initialReplayIdMap = new HashMap<>();
        initialReplayIdMap.put("my-topic-1", 10L);
        initialReplayIdMap.put("/topic/my-topic-1", 20L);
        initialReplayIdMap.put("/topic/my-topic-2", 30L);

        final SalesforceEndpointConfig config = new SalesforceEndpointConfig();
        config.setDefaultReplayId(14L);
        config.setInitialReplayIdMap(initialReplayIdMap);

        final SalesforceComponent component = mock(SalesforceComponent.class);
        final SalesforceEndpoint endpoint = mock(SalesforceEndpoint.class);

        when(endpoint.getComponent()).thenReturn(component);
        when(endpoint.getConfiguration()).thenReturn(config);
        when(component.getConfig()).thenReturn(new SalesforceEndpointConfig());

        assertEquals("Expecting replayId for `my-topic-1` to be 10, as short topic names have priority",
                     (Long) 10L, determineReplayIdFor(endpoint, "my-topic-1"));

        assertEquals("Expecting replayId for `my-topic-2` to be 30, the only one given", (Long) 30L,
                     determineReplayIdFor(endpoint, "my-topic-2"));

        assertEquals("Expecting replayId for `my-topic-3` to be 14, the default", (Long) 14L,
                     determineReplayIdFor(endpoint, "my-topic-3"));
    }

    @Test
    public void precedenceShouldBeFollowed() {
        final SalesforceEndpointConfig componentConfig = new SalesforceEndpointConfig();
        componentConfig.setDefaultReplayId(1L);
        componentConfig.setInitialReplayIdMap(Collections.singletonMap("my-topic-1", 2L));
        componentConfig.setInitialReplayIdMap(Collections.singletonMap("my-topic-2", 3L));

        final SalesforceEndpointConfig endpointConfig = new SalesforceEndpointConfig();
        endpointConfig.setDefaultReplayId(4L);
        endpointConfig.setInitialReplayIdMap(Collections.singletonMap("my-topic-1", 5L));

        final SalesforceComponent component = mock(SalesforceComponent.class);
        when(component.getConfig()).thenReturn(componentConfig);

        final SalesforceEndpoint endpoint = mock(SalesforceEndpoint.class);
        when(endpoint.getComponent()).thenReturn(component);
        when(endpoint.getConfiguration()).thenReturn(endpointConfig);

        assertEquals("Expecting replayId for `my-topic-1` to be 5, as endpoint configuration has priority",
                     (Long) 5L, determineReplayIdFor(endpoint, "my-topic-1"));

        assertEquals("Expecting replayId for `my-topic-2` to be 3, as endpoint does not configure it",
                     (Long) 3L, determineReplayIdFor(endpoint, "my-topic-2"));

        assertEquals("Expecting replayId for `my-topic-3` to be 4, as it is endpoint's default",
                     (Long) 4L, determineReplayIdFor(endpoint, "my-topic-3"));

        endpointConfig.setDefaultReplayId(null);

        assertEquals("Expecting replayId for `my-topic-3` to be 1, as it is component's default when endpoint does not have a default",
                     (Long) 1L, determineReplayIdFor(endpoint, "my-topic-3"));
    }
}
