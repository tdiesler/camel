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
package org.apache.camel.component.cxf;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * @version $Revision$
 */
public class CxfProducerOperationTest extends CxfProducerTest {
    private static final String NAMESPACE = "http://apache.org/hello_world_soap_http";
   
    protected String getSimpleEndpointUri() {
        return "cxf://" + SIMPLE_SERVER_ADDRESS
            + "?serviceClass=org.apache.camel.component.cxf.HelloService" 
            + "&defaultOperationName=" + ECHO_OPERATION;
    }

    protected String getJaxwsEndpointUri() {
        return "cxf://" + JAXWS_SERVER_ADDRESS
            + "?serviceClass=org.apache.hello_world_soap_http.Greeter"
            + "&defaultOperationName=" + GREET_ME_OPERATION
            + "&defaultOperationNamespace=" + NAMESPACE;
    }

    protected Exchange sendSimpleMessage() {
        return sendSimpleMessage(getSimpleEndpointUri());
    }

    private Exchange sendSimpleMessage(String endpointUri) {
        Exchange exchange = template.send(endpointUri, new Processor() {
            public void process(final Exchange exchange) {
                final List<String> params = new ArrayList<String>();
                params.add(TEST_MESSAGE);
                exchange.getIn().setBody(params);
                exchange.getIn().setHeader(Exchange.FILE_NAME, "testFile");
            }
        });
        return exchange;

    }
    protected Exchange sendJaxWsMessage() {
        Exchange exchange = template.send(getJaxwsEndpointUri(), new Processor() {
            public void process(final Exchange exchange) {
                final List<String> params = new ArrayList<String>();
                params.add(TEST_MESSAGE);
                exchange.getIn().setBody(params);
                exchange.getIn().setHeader(Exchange.FILE_NAME, "testFile");
            }
        });
        return exchange;
    }
}
