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

package org.apache.camel.component.cxf.headers;

import java.util.Arrays;
import java.util.List;

import org.apache.cxf.headers.Header;

public class DefaultMessageHeadersRelay implements MessageHeadersRelay {
    
    public static final String ACTIVATION_NAMESPACE = "http://cxf.apache.org/bindings";
    public static final List<String> ACTIVATION_NAMESPACES = Arrays.asList(ACTIVATION_NAMESPACE);

    public DefaultMessageHeadersRelay() {
    }

    public List<String> getActivationNamespaces() {
        return ACTIVATION_NAMESPACES;
    }
    
    /*
     * simply relay all headers  
     */
    public void relay(Direction direction, List<Header> from, List<Header> to) {
        for (Header header : from) {
            to.add(header);
        }
    }
}
