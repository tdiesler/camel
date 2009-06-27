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

import java.io.File;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a {@link org.apache.camel.Message} for working with JMS
 *
 * @version $Revision:520964 $
 */
public class JmsMessage extends DefaultMessage {
    private static final transient Log LOG = LogFactory.getLog(JmsMessage.class);
    private Message jmsMessage;
    private JmsBinding binding;

    public JmsMessage() {
    }

    public JmsMessage(Message jmsMessage) {
        setJmsMessage(jmsMessage);
    }

    public JmsMessage(Message jmsMessage, JmsBinding binding) {
        this(jmsMessage);
        setBinding(binding);
    }

    @Override
    public String toString() {
        if (jmsMessage != null) {
            return "JmsMessage: " + jmsMessage;
        } else {
            return "JmsMessage: " + getBody();
        }
    }

    @Override
    public void copyFrom(org.apache.camel.Message that) {
        boolean copyMessageId = true;
        if (that instanceof JmsMessage) {
            JmsMessage thatMessage = (JmsMessage) that;
            this.jmsMessage = thatMessage.jmsMessage;
            if (this.jmsMessage != null) {
                // for performance lets not copy the messageID if we are a JMS message
                copyMessageId = false;
            }
        }
        if (copyMessageId) {
            setMessageId(that.getMessageId());
        }
        setBody(that.getBody());
        getHeaders().putAll(that.getHeaders());
        getAttachments().putAll(that.getAttachments());
    }

    /**
     * Returns the underlying JMS message
     */
    public Message getJmsMessage() {
        return jmsMessage;
    }

    public JmsBinding getBinding() {
        if (binding == null) {
            JmsBinding b = ExchangeHelper.getBinding(getExchange(), JmsBinding.class);
            return b != null ? b : new JmsBinding();
        }
        return binding;
    }

    public void setBinding(JmsBinding binding) {
        this.binding = binding;
    }

    public void setJmsMessage(Message jmsMessage) {
        try {
            setMessageId(jmsMessage.getJMSMessageID());
        } catch (JMSException e) {
            LOG.warn("Unable to retrieve JMSMessageID from JMS Message", e);
        }
        this.jmsMessage = jmsMessage;
    }

    public Object getHeader(String name) {
        Object answer = null;

        // we will exclude using JMS-prefixed headers here to avoid strangeness with some JMS providers
        // e.g. ActiveMQ returns the String not the Destination type for "JMSReplyTo"!
        if (jmsMessage != null && !name.startsWith("JMS")) {
            try {
                answer = jmsMessage.getObjectProperty(name);
            } catch (JMSException e) {
                throw new RuntimeCamelException(name, e);
            }
        }
        if (answer == null) {
            answer = super.getHeader(name);
        }
        return answer;
    }

    @Override
    public Object removeHeader(String name) {
        Object answer = super.removeHeader(name);

        if (jmsMessage != null && !name.startsWith("JMS")) {
            try {
                // also remove header from the JMS message
                if (jmsMessage.propertyExists(name)) {
                    answer = JmsMessageHelper.removeJmsProperty(jmsMessage, name);
                }
            } catch (JMSException e) {
                throw new RuntimeCamelException(name, e);
            }
        }

        return answer;
    }

    @Override
    public JmsMessage newInstance() {
        return new JmsMessage();
    }

    /**
     * Returns true if a new JMS message instance should be created to send to the next component
     */
    public boolean shouldCreateNewMessage() {
        return super.hasPopulatedHeaders();
    }

    @Override
    protected Object createBody() {
        if (jmsMessage != null) {
            return getBinding().extractBodyFromJms(getExchange(), jmsMessage);
        }
        return null;
    }

    @Override
    protected void populateInitialHeaders(Map<String, Object> map) {
        if (jmsMessage != null && map != null) {
            map.putAll(getBinding().extractHeadersFromJms(jmsMessage, getExchange()));
        }
    }

    @Override
    protected String createMessageId() {
        if (jmsMessage == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("No javax.jms.Message set so generating a new message id");
            }
            return super.createMessageId();
        }
        try {
            String id = getDestinationAsString(jmsMessage.getJMSDestination()) + jmsMessage.getJMSMessageID();
            return getSanitizedString(id);
        } catch (JMSException e) {
            throw new RuntimeCamelException("Failed to get JMSMessageID property", e);
        }
    }

    private String getDestinationAsString(Destination destination) throws JMSException {
        String result;
        if (destination == null) {
            result = "null destination!" + File.separator;
        } else if (destination instanceof Topic) {
            result = "topic" + File.separator + ((Topic) destination).getTopicName() + File.separator;
        } else {
            result = "queue" + File.separator + ((Queue) destination).getQueueName() + File.separator;
        }
        return result;
    }

    private String getSanitizedString(Object value) {
        return value != null ? value.toString().replaceAll("[^a-zA-Z0-9\\.\\_\\-]", "_") : "";
    }

    @Override
    public String createExchangeId() {
        if (jmsMessage != null) {
            try {
                return jmsMessage.getJMSMessageID();
            } catch (JMSException e) {
                throw new RuntimeCamelException("Failed to get JMSMessageID property", e);
            }
        }
        return super.createExchangeId();

    }
}
