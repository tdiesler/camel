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
package org.apache.camel.component.seda;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.seda.SedaEndpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An implementation of the <a href="http://activemq.apache.org/camel/queue.html">Queue components</a>
 * for asynchronous SEDA exchanges on a {@link BlockingQueue} within a CamelContext
 *
 * @org.apache.xbean.XBean
 * @version $Revision: 519973 $
 */
public class QueueComponent<E extends Exchange> extends SedaComponent<E> {
    private static final transient Log log = LogFactory.getLog(QueueComponent.class);

    public QueueComponent() {
        log.warn("This component has been deprecated; please use the seda: URI format instead of queue:");
    }
}
