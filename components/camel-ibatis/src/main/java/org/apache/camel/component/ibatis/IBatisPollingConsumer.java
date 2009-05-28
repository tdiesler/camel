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
package org.apache.camel.component.ibatis;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.BatchConsumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <pre>
 *  Ibatis Camel Component used to read data from a database.
 * 
 *  Example Configuration :
 *  &lt;route&gt;
 *   &lt;from uri=&quot;ibatis:selectRecords&quot; /&gt;
 *   &lt;to uri=&quot;jms:destinationQueue&quot; /&gt;
 *  &lt;/route&gt;
 * 
 * 
 *  This also can be configured to treat a table as a logical queue by defining
 *  an &quot;onConsume&quot; statement.
 * 
 *  Example Configuration :
 *  &lt;route&gt;
 *   &lt;from uri=&quot;ibatis:selectRecords?consumer.onConsume=updateRecord&quot; /&gt;
 *   &lt;to uri=&quot;jms:destinationQueue&quot; /&gt;
 *  &lt;/route&gt;
 * 
 *  By default, if the select statement contains multiple rows, it will
 *  iterate over the set and deliver each row to the route.  If this is not the
 *  desired behavior then set &quot;useIterator=false&quot;.  This will deliver the entire
 *  set to the route as a list.
 * </pre>
 *
 * <b>URI Options</b>
 * <table border="1">
 * <thead>
 * <th>Name</th>
 * <th>Default Value</th>
 * <th>description</th>
 * </thead>
 * <tbody>
 * <tr>
 * <td>initialDelay</td>
 * <td>1000 ms</td>
 * <td>time before polling starts</td>
 * </tr>
 * <tr>
 * <td>delay</td>
 * <td>500 ms</td>
 * <td>time before the next poll</td>
 * </tr>
 * <tr>
 * <td>timeUnit</td>
 * <td>MILLISECONDS</td>
 * <td>Time unit to use for delay properties (NANOSECONDS, MICROSECONDS,
 * MILLISECONDS, SECONDS)</td>
 * </tr>
 * <tr>
 * <td>useIterator</td>
 * <td>true</td>
 * <td>If true, processes one exchange per row. If false processes one exchange
 * for all rows</td>
 * </tr>
 * <tr>
 * <td>onConsume</td>
 * <td>null</td>
 * <td>statement to run after data has been processed</td>
 * </tr>
 * <tbody> </table>
 *
 * @see org.apache.camel.component.ibatis.strategy.IBatisProcessingStrategy
 */
public class IBatisPollingConsumer extends ScheduledPollConsumer implements BatchConsumer {

    private static final Log LOG = LogFactory.getLog(IBatisPollingConsumer.class);

    private final class DataHolder {
        private Exchange exchange;
        private Object data;
        private DataHolder() {
        }
    }

    /**
     * Statement to run after data has been processed in the route
     */
    private String onConsume;

    /**
     * Process resultset individually or as a list
     */
    private boolean useIterator = true;

    /**
     * Whether allow empty resultset to be routed to the next hop
     */
    private boolean routeEmptyResultSet;

    public IBatisPollingConsumer(IBatisEndpoint endpoint, Processor processor) throws Exception {
        super(endpoint, processor);
    }

    public IBatisEndpoint getEndpoint() {
        return (IBatisEndpoint) super.getEndpoint();
    }

    /**
     * Polls the database
     */
    @Override
    protected void poll() throws Exception {

        // poll data from the database
        IBatisEndpoint endpoint = getEndpoint();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Polling: " + endpoint);
        }
        List data = endpoint.getProcessingStrategy().poll(this, getEndpoint());

        // create a list of exchange objects with the data
        List<DataHolder> answer = new ArrayList<DataHolder>();
        if (useIterator) {
            for (Object item : data) {
                Exchange exchange = createExchange(item);
                DataHolder holder = new DataHolder();
                holder.exchange = exchange;
                holder.data = item;
                answer.add(holder);
            }
        } else {
            if (!data.isEmpty() || routeEmptyResultSet) {
                Exchange exchange = createExchange(data);
                DataHolder holder = new DataHolder();
                holder.exchange = exchange;
                holder.data = data;
                answer.add(holder);
            }
        }

        // process all the exchanges in this batch
        processBatch(answer);
    }

    public void processBatch(List exchanges) throws Exception {
        final IBatisEndpoint endpoint = getEndpoint();
        final List<DataHolder> list = exchanges;

        int total = list.size();
        for (int index = 0; index < total && isRunAllowed(); index++) {
            // only loop if we are started (allowed to run)
            Exchange exchange = list.get(index).exchange;
            Object data = list.get(index).data;

            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // process the current exchange
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing exchange: " + exchange);
            }
            getProcessor().process(exchange);

            try {
                if (onConsume != null) {
                    endpoint.getProcessingStrategy().commit(endpoint, exchange, data, onConsume);
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private Exchange createExchange(Object data) {
        final IBatisEndpoint endpoint = getEndpoint();
        final Exchange exchange = endpoint.createExchange(ExchangePattern.InOnly);

        Message msg = exchange.getIn();
        msg.setBody(data);
        msg.setHeader(IBatisConstants.IBATIS_STATEMENT_NAME, endpoint.getStatement());

        return exchange;
    }

    /**
     * Gets the statement(s) to run after successful processing.
     * Use comma to separate multiple statements.
     */
    public String getOnConsume() {
        return onConsume;
    }

    /**
     * Sets the statement to run after successful processing.
     * Use comma to separate multiple statements.
     */
    public void setOnConsume(String onConsume) {
        this.onConsume = onConsume;
    }

    /**
     * Indicates how resultset should be delivered to the route
     */
    public boolean isUseIterator() {
        return useIterator;
    }

    /**
     * Sets how resultset should be delivered to route.
     * Indicates delivery as either a list or individual object.
     * defaults to true.
     */
    public void setUseIterator(boolean useIterator) {
        this.useIterator = useIterator;
    }

    /**
     * Indicates whether empty resultset should be allowed to be sent to the next hop or not
     */
    public boolean isRouteEmptyResultSet() {
        return routeEmptyResultSet;
    }

    /**
     * Sets whether empty resultset should be allowed to be sent to the next hop.
     * defaults to false. So the empty resultset will be filtered out.
     */
    public void setRouteEmptyResultSet(boolean routeEmptyResultSet) {
        this.routeEmptyResultSet = routeEmptyResultSet;
    }
}
