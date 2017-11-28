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
package org.apache.camel.component.apns.factory;

import java.io.IOException;

import com.notnoop.apns.ApnsServiceBuilder;

import org.apache.camel.util.jsse.SSLContextParameters;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConfiguredApnsServiceFactoryTest {

    ApnsServiceBuilder apnsServiceBuilder = mock(ApnsServiceBuilder.class);

    ApnsServiceFactory apnsServiceFactory = new ApnsServiceFactory() {
        @Override
        protected ApnsServiceBuilder configureServiceBuilder(ApnsServiceBuilder serviceBuilder) {
            apnsServiceBuilder.withSocksProxy("my.proxy.com", 6666);
            return apnsServiceBuilder;
        }
    };

    @Before
    public void setUp() throws IOException {
        apnsServiceFactory.setSslContextParameters(mock(SSLContextParameters.class));
    }

    @Test
    public void shouldSetProxyOnDefaultServiceBuilder() {
        // When
        apnsServiceFactory.getApnsService();

        // Then
        verify(apnsServiceBuilder).withSocksProxy(anyString(), anyInt());
    }

}
