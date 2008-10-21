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
package org.apache.camel.fix;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.LogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.MessageStoreFactory;

/**
 * @version $Revision$
 */
public class FixServerEndpoint extends FixEndpoint {
    private SocketAcceptor acceptor;

    public FixServerEndpoint(String uri, CamelContext camelContext, String resourceUri) {
        super(uri, camelContext, resourceUri);
    }

    public FixServerEndpoint(String uri, Component component, String resourceUri) {
        super(uri, component, resourceUri);
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if (acceptor != null) {
            acceptor.stop();
            acceptor = null;
        }
    }

    protected void login(SessionSettings settings, Application application, MessageStoreFactory storeFactory, LogFactory logFactory) throws ConfigError {
        acceptor = new SocketAcceptor(application, storeFactory, settings,
                logFactory, getMessageFactory());

        acceptor.start();
    }
}
