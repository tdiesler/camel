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
package org.apache.camel.loanbroker.queue.version;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//START SNIPPET: aggregation
public class BankResponseAggregationStrategy implements AggregationStrategy {    
    private static final transient Logger LOG = LoggerFactory.getLogger(BankResponseAggregationStrategy.class);
    
    // Here we put the bank response together
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        LOG.debug("oldExchange: {}, newExchange: {}", oldExchange, newExchange);

        // the first time we only have the new exchange
        if (oldExchange == null) {
            return newExchange;
        }

        Message oldMessage;
        Message newMessage;
       
        oldMessage = oldExchange.getIn();
        newMessage = newExchange.getIn();

        Double oldRate = oldMessage.getHeader(Constants.PROPERTY_RATE, Double.class);
        Double newRate = newMessage.getHeader(Constants.PROPERTY_RATE, Double.class);

        Exchange result;
        if (newRate >= oldRate) {
            result = oldExchange;
        } else {
            result = newExchange;
        }

        LOG.debug("Lowest rate exchange: {}", result);
        return result;
    }

}
// END SNIPPET: aggregation
