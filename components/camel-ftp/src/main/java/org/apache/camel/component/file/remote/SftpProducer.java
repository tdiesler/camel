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
package org.apache.camel.component.file.remote;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SftpProducer extends RemoteFileProducer<RemoteFileExchange> {
    private static final transient Log LOG = LogFactory.getLog(SftpProducer.class);

    private SftpEndpoint endpoint;
    private ChannelSftp channel;
    private Session session;

    public SftpProducer(SftpEndpoint endpoint, Session session) {
        super(endpoint);
        this.endpoint = endpoint;
        this.session = session;
    }

    protected void connectIfNecessary() throws JSchException {
        if (channel == null || !channel.isConnected()) {
            if (session == null || !session.isConnected()) {
                LOG.debug("Session isn't connected, trying to recreate and connect.");
                session = endpoint.createSession();
                session.connect();
            }
            LOG.debug("Channel isn't connected, trying to recreate and connect.");
            channel = endpoint.createChannelSftp(session);
            channel.connect();
            LOG.info("Connected to " + endpoint.getConfiguration().remoteServerInformation());
        }
    }

    protected void disconnect() throws JSchException {
        if (session != null) {
            LOG.debug("Session is being explicitly disconnected");
            session.disconnect();
        }
        if (channel != null) {
            LOG.debug("Channel is being explicitly disconnected");
            channel.disconnect();
        }
    }

    public void process(Exchange exchange) throws Exception {
        connectIfNecessary();
        // If the attempt to connect isn't successful, then the thrown
        // exception will signify that we couldn't deliver
        try {
            process(endpoint.createExchange(exchange));
        } catch (JSchException e) {
            // If the connection has gone stale, then we must manually disconnect
            // the client before attempting to reconnect
            LOG.warn("Disconnecting due to exception: " + e.getMessage());
            disconnect();
            // Rethrow to signify that we didn't deliver
            throw e;
        } catch (SftpException e) {
            // Still not sure if/when these come up and what we should do about them
            // client.disconnect();
            LOG.warn("Caught SftpException:" + e.getMessage(), e);
            LOG.warn("Hoping an explicit disconnect/reconnect will solve the problem");
            disconnect();
            // Rethrow to signify that we didn't deliver
            throw e;
        }
    }

    public void process(RemoteFileExchange exchange) throws Exception {
        InputStream payload = exchange.getIn().getBody(InputStream.class);
        try {
            String remoteServer = endpoint.getConfiguration().remoteServerInformation();
            String fileName = createFileName(exchange.getIn(), endpoint.getConfiguration());

            int lastPathIndex = fileName.lastIndexOf('/');
            if (lastPathIndex != -1) {
                String directory = fileName.substring(0, lastPathIndex);
                boolean success = buildDirectory(channel, directory);
                if (!success) {
                    LOG.warn("Couldn't build directory: " + directory + " (could be because of denied permissions)");
                }
            }

            channel.put(payload, fileName);

            if (LOG.isInfoEnabled()) {
                LOG.info("Sent: " + fileName + " to: " + remoteServer);
            }
        } finally {
            if (payload != null) {
                payload.close();
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        LOG.info("Starting");
        try {
            connectIfNecessary();
        } catch (JSchException e) {
            LOG.warn("Couldn't connect to: " + endpoint.getConfiguration().remoteServerInformation());
        }
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        LOG.info("Stopping");
        disconnect();
        super.doStop();
    }

    protected static boolean buildDirectory(ChannelSftp sftpClient, String dirName)
        throws IOException, SftpException {
        String originalDirectory = sftpClient.pwd();

        boolean success = false;
        try {
            // maybe the full directory already exsits
            try {
                sftpClient.cd(dirName);
                success = true;
            } catch (SftpException e) {
                // ignore, we could not change directory so try to create it instead
            }

            if (!success) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying to build remote directory: " + dirName);
                }
                sftpClient.mkdir(dirName);
                success = true;
            }
        } finally {
            // change back to original directory
            sftpClient.cd(originalDirectory);
        }

        return success;
    }

}