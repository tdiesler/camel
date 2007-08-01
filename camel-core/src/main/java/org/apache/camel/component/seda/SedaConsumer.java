/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.seda;

import org.apache.camel.AlreadyStoppedException;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ServiceSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$
 */
public class SedaConsumer<E extends Exchange> extends ServiceSupport implements Consumer<E>, Runnable {
    private static final Log log = LogFactory.getLog(SedaConsumer.class);

    private SedaEndpoint<E> endpoint;
    private Processor processor;
    private Thread thread;

    public SedaConsumer(SedaEndpoint<E> endpoint, Processor processor) {
        this.endpoint = endpoint;
        this.processor = processor;
    }

    @Override
    public String toString() {
        return "QueueConsumer: " + endpoint.getEndpointUri();
    }

    public void run() {
        while (!isStopping()) {
            E exchange;
            try {
                exchange = endpoint.getQueue().poll(1000, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                break;
            }
            if (exchange != null && !isStopping()) {
                try {
                    processor.process(exchange);
                }
                catch (AlreadyStoppedException e) {
                    log.debug("Ignoring failed message due to shutdown: " + e, e);
                    break;
                }
                catch (Throwable e) {
                    log.error(e);
                }
            }
        }
    }

    protected void doStart() throws Exception {
        thread = new Thread(this, getThreadName(endpoint.getEndpointUri()));
        thread.setDaemon(true);
        thread.start();
    }

    protected void doStop() throws Exception {
        thread.join();
    }

}
