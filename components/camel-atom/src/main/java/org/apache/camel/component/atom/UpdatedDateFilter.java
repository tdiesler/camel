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
package org.apache.camel.component.atom;

import java.util.Date;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filters out all entries which occur before the last time of the entry we saw (assuming
 * entries arrive sorted in order).
 *
 * @version $Revision: 1.1 $
 */
public class UpdatedDateFilter implements EntryFilter {
    private static final transient Log LOG = LogFactory.getLog(UpdatedDateFilter.class);
    private Date lastTime;

    public boolean isValidEntry(AtomEndpoint endpoint, Document<Feed> feed, Entry entry) {
        Date updated = getUpdated(endpoint, feed, entry);
        if (updated == null) {
            warnNoUpdatedTime(endpoint, feed, entry);
            return true;
        }
        if (lastTime != null) {
            if (lastTime.after(updated)) {
                return false;
            }
        }
        lastTime = updated;
        return true;
    }

    protected Date getUpdated(AtomEndpoint endpoint, Document<Feed> feed, Entry entry) {
        Date answer = entry.getUpdated();
        if (answer == null) {
            answer = entry.getEdited();

            // TODO is this valid?
            if (answer == null) {
                answer = entry.getPublished();
            }
        }
        return answer;
    }

    protected void warnNoUpdatedTime(AtomEndpoint endpoint, Document<Feed> feed, Entry entry) {
        LOG.warn("No updated time for entry so assuming new: " + entry);
    }
}
