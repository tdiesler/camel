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
package org.apache.camel.component.atom;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.component.feed.FeedComponent;
import org.apache.camel.component.feed.FeedEndpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * An <a href="http://activemq.apache.org/camel/atom.html">Atom Component</a>.
 * <p/>
 * Camel uses Apache Abdera as the Atom implementation. 
 *
 * @version $Revision$
 */
public class AtomComponent extends FeedComponent {

    @Override
    protected FeedEndpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        return new AtomEndpoint(uri, this, remaining);
    }

}
