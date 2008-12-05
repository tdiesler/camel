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
package org.apache.camel.component.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.util.Date;

import javax.naming.Context;

import org.apache.camel.Body;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.jndi.JndiContext;

public class RssEntrySortDefaultsTest extends RssEntrySortTest {   
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("rss:file:src/test/data/rss20.xml?splitEntries=true&sortEntries=true&consumer.delay=50").to("mock:sorted");
                
                // should NOT sort by default
                from("rss:file:src/test/data/rss20.xml?splitEntries=true&consumer.delay=50").to("mock:unsorted");
            }
        };
    }
}
