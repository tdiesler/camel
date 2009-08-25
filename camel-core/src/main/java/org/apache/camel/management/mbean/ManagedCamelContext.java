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
package org.apache.camel.management.mbean;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.ServiceSupport;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @version $Revision$
 */
@ManagedResource(description = "Managed CamelContext")
public class ManagedCamelContext {

    private CamelContext context;
    private ProducerTemplate template;

    public ManagedCamelContext(CamelContext context) {
        this.context = context;
    }

    public CamelContext getContext() {
        return context;
    }

    @ManagedAttribute(description = "Name")
    public String getName() {
        return context.getName();
    }

    @ManagedAttribute(description = "Camel running state")
    public boolean isStarted() {
        return ((ServiceSupport) context).isStarted();
    }

    @ManagedOperation(description = "Start Camel")
    public void start() throws Exception {
        context.start();
    }

    @ManagedOperation(description = "Stop Camel")
    public void stop() throws Exception {
        context.stop();
    }

    @ManagedOperation(description = "Send body (in only)")
    public void sendBody(String endpointUri, String body) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody(endpointUri, body);
        template.stop();
    }

    @ManagedOperation(description = "Request body (in out)")
    public Object requestBody(String endpointUri, String body) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();
        Object answer = template.requestBody(endpointUri, body);
        template.stop();
        return answer;
    }




}
