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
package org.apache.camel.spring.example;

import org.apache.camel.Consume;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
public class MyCamel1Consumer {

    private static final Log LOG = LogFactory.getLog(MyCamel1Consumer.class);

    @EndpointInject(uri = "mock:result", context = "camel-1")
    private ProducerTemplate destination;

    @Consume(uri = "direct:start", context = "camel-1")
    public void doSomething(String body) {
        ObjectHelper.notNull(destination, "destination");

        LOG.info("Received body: " + body);
        destination.sendBody(body);
    }

    public ProducerTemplate getDestination() {
        return destination;
    }

    public void setDestination(ProducerTemplate destination) {
        this.destination = destination;
    }

}
