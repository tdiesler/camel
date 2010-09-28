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
package org.apache.camel.component.quickfixj.examples.transform;

import org.apache.camel.Exchange;
import org.apache.camel.component.quickfixj.QuickfixjEndpoint;

import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;

public class QuickfixjEventJsonTransformer {
    private final QuickfixjMessageJsonTransformer renderer;
    
    public QuickfixjEventJsonTransformer() throws ConfigError {
        renderer = new QuickfixjMessageJsonTransformer();
    }
    
    public String transform(Exchange exchange) {
        SessionID sessionID = (SessionID) exchange.getIn().getHeader(QuickfixjEndpoint.SESSION_ID_KEY);
        Session session = Session.lookupSession(sessionID);
        DataDictionary dataDictionary = session.getDataDictionary();
        
        if (dataDictionary == null) {
            throw new IllegalStateException("No Data Dictionary. Exchange must reference an existing session");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("\"event\": {\n");
        
        org.apache.camel.Message in = exchange.getIn();
        for (String key : in.getHeaders().keySet()) {
            sb.append("  \"").append(key).append("\": ").append(in.getHeader(key)).append(",\n");                
        }
        
        sb.append(renderer.transform(in.getBody(Message.class), "  ", dataDictionary)).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}