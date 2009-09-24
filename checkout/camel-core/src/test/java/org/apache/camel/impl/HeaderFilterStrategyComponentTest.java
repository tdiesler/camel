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

import java.util.Map;

import junit.framework.TestCase;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategyAware;

/**
 * @version $Revision$
 */
public class HeaderFilterStrategyComponentTest extends TestCase {

    private class MyComponent extends HeaderFilterStrategyComponent {

        protected Endpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
            return null;
        }
    }

    private class MyEndpoint extends DefaultEndpoint implements HeaderFilterStrategyAware {

        private HeaderFilterStrategy strategy;

        public HeaderFilterStrategy getHeaderFilterStrategy() {
            return strategy;
        }

        public void setHeaderFilterStrategy(HeaderFilterStrategy strategy) {
            this.strategy = strategy;
        }

        public Producer createProducer() throws Exception {
            return null;
        }

        public Consumer createConsumer(Processor processor) throws Exception {
            return null;
        }

        public boolean isSingleton() {
            return true;
        }
    }

    public void testHeaderFilterStrategyComponent() {
        MyComponent comp = new MyComponent();
        assertNull(comp.getHeaderFilterStrategy());

        HeaderFilterStrategy strategy = new DefaultHeaderFilterStrategy();
        comp.setHeaderFilterStrategy(strategy);

        assertSame(strategy, comp.getHeaderFilterStrategy());
    }

    public void testHeaderFilterStrategyAware() {
        MyComponent comp = new MyComponent();
        assertNull(comp.getHeaderFilterStrategy());

        HeaderFilterStrategy strategy = new DefaultHeaderFilterStrategy();
        comp.setHeaderFilterStrategy(strategy);

        MyEndpoint my = new MyEndpoint();
        comp.setEndpointHeaderFilterStrategy(my);

        assertSame(strategy, my.getHeaderFilterStrategy());
        assertSame(strategy, comp.getHeaderFilterStrategy());
    }

}
