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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Remote file operations based on some backing framework
 */
public interface RemoteFileOperations<T> {

    /**
     * Connects to the remote server
     *
     * @param configuration configuraiton
     * @return true if connected
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean connect(RemoteFileConfiguration configuration) throws RemoteFileOperationFailedException;

    /**
     * Returns whether we are connected to the remote server or not
     *
     * @return true if connected, false if not
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean isConnected() throws RemoteFileOperationFailedException;

    /**
     * Discconects from the remote server
     *
     * @throws RemoteFileOperationFailedException can be thrown
     */
    void disconnect() throws RemoteFileOperationFailedException;

    /**
     * Deletes the file from the remote server
     *
     * @param name name of the file
     * @return true if deleted, false if not
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean deleteFile(String name) throws RemoteFileOperationFailedException;

    /**
     * Renames the file on the remote server
     *
     * @param from original name
     * @param to   the new name
     * @return true if renamed, false if not
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean renameFile(String from, String to) throws RemoteFileOperationFailedException;

    /**
     * Builds the directory structure on the remote server. Will test if the folder already exists.
     *
     * @param directory the directory path to build
     * @return true if build or already exists, false if not possbile (could be lack of permissions)
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean buildDirectory(String directory) throws RemoteFileOperationFailedException;

    /**
     * Retrieves the remote file (download)
     *
     * @param name  name of the file
     * @param out   stream to write the content of the file into
     * @return true if file has been retrieved, false if not
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean retrieveFile(String name, OutputStream out) throws RemoteFileOperationFailedException;

    /**
     * Stores the content as a new remote file (upload)
     *
     * @param name  name of new file
     * @param body  content of the file
     * @return true if the file was stored, false if not
     * @throws RemoteFileOperationFailedException can be thrown
     */
    boolean storeFile(String name, InputStream body) throws RemoteFileOperationFailedException;

    /**
     * Gets the current remote directory
     * @return the current directory path
     * @throws RemoteFileOperationFailedException can be thrown
     */
    String getCurrentDirectory() throws RemoteFileOperationFailedException;

    /**
     * Change the current remote directory
     *
     * @param path the path to change to
     * @throws RemoteFileOperationFailedException can be thrown
     */
    void changeCurrentDirectory(String path) throws RemoteFileOperationFailedException;

    /**
     * List the files in the current remote directory
     *
     * @return a list of backing objects representing the files
     * @throws RemoteFileOperationFailedException can be thrown
     */
    List listFiles() throws RemoteFileOperationFailedException;

    /**
     * List the files in the given remote directory
     *
     * @param path  the remote directory
     * @return a list of backing objects representing the files
     * @throws RemoteFileOperationFailedException can be thrown
     */
    List listFiles(String path) throws RemoteFileOperationFailedException;

}
