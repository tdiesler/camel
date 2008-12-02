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

import com.sun.syndication.feed.synd.SyndFeed;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Consumer to poll RSS feeds and return the full feed.
 */
public class RssPollingConsumer extends RssConsumerSupport {

    public RssPollingConsumer(RssEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    protected void poll() throws Exception {
        SyndFeed feed = createFeed();
        Exchange exchange = endpoint.createExchange(feed);
        getProcessor().process(exchange);        
    }

}
