/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.activemq;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.*;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.component.jms.JmsBinding;

import javax.jms.MessageNotWriteableException;
import javax.jms.JMSException;
import java.io.Serializable;

/**
 * @version $Revision: 1.1 $
 */
@Converter
public class ActiveMQMessageConverter {
    private JmsBinding binding = new JmsBinding();

    /**
     * Converts the inbound message exchange to an ActiveMQ JMS message
     *
     * @return the ActiveMQ message
     */
    @Converter
    public ActiveMQMessage toMessage(Exchange exchange) throws JMSException {
        ActiveMQMessage message = createActiveMQMessage(exchange);
        getBinding().appendJmsProperties(message, exchange);
        return message;
    }

    private static ActiveMQMessage createActiveMQMessage(Exchange exchange) throws JMSException {
        Object body = exchange.getIn().getBody();
        if (body instanceof String) {
            ActiveMQTextMessage answer = new ActiveMQTextMessage();
            answer.setText((String) body);
            return answer;
        } else if (body instanceof Serializable) {
            ActiveMQObjectMessage answer = new ActiveMQObjectMessage();
            answer.setObject((Serializable) body);
            return answer;
        } else {
            return new ActiveMQMessage();
        }

    }

    // Properties
    //-------------------------------------------------------------------------
    public JmsBinding getBinding() {
        return binding;
    }

    public void setBinding(JmsBinding binding) {
        this.binding = binding;
    }
}