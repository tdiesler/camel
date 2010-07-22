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

import java.util.List;

import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP consumer
 */
public class FtpConsumer extends RemoteFileConsumer<FTPFile> {

    protected String endpointPath;

    public FtpConsumer(RemoteFileEndpoint<FTPFile> endpoint, Processor processor, RemoteFileOperations<FTPFile> fileOperations) {
        super(endpoint, processor, fileOperations);
        this.endpointPath = endpoint.getConfiguration().getDirectory();
    }

    protected boolean pollDirectory(String fileName, List<GenericFile<FTPFile>> fileList) {
        // strip trailing slash
        fileName = FileUtil.stripTrailingSeparator(fileName);
        return pollDirectory(fileName, null, fileList);
    }

    protected boolean pollDirectory(String absolutePath, String dirName, List<GenericFile<FTPFile>> fileList) {
        // must remember current dir so we stay in that directory after the poll
        String currentDir = operations.getCurrentDirectory();

        boolean answer = doPollDirectory(absolutePath, dirName, fileList);

        operations.changeCurrentDirectory(currentDir);
        return answer;
    }

    protected boolean doPollDirectory(String absolutePath, String dirName, List<GenericFile<FTPFile>> fileList) {
        if (log.isTraceEnabled()) {
            log.trace("doPollDirectory from absolutePath: " + absolutePath + ", dirName: " + dirName);
        }

        // remove trailing /
        dirName = FileUtil.stripTrailingSeparator(dirName);
        String dir = ObjectHelper.isNotEmpty(dirName) ? dirName : absolutePath;

        // change into directory (to ensure most FTP servers can list files)
        operations.changeCurrentDirectory(dir);

        if (log.isTraceEnabled()) {
            log.trace("Polling directory: " + dir);
        }
        List<FTPFile> files = operations.listFiles();
        if (files == null || files.isEmpty()) {
            // no files in this directory to poll
            if (log.isTraceEnabled()) {
                log.trace("No files found in directory: " + dir);
            }
            return true;
        } else {
            // we found some files
            if (log.isTraceEnabled()) {
                log.trace("Found " + files.size() + " in directory: " + dir);
            }
        }

        for (FTPFile file : files) {

            // check if we can continue polling in files
            if (!canPollMoreFiles(fileList)) {
                return false;
            }

            if (file.isDirectory()) {
                RemoteFile<FTPFile> remote = asRemoteFile(absolutePath, file);
                if (endpoint.isRecursive() && isValidFile(remote, true)) {
                    // recursive scan and add the sub files and folders
                    String subDirectory = file.getName();
                    String path = absolutePath + "/" + subDirectory;
                    boolean canPollMore = pollDirectory(path, subDirectory, fileList);
                    if (!canPollMore) {
                        return false;
                    }
                }
            } else if (file.isFile()) {
                RemoteFile<FTPFile> remote = asRemoteFile(absolutePath, file);
                if (isValidFile(remote, false)) {
                    if (isInProgress(remote)) {
                        if (log.isTraceEnabled()) {
                            log.trace("Skipping as file is already in progress: " + remote.getFileName());
                        }
                    } else {
                        // matched file so add
                        fileList.add(remote);
                    }
                }
            } else {
                log.debug("Ignoring unsupported remote file type: " + file);
            }
        }

        return true;
    }

    private RemoteFile<FTPFile> asRemoteFile(String absolutePath, FTPFile file) {
        RemoteFile<FTPFile> answer = new RemoteFile<FTPFile>();

        answer.setEndpointPath(endpointPath);
        answer.setFile(file);
        answer.setFileName(file.getName());
        answer.setFileNameOnly(file.getName());
        answer.setFileLength(file.getSize());
        if (file.getTimestamp() != null) {
            answer.setLastModified(file.getTimestamp().getTimeInMillis());
        }
        answer.setHostname(((RemoteFileConfiguration) endpoint.getConfiguration()).getHost());

        // all ftp files is considered as relative
        answer.setAbsolute(false);

        // create a pseudo absolute name
        String dir = FileUtil.stripTrailingSeparator(absolutePath);
        String absoluteFileName = dir + "/" + file.getName();
        answer.setAbsoluteFilePath(absoluteFileName);

        // the relative filename, skip the leading endpoint configured path
        String relativePath = ObjectHelper.after(absoluteFileName, endpointPath);
        // skip leading /
        relativePath = FileUtil.stripLeadingSeparator(relativePath);
        answer.setRelativeFilePath(relativePath);

        return answer;
    }

}
