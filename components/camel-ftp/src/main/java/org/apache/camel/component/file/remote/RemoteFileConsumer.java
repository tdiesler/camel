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

import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

public abstract class RemoteFileConsumer<T extends RemoteFileExchange> extends ScheduledPollConsumer<T> {
    RemoteFileEndpoint<T> endpoint;

    public RemoteFileConsumer(RemoteFileEndpoint<T> endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    public RemoteFileConsumer(RemoteFileEndpoint<T> endpoint, Processor processor, ScheduledExecutorService executor) {
        super(endpoint, processor, executor);
    }
}
