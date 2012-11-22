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
package org.apache.camel.component.xmlrpc;
//START SNIPPET: e1
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class MyClientConfigurer implements XmlRpcClientConfigurer {

    @Override
    public void configureXmlRpcClient(XmlRpcClient client) {
        // get the configure first
        XmlRpcClientConfigImpl clientConfig = (XmlRpcClientConfigImpl)client.getClientConfig();
        // change the value of clientConfig
        clientConfig.setEnabledForExtensions(true);
        // set the option on the XmlRpcClient
        client.setMaxThreads(10);
    }

}
//END SNIPPET: e1
