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
package org.apache.camel.component.irc;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventAdapter;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

public class IrcConsumer extends DefaultConsumer {
    private static final transient Log LOG = LogFactory.getLog(IrcConsumer.class);

    private final IrcConfiguration configuration;
    private final IrcEndpoint endpoint;
    private final IRCConnection connection;
    private FilteredIRCEventAdapter listener;

    public IrcConsumer(IrcEndpoint endpoint, Processor processor, IRCConnection connection) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.connection = connection;
        this.configuration = endpoint.getConfiguration();
    }

    @Override
    protected void doStop() throws Exception {
        if (connection != null) {
            for (String channel : endpoint.getConfiguration().getChannels()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Parting: " + channel);
                }
                connection.doPart(channel);
            }
            connection.removeIRCEventListener(listener);
        }
        super.doStop();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        listener = new FilteredIRCEventAdapter();
        connection.addIRCEventListener(listener);

        List<String> channels = endpoint.getConfiguration().getChannels();
        for (String channel : channels) {

            // find key for channel
            int ndx = channels.indexOf(channel);
            String key = null;
            if (ndx >= 0) {
                List<String> keys = endpoint.getConfiguration().getKeys();
                if (keys.size() > 0 && ndx < keys.size()) {
                    key = keys.get(ndx);
                }
            }

            if (ObjectHelper.isNotEmpty(key)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Joining: " + channel + " using " + connection.getClass().getName() + " with key " + key);
                }
                connection.doJoin(channel, key);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Joining: " + channel + " using " + connection.getClass().getName());
                }
                connection.doJoin(channel);
            }
        }
    }

    public IRCConnection getConnection() {
        return connection;
    }

    class FilteredIRCEventAdapter extends IRCEventAdapter {

        @Override
        public void onNick(IRCUser user, String newNick) {
            if (configuration.isOnNick()) {
                Exchange exchange = endpoint.createOnNickExchange(user, newNick);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onQuit(IRCUser user, String msg) {
            if (configuration.isOnQuit()) {
                Exchange exchange = endpoint.createOnQuitExchange(user, msg);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onJoin(String channel, IRCUser user) {
            if (configuration.isOnJoin()) {
                Exchange exchange = endpoint.createOnJoinExchange(channel, user);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onKick(String channel, IRCUser user, String passiveNick, String msg) {
            if (configuration.isOnKick()) {
                Exchange exchange = endpoint.createOnKickExchange(channel, user, passiveNick, msg);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onMode(String channel, IRCUser user, IRCModeParser modeParser) {
            if (configuration.isOnMode()) {
                Exchange exchange = endpoint.createOnModeExchange(channel, user, modeParser);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onPart(String channel, IRCUser user, String msg) {
            if (configuration.isOnPart()) {
                Exchange exchange = endpoint.createOnPartExchange(channel, user, msg);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onReply(int num, String value, String msg) {
            if (configuration.isOnReply()) {
                Exchange exchange = endpoint.createOnReplyExchange(num, value, msg);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onTopic(String channel, IRCUser user, String topic) {
            if (configuration.isOnTopic()) {
                Exchange exchange = endpoint.createOnTopicExchange(channel, user, topic);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }

        @Override
        public void onPrivmsg(String target, IRCUser user, String msg) {
            if (configuration.isOnPrivmsg()) {
                Exchange exchange = endpoint.createOnPrivmsgExchange(target, user, msg);
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }
    }

}
