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
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.net.ftp.FTPFile;

public class FtpRemoteFileEndpoint extends RemoteFileEndpoint<FTPFile> {

    public FtpRemoteFileEndpoint(String uri, FtpRemoteFileComponent component, FtpRemoteFileOperations operations,
                                 RemoteFileConfiguration configuration) {
        super(uri, component, operations, configuration);

    }

    @Override
    protected RemoteFileConsumer buildConsumer(Processor processor, RemoteFileOperations<FTPFile> operations) {
        return new FtpConsumer(this, processor, operations);
    }

    @Override
    public RemoteFileConsumer createConsumer(Processor processor) throws Exception {
        RemoteFileConsumer rfc = super.createConsumer(processor);
        ObjectHelper.notEmpty(((FtpRemoteFileConfiguration) configuration).getHost(), "host");
        return rfc;
    }

    @Override
    public String getScheme() {
        return "ftp";
    }

}
