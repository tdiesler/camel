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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileExchange;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP remote file operations
 */
public class FtpRemoteFileOperations implements RemoteFileOperations<FTPFile> {

    private static final transient Log LOG = LogFactory.getLog(FtpRemoteFileOperations.class);

    private final FTPClient client;

    public FtpRemoteFileOperations() {
        this.client = new FTPClient();
    }

    public FtpRemoteFileOperations(FTPClient client) {
        this.client = client;
    }

    public boolean connect(RemoteFileConfiguration config) throws GenericFileOperationFailedException {
        String host = config.getHost();
        int port = config.getPort();
        String username = config.getUsername();

        if (config.getFtpClientConfig() != null) {
            LOG.trace("Configuring FTPFile with config: " + config.getFtpClientConfig());
            client.configure(config.getFtpClientConfig());
        }

        LOG.trace("Connecting to " + config.remoteServerInformation());
        try {
            client.connect(host, port);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }

        // must enter passive mode directly after connect
        if (config.isPassiveMode()) {
            LOG.trace("Using passive mode connections");
            client.enterLocalPassiveMode();
        }

        try {
            boolean login;
            if (username != null) {
                LOG.trace("Attempting to login user: " + username);
                login = client.login(username, config.getPassword());
            } else {
                LOG.trace("Attempting to login anonymousl");
                login = client.login("anonymous", null);
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("User " + (username != null ? username : "anonymous") + " logged in: " + login);
            }
            if (!login) {
                throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString());
            }
            client.setFileType(config.isBinary() ? FTPClient.BINARY_FILE_TYPE : FTPClient.ASCII_FILE_TYPE);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }

        return true;
    }

    public boolean isConnected() throws GenericFileOperationFailedException {
        return client.isConnected();
    }

    public void disconnect() throws GenericFileOperationFailedException {
        try {
            client.disconnect();
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean deleteFile(FTPClient client, String name) throws GenericFileOperationFailedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleteing file: " + name);
        }
        try {
            return client.deleteFile(name);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean renameFile(String from, String to) throws GenericFileOperationFailedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Renaming file: " + from + " to: " + to);
        }
        try {
            return client.rename(from, to);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean buildDirectory(String directory, boolean absolute) throws GenericFileOperationFailedException {
        try {
            String originalDirectory = client.printWorkingDirectory();

            boolean success = false;
            try {
                // maybe the full directory already exsits
                success = client.changeWorkingDirectory(directory);
                if (!success) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Trying to build remote directory: " + directory);
                    }
                    success = client.makeDirectory(directory);
                    if (!success) {
                        // we are here if the server side doesn't create
                        // intermediate folders
                        // so create the folder one by one
                        buildDirectoryChunks(directory);
                    }
                }
            } finally {
                // change back to original directory
                client.changeWorkingDirectory(originalDirectory);
            }

            return success;
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean retrieveFile(String name, GenericFileExchange<FTPFile> exchange) throws GenericFileOperationFailedException {
        try {
            GenericFile<FTPFile> target = exchange.getGenericFile();
            OutputStream os = new ByteArrayOutputStream();
            target.setBody(os);
            return client.retrieveFile(name, os);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public boolean storeFile(String name, GenericFileExchange<FTPFile> exchange) throws GenericFileOperationFailedException {
        try {
            return client.storeFile(name, exchange.getIn().getBody(InputStream.class));
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public String getCurrentDirectory() throws GenericFileOperationFailedException {
        try {
            return client.printWorkingDirectory();
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public void changeCurrentDirectory(String newDirectory) throws GenericFileOperationFailedException {
        try {
            client.changeWorkingDirectory(newDirectory);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    public List listFiles() throws GenericFileOperationFailedException {
        return listFiles(".");
    }

    public List listFiles(String path) throws GenericFileOperationFailedException {
        // use current directory if path not given
        if (ObjectHelper.isEmpty(path)) {
            path = ".";
        }

        try {
            final List list = new ArrayList();
            FTPFile[] files = client.listFiles(path);
            for (FTPFile file : files) {
                list.add(file);
            }
            return list;
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException(client.getReplyCode(), client.getReplyString(), e.getMessage(), e);
        }
    }

    private boolean buildDirectoryChunks(String dirName) throws IOException {
        final StringBuilder sb = new StringBuilder(dirName.length());
        final String[] dirs = dirName.split("\\/");

        boolean success = false;
        for (String dir : dirs) {
            sb.append(dir).append('/');
            String directory = sb.toString();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Trying to build remote directory: " + directory);
            }

            success = client.makeDirectory(directory);
        }

        return success;
    }

    public FTPClient changeCurrentDirectory(FTPClient client, String path) throws GenericFileOperationFailedException {
        try {
            client.changeWorkingDirectory(path);
            return client;
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException("Failed to delete [" + path + "]", e);
        }
    }

    public boolean deleteFile(String name) throws GenericFileOperationFailedException {
        try {
            return this.client.deleteFile(name);
        } catch (IOException e) {
            throw new RemoteFileOperationFailedException("Failed to delete [" + name + "]", e);
        }
    }

}
