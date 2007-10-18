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
package org.apache.camel.component.jms;

import javax.jms.Message;

import org.apache.camel.ExchangePattern;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.requestor.Requestor;
import org.apache.camel.impl.DefaultEndpoint;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

/**
 * A <a href="http://activemq.apache.org/jms.html">JMS Endpoint</a>
 *
 * @version $Revision:520964 $
 */
public class JmsEndpoint extends DefaultEndpoint<JmsExchange> {
    private final JmsComponent component;
    private final boolean pubSubDomain;
    private JmsBinding binding;
    private String destination;
    private String selector;
    private JmsConfiguration configuration;
    private Requestor requestor;
    private long requestTimeout = 20000L;

    public JmsEndpoint(String uri, JmsComponent component, String destination, boolean pubSubDomain, JmsConfiguration configuration) {
        super(uri, component);
        this.component = component;
        this.configuration = configuration;
        this.destination = destination;
        this.pubSubDomain = pubSubDomain;
    }

    public JmsProducer createProducer() throws Exception {
        return new JmsProducer(this);
    }

    /**
     * Creates a producer using the given template for InOnly message exchanges
     */
    public JmsProducer createProducer(JmsOperations template) throws Exception {
        JmsProducer answer = createProducer();
        if (template instanceof JmsTemplate) {
            JmsTemplate jmsTemplate = (JmsTemplate) template;
            jmsTemplate.setPubSubDomain(pubSubDomain);
            jmsTemplate.setDefaultDestinationName(destination);
        }
        answer.setInOnlyTemplate(template);
        return answer;
    }

    public JmsConsumer createConsumer(Processor processor) throws Exception {
        AbstractMessageListenerContainer listenerContainer = configuration.createMessageListenerContainer();
        return createConsumer(processor, listenerContainer);
    }

    /**
     * Creates a consumer using the given processor and listener container
     *
     * @param processor         the processor to use to process the messages
     * @param listenerContainer the listener container
     * @return a newly created consumer
     * @throws Exception if the consumer cannot be created
     */
    public JmsConsumer createConsumer(Processor processor, AbstractMessageListenerContainer listenerContainer) throws Exception {
        listenerContainer.setDestinationName(destination);
        listenerContainer.setPubSubDomain(pubSubDomain);
        if (selector != null) {
            listenerContainer.setMessageSelector(selector);
        }
        return new JmsConsumer(this, processor, listenerContainer);
    }

    @Override
    public PollingConsumer<JmsExchange> createPollingConsumer() throws Exception {
        JmsOperations template = createInOnlyTemplate();
        return new JmsPollingConsumer(this, template);
    }

    @Override
    public JmsExchange createExchange(ExchangePattern pattern) {
        return new JmsExchange(getContext(), pattern, getBinding());
    }

    public JmsExchange createExchange(Message message) {
        return new JmsExchange(getContext(), getExchangePattern(), getBinding(), message);
    }

    /**
     * Factory method for creating a new template for InOnly message exchanges
     */
    public JmsOperations createInOnlyTemplate() {
        return configuration.createInOnlyTemplate(pubSubDomain, destination);
    }

    /**
     * Factory method for creating a new template for InOut message exchanges
     */
    public JmsOperations createInOutTemplate() {
        return configuration.createInOutTemplate(pubSubDomain, destination, getRequestTimeout());
    }

    // Properties
    // -------------------------------------------------------------------------
    public JmsBinding getBinding() {
        if (binding == null) {
            binding = new JmsBinding();
        }
        return binding;
    }

    /**
     * Sets the binding used to convert from a Camel message to and from a JMS
     * message
     *
     * @param binding the binding to use
     */
    public void setBinding(JmsBinding binding) {
        this.binding = binding;
    }

    public String getDestination() {
        return destination;
    }

    public JmsConfiguration getConfiguration() {
        return configuration;
    }

    public String getSelector() {
        return selector;
    }

    /**
     * Sets the JMS selector to use
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    public boolean isSingleton() {
        return false;
    }

    public Requestor getRequestor() throws Exception {
        if (requestor == null) {
            requestor = component.getRequestor();
        }
        return requestor;
    }

    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the timeout in milliseconds which requests should timeout after
     * 
     * @param requestTimeout
     */
    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
}
