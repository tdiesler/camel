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
package org.apache.camel.component.msmq;

import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;

/**
 * @version $Revision: 630591 $
 */
public class MsmqEndpoint extends DefaultEndpoint<DefaultExchange> {
	
	private final String    remaining;
	private final Map<?, ?> parameters;

    public MsmqEndpoint(String endpointUri, String remaining, Map<?, ?> parameters, MsmqComponent component) {
        super(endpointUri, component);
        this.remaining = remaining;
        this.parameters = parameters;
    }

    public Producer<DefaultExchange> createProducer() throws Exception {
        return new MsmqProducer(this);
    }

    @Override
    public DefaultExchange createExchange() {
    	return new DefaultExchange(getContext(), getExchangePattern());
    }

    public boolean isSingleton() {
        return true;
    }

	public String getRemaining() {
		return remaining;
	}

	public Map<?, ?> getParameters() {
		return parameters;
	}

	public Consumer<DefaultExchange> createConsumer(Processor processor)
			throws Exception {
		return new MsmqConsumer(this, processor);
	}
	
}
