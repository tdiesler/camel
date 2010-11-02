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
package org.apache.camel.component.jms.reply;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.util.IntrospectionSupport;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * A {@link ReplyManager} when using temporary queues.
 *
 * @version $Revision$
 */
public class TemporaryQueueReplyManager extends ReplyManagerSupport {

    public String registerReply(ReplyManager replyManager, Exchange exchange, AsyncCallback callback,
                                String originalCorrelationId, String correlationId, long requestTimeout) {
        // add to correlation map
        TemporaryQueueReplyHandler handler = new TemporaryQueueReplyHandler(this, exchange, callback, originalCorrelationId, requestTimeout);
        correlation.put(correlationId, handler, requestTimeout);
        return correlationId;
    }

    public void updateCorrelationId(String correlationId, String newCorrelationId, long requestTimeout) {
        if (log.isTraceEnabled()) {
            log.trace("Updated provisional correlationId [" + correlationId + "] to expected correlationId [" + newCorrelationId + "]");
        }

        ReplyHandler handler = correlation.remove(correlationId);
        correlation.put(newCorrelationId, handler, requestTimeout);
    }

    @Override
    protected void handleReplyMessage(String correlationID, Message message) {
        ReplyHandler handler = correlation.get(correlationID);
        if (handler == null && endpoint.isUseMessageIDAsCorrelationID()) {
            handler = waitForProvisionCorrelationToBeUpdated(correlationID, message);
        }

        if (handler != null) {
            try {
                handler.onReply(correlationID, message);
            } finally {
                correlation.remove(correlationID);
            }
        } else {
            // we could not correlate the received reply message to a matching request and therefore
            // we cannot continue routing the unknown message
            String text = "Reply received for unknown correlationID [" + correlationID + "] -> " + message;
            log.warn(text);
            throw new UnknownReplyMessageException(text, message, correlationID);
        }
    }

    public void setReplyToSelectorHeader(org.apache.camel.Message camelMessage, Message jmsMessage) throws JMSException {
        // noop
    }

    @Override
    protected AbstractMessageListenerContainer createListenerContainer() throws Exception {
        // Use DefaultMessageListenerContainer as it supports reconnects (see CAMEL-3193)
        DefaultMessageListenerContainer answer = new DefaultMessageListenerContainer();

        answer.setDestinationName("temporary");
        answer.setDestinationResolver(new DestinationResolver() {
            public Destination resolveDestinationName(Session session, String destinationName,
                                                      boolean pubSubDomain) throws JMSException {
                // use a temporary queue to gather the reply message
                TemporaryQueue queue = null;
                synchronized (TemporaryQueueReplyManager.this) {
                    try {
                        queue = session.createTemporaryQueue();
                        setReplyTo(queue);
                    } finally {
                        TemporaryQueueReplyManager.this.notifyAll();
                    }
                }
                return queue;
            }
        });
        answer.setAutoStartup(true);
        answer.setMessageListener(this);
        answer.setPubSubDomain(false);
        answer.setSubscriptionDurable(false);
        answer.setConcurrentConsumers(1);
        answer.setConnectionFactory(endpoint.getConnectionFactory());
        answer.setSessionTransacted(false);
        String clientId = endpoint.getClientId();
        if (clientId != null) {
            clientId += ".CamelReplyManager";
            answer.setClientId(clientId);
        }
        TaskExecutor taskExecutor = endpoint.getTaskExecutor();
        if (taskExecutor != null) {
            answer.setTaskExecutor(taskExecutor);
        }
        if (endpoint.getTaskExecutorSpring2() != null) {
            // use reflection to invoke to support spring 2 when JAR is compiled with Spring 3.0
            IntrospectionSupport.setProperty(answer, "taskExecutor", endpoint.getTaskExecutorSpring2());
        }
        ExceptionListener exceptionListener = endpoint.getExceptionListener();
        if (exceptionListener != null) {
            answer.setExceptionListener(exceptionListener);
        }
        return answer;
    }

}
