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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

/**
 * @version $Revision: 1.1 $
 */
public class CamelApplication implements Application {
    private static final transient Log LOG = LogFactory.getLog(CamelApplication.class);
    private final FixEndpoint endpoint;

    public CamelApplication(FixEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void onCreate(SessionID sessionID) {
        endpoint.setSessionID(sessionID);
    }

    public void onLogon(SessionID sessionID) {
        endpoint.setSessionID(sessionID);
    }

    public void onLogout(SessionID sessionID) {
        // TODO

    }

    public void toAdmin(Message message, SessionID sessionID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toAdmin() session: " + sessionID + " " + message);
        }
    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fromAdmin() session: " + sessionID + " " + message);
        }
    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toApp() session: " + sessionID + " " + message);
        }

        endpoint.onMessage(message);
    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fromApp() session: " + sessionID + " " + message);
        }
    }
}
