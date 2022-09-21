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
package org.apache.camel.component.xchange;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class XChangeTestSupport extends CamelTestSupport {
    public static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @Override
    protected void doPreSetup() throws Exception {
        super.doPreSetup();
        if (useMockedBackend()) {
            wireMockServer.start();
        }
    }

    @Override
    protected void doPostTearDown() throws Exception {
        super.doPostTearDown();
        if (useMockedBackend()) {
            wireMockServer.stop();
        }
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        addXChangeClient(context);
        return context;
    }

    protected void addXChangeClient(CamelContext context) {
        XChangeComponent xChangeComponent = new XChangeComponent();

        Class<? extends Exchange> exchangeClass = BinanceExchange.class;
        ExchangeSpecification specification = new ExchangeSpecification(exchangeClass);

        if (useMockedBackend()) {
            specification.setSslUri("http://localhost:" + wireMockServer.port());
        } else {
            specification.setApiKey(System.getProperty("xchange.api.key", System.getenv("XCHANGE_API_KEY")));
            specification.setSecretKey(System.getProperty("xchange.secret.key", System.getenv("XCHANGE_SECRET_KEY")));
        }

        XChange xchange = new XChange(ExchangeFactory.INSTANCE.createExchange(specification));
        xChangeComponent.setExchange(xchange);

        context.addComponent("xchange", xChangeComponent);
    }

    protected boolean useMockedBackend() {
        return !Boolean.TRUE.toString().equals(System.getProperty("enable.xchange.itests"));
    }
}
