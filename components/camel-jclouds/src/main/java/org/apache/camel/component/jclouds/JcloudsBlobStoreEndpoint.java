/*
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

package org.apache.camel.component.jclouds;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.jclouds.blobstore.BlobStoreContext;


public class JcloudsBlobStoreEndpoint extends JcloudsEndpoint {

    private String container;

    private BlobStoreContext blobStoreContext;

    /**
     * Constructor
     *
     * @param uri
     * @param component
     * @param blobStoreContext
     */
    public JcloudsBlobStoreEndpoint(String uri, JcloudsComponent component, BlobStoreContext blobStoreContext,String container) {
        super(uri, component);
        this.blobStoreContext=blobStoreContext;
        this.container=container;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new JcloudsBlobStoreProducer(this,blobStoreContext,container);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        return new JcloudsBlobStoreConsumer(this,processor,blobStoreContext,container);
    }
}
