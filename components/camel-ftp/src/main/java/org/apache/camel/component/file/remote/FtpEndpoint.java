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

import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFileConfiguration;
import org.apache.camel.component.file.GenericFileProducer;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP endpoint
 */
public class FtpEndpoint extends RemoteFileEndpoint<FTPFile> {

    public FtpEndpoint() {
    }

    public FtpEndpoint(String uri, FtpComponent component, RemoteFileConfiguration configuration) {
        super(uri, component, configuration);
    }

    @Override
    protected RemoteFileConsumer<FTPFile> buildConsumer(Processor processor) {
        return new FtpConsumer(this, processor, createRemoteFileOperations());
    }

    protected GenericFileProducer<FTPFile> buildProducer() {
        return new RemoteFileProducer<FTPFile>(this, createRemoteFileOperations());
    }
    
    protected RemoteFileOperations<FTPFile> createRemoteFileOperations() {
        FtpOperations operations = new FtpOperations();
        operations.setEndpoint(this);
        return operations;
    }

    @Override
    public String getScheme() {
        return "ftp";
    }
}
