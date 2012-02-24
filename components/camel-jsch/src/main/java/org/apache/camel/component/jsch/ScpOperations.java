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
package org.apache.camel.component.jsch;

import static org.apache.camel.util.ObjectHelper.isNotEmpty;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.camel.component.file.remote.RemoteFileOperations;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SCP remote file operations
 */
public class ScpOperations implements RemoteFileOperations<ScpFile> {
    private static final String DEFAULT_KNOWN_HOSTS = "META-INF/.ssh/known_hosts";
    private static final transient Logger LOG = LoggerFactory.getLogger(ScpOperations.class);

    private ScpEndpoint endpoint;
    private Session session;
    private ChannelExec channel;

    @Override
    public void setEndpoint(GenericFileEndpoint<ScpFile> endpoint) {
        this.endpoint = (ScpEndpoint)endpoint;
    }

    @Override
    public boolean deleteFile(String name) throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'delete' not supported by the scp: protocol");
    }

    @Override
    public boolean existsFile(String name) throws GenericFileOperationFailedException {
        // maybe... cannot determine using the scp: protocol
        return false;
    }

    @Override
    public boolean renameFile(String from, String to) throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'rename' not supported by the scp: protocol");
    }

    @Override
    public boolean buildDirectory(String directory, boolean absolute) throws GenericFileOperationFailedException {
        // done by the server
        return true;
    }

    @Override
    public boolean retrieveFile(String name, Exchange exchange) throws GenericFileOperationFailedException {
        // TODO: implement
        return false;
    }

    @Override
    public boolean storeFile(String name, Exchange exchange) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(session, "session");
        ScpConfiguration cfg = endpoint.getConfiguration();
        
        int timeout = cfg.getConnectTimeout();
        LOG.trace("Opening channel to {} with {} timeout...", cfg.remoteServerInformation(), 
            timeout > 0 ? (Integer.toString(timeout) + " ms") : "no");
        String target = getRemoteTarget(cfg);
        String file = getRemoteFile(name, cfg);
        try {
            channel = (ChannelExec) session.openChannel("exec");
            // TODO: need config for scp *-p* (preserves modification times, access times, and modes from the original file)
            // String command="scp " + (ptimestamp ? "-p " : "") + "-t " + configuration.getDirectory();
            // TODO: refactor to use generic command
            String command="scp -t " + target;
            channel.setCommand(command);
            channel.connect(timeout);
            LOG.trace("Channel connected to {}", cfg.remoteServerInformation());

            try {
                writeFile(channel, file, "foo");
            } catch (IOException e) {
                throw new GenericFileOperationFailedException("Failed to write file " + file, e);
            }
        } catch (JSchException e) {
            LOG.warn("Failed to secure copy file " + file, e);
            return false;
        } finally {
            if (channel != null) {
                LOG.trace("Disconnecting 'exec' scp channel");
                channel.disconnect();
                channel = null;
                LOG.trace("Channel disconnected from {}", cfg.remoteServerInformation());
            }
        }
        return true;
    }

    @Override
    public String getCurrentDirectory() throws GenericFileOperationFailedException {
        return endpoint.getConfiguration().getDirectory();
    }

    @Override
    public void changeCurrentDirectory(String path) throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'cd " + path + "' not supported by the scp: protocol");
    }

    @Override
    public void changeToParentDirectory() throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'cd ..' not supported by the scp: protocol");
    }

    @Override
    public List<ScpFile> listFiles() throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'ls' not supported by the scp: protocol");
    }

    @Override
    public List<ScpFile> listFiles(String path) throws GenericFileOperationFailedException {
        throw new GenericFileOperationFailedException("Operation 'ls " + path + "' not supported by the scp: protocol");
    }

    @Override
    public boolean connect(RemoteFileConfiguration configuration) throws GenericFileOperationFailedException {
        if (!isConnected()) {
            session = createSession(configuration instanceof ScpConfiguration ? (ScpConfiguration)configuration : null);
            // TODO: deal with reconnection attempts
            if (!isConnected()) {
                session = null;
                throw new GenericFileOperationFailedException("Failed to connect to " + configuration.remoteServerInformation());
            }
        }
        return true;
    }

    @Override
    public boolean isConnected() throws GenericFileOperationFailedException {
        return session != null && session.isConnected();
    }

    @Override
    public void disconnect() throws GenericFileOperationFailedException {
        if (isConnected()) {
            session.disconnect();
        }
        session = null;
    }

    @Override
    public boolean sendNoop() throws GenericFileOperationFailedException {
        // not supported by scp:
        return true;
    }

    @Override
    public boolean sendSiteCommand(String command) throws GenericFileOperationFailedException {
        // TODO: not really used, maybe implement at a later time
        return true;
    }
    
    private Session createSession(ScpConfiguration config) {
        ObjectHelper.notNull(config, "ScpConfiguration");
        try {
            final JSch jsch = new JSch();
            // get from configuration
            if (isNotEmpty(config.getCiphers())) {
                LOG.debug("Using ciphers: {}", config.getCiphers());
                Hashtable<String, String> ciphers = new Hashtable<String, String>();
                ciphers.put("cipher.s2c", config.getCiphers());
                ciphers.put("cipher.c2s", config.getCiphers());
                JSch.setConfig(ciphers);
            }
            

            
            
            String knownHostsFile = config.getKnownHostsFile();
            jsch.setKnownHosts(ObjectHelper.isEmpty(knownHostsFile) ? DEFAULT_KNOWN_HOSTS : knownHostsFile);
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setTimeout(config.getTimeout());
            session.setUserInfo(new SessionUserInfo(config));

            int timeout = config.getConnectTimeout();
            LOG.debug("Connecting to {} with {} timeout...", config.remoteServerInformation(),
                timeout > 0 ? (Integer.toString(timeout) + " ms") : "no");
            if (timeout > 0) {
                session.connect(timeout);
            } else {
                session.connect();
            }
        } catch (JSchException e) {
            session = null;
            LOG.warn("Could not create ssh session for " + config.remoteServerInformation(), e);
        }
        return session;
    }
    
    private void writeFile(ChannelExec c, String name, String data) throws IOException {
        data = "Hello World";
        OutputStream os = c.getOutputStream();
        InputStream is = c.getInputStream();

        os.write(("C7777 " + data.length() + " " + name + "\n").getBytes());
        os.flush();
        is.read();
        
        os.write(data.getBytes());
        os.flush();
        is.read();
        
        os.write(0);
        os.flush();
        os.close();
        is.close();
    }
    
    private static String getRemoteTarget(ScpConfiguration config) {
        // use current dir (".") if target directory not specified in uri
        return config.getDirectory().isEmpty() ? "." : config.getDirectory();
    }

    private static String getRemoteFile(String name, ScpConfiguration config) {
        // assume that the directory path of 'name' is the same as config.getDirectory()
        int pos = name.lastIndexOf('/');
        return pos >= 0 ? name.substring(pos + 1) : name;
    }


    protected static final class SessionUserInfo implements UserInfo, UIKeyboardInteractive {
        private final ScpConfiguration config;
        public SessionUserInfo(ScpConfiguration config) {
            ObjectHelper.notNull(config, "config");
            this.config = config;
        }
        
        @Override
        public String getPassphrase() {
            LOG.info("Private Key authentication not supported");
            return null;
        }
        @Override
        public String getPassword() {
            LOG.debug("Providing password for ssh authentication of user '{}'", config.getUsername());
            return config.getPassword();
        }
        @Override
        public boolean promptPassword(String message) {
            LOG.debug(message);
            return true;
        }
        @Override
        public boolean promptPassphrase(String message) {
            LOG.debug(message);
            return true;
        }
        @Override
        public boolean promptYesNo(String message) {
            LOG.debug(message);
            return false;
        }
        @Override
        public void showMessage(String message) {
            LOG.debug(message);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, 
            String instruction, String[] prompt, boolean[] echo) {
            LOG.debug(instruction);
            // Called for either SSH_MSG_USERAUTH_INFO_REQUEST or SSH_MSG_USERAUTH_PASSWD_CHANGEREQ
            // The most secure choice (especially for the second case) is to return null
            return null;
        }
    }
}
