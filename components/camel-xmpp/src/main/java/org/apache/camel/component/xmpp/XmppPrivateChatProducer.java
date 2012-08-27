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
package org.apache.camel.component.xmpp;

import org.apache.camel.Exchange;
import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 
 */
public class XmppPrivateChatProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(XmppPrivateChatProducer.class);
    private final XmppEndpoint endpoint;
    private XMPPConnection connection;
    private final String participant;

    public XmppPrivateChatProducer(XmppEndpoint endpoint, String participant) {
        super(endpoint);
        this.endpoint = endpoint;
        this.participant = participant;
        ObjectHelper.notEmpty(participant, "participant");

        LOG.debug("Creating XmppPrivateChatProducer to participant {}", participant);
    }

    public void process(Exchange exchange) {

        // make sure we are connected
        try {
            if (connection == null) {
                connection = endpoint.createConnection();
            }

            if (!connection.isConnected()) {
                this.reconnect();
            }
        } catch (XMPPException e) {
            throw new RuntimeException("Could not connect to XMPP server.", e);
        }

        ChatManager chatManager = connection.getChatManager();
        Chat chat = getOrCreateChat(chatManager);
        Message message = null;
        try {
            message = new Message();
            message.setTo(getParticipant());
            message.setThread(endpoint.getChatId());
            message.setType(Message.Type.normal);

            endpoint.getBinding().populateXmppMessage(message, exchange);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending XMPP message to {} from {} : {}", new Object[]{endpoint.getParticipant(), endpoint.getUser(), message.getBody()});
            }
            chat.sendMessage(message);
        } catch (XMPPException xmppe) {
            throw new RuntimeExchangeException("Could not send XMPP message: to " + endpoint.getParticipant() + " from " + endpoint.getUser() + " : " + message
                    + " to: " + XmppEndpoint.getConnectionMessage(connection), exchange, xmppe);
        } catch (Exception e) {
            throw new RuntimeExchangeException("Could not send XMPP message to " + endpoint.getParticipant() + " from " + endpoint.getUser() + " : " + message
                    + " to: " + XmppEndpoint.getConnectionMessage(connection), exchange, e);
        }
    }

    private synchronized Chat getOrCreateChat(ChatManager chatManager) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Looking for existing chat instance with thread ID {}", endpoint.getChatId());
        }
        Chat chat = chatManager.getThreadChat(endpoint.getChatId());
        if (chat == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Creating new chat instance with thread ID {}", endpoint.getChatId());
            }
            chat = chatManager.createChat(getParticipant(), endpoint.getChatId(), new MessageListener() {
                public void processMessage(Chat chat, Message message) {
                    // not here to do conversation
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Received and discarding message from {} : {}"
                                , getParticipant(), message.getBody());
                    }
                }
            });
        }
        return chat;
    }
    
    private synchronized void reconnect() throws XMPPException {
        if (!connection.isConnected()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reconnecting to: {}", XmppEndpoint.getConnectionMessage(connection));
            }
            connection.connect();
        }
    }

    @Override
    protected void doStart() throws Exception {
        if (connection == null) {
            try {
                connection = endpoint.createConnection();
            } catch (XMPPException e) {
                if (endpoint.isTestConnectionOnStartup()) {
                    throw new RuntimeException("Could not establish connection to XMPP server:  " + endpoint.getConnectionDescription(), e);
                } else {
                    LOG.warn("Could not connect to XMPP server. {}  Producer will attempt lazy connection when needed.", XmppEndpoint.getXmppExceptionLogMessage(e));
                }
            }
        }
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        connection = null;
        super.doStop();
    }

    // Properties
    // -------------------------------------------------------------------------

    public String getParticipant() {
        return participant;
    }
}
