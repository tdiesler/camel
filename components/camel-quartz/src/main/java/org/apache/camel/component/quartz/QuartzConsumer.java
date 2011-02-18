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
package org.apache.camel.component.quartz;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 * @version 
 */
public class QuartzConsumer extends DefaultConsumer {

    public QuartzConsumer(QuartzEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public QuartzEndpoint getEndpoint() {
        return (QuartzEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        getEndpoint().consumerStarted(this);
    }

    @Override
    protected void doStop() throws Exception {
        getEndpoint().consumerStopped(this);
        super.doStop();
    }
}
