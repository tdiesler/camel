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
package org.apache.camel.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.impl.ProducerCache;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * Implements a dynamic <a
 * href="http://camel.apache.org/recipient-list.html">Recipient List</a>
 * pattern where the list of actual endpoints to send a message exchange to are
 * dependent on some dynamic expression.
 *
 * @version $Revision$
 */
public class RecipientList extends ServiceSupport implements AsyncProcessor {
    private static final transient Log LOG = LogFactory.getLog(RecipientList.class);
    private final CamelContext camelContext;
    private ProducerCache producerCache;
    private Expression expression;
    private final String delimiter;
    private boolean parallelProcessing;
    private boolean stopOnException;
    private boolean ignoreInvalidEndpoints;
    private ExecutorService executorService;
    private AggregationStrategy aggregationStrategy = new UseLatestAggregationStrategy();

    public RecipientList(CamelContext camelContext) {
        // use comma by default as delimiter
        this(camelContext, ",");
    }

    public RecipientList(CamelContext camelContext, String delimiter) {
        notNull(camelContext, "camelContext");
        ObjectHelper.notEmpty(delimiter, "delimiter");
        this.camelContext = camelContext;
        this.delimiter = delimiter;
    }

    public RecipientList(CamelContext camelContext, Expression expression) {
        // use comma by default as delimiter
        this(camelContext, expression, ",");
    }

    public RecipientList(CamelContext camelContext, Expression expression, String delimiter) {
        notNull(camelContext, "camelContext");
        ObjectHelper.notNull(expression, "expression");
        ObjectHelper.notEmpty(delimiter, "delimiter");
        this.camelContext = camelContext;
        this.expression = expression;
        this.delimiter = delimiter;
    }

    @Override
    public String toString() {
        return "RecipientList[" + (expression != null ? expression : "") + "]";
    }

    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        if (!isStarted()) {
            throw new IllegalStateException("RecipientList has not been started: " + this);
        }

        Object recipientList = expression.evaluate(exchange, Object.class);
        return sendToRecipientList(exchange, recipientList, callback);
    }

    public boolean sendToRecipientList(Exchange exchange, Object routingSlip) {
        // this method is invoked from @RecipientList so we bridge with an empty callback
        // TODO: Have @RecipientList support async out of the box
        return sendToRecipientList(exchange, routingSlip, new AsyncCallback() {
            public void done(boolean doneSync) {
                // noop
            }
        });
    }

    /**
     * Sends the given exchange to the recipient list
     */
    public boolean sendToRecipientList(Exchange exchange, Object recipientList, AsyncCallback callback) {
        Iterator<Object> iter = ObjectHelper.createIterator(recipientList, delimiter);

        RecipientListProcessor rlp = new RecipientListProcessor(exchange.getContext(), producerCache, iter, getAggregationStrategy(),
                                                                isParallelProcessing(), getExecutorService(), false, isStopOnException());
        rlp.setIgnoreInvalidEndpoints(isIgnoreInvalidEndpoints());

        // now let the multicast process the exchange
        return rlp.process(exchange, callback);
    }

    protected Endpoint resolveEndpoint(Exchange exchange, Object recipient) {
        // trim strings as end users might have added spaces between separators
        if (recipient instanceof String) {
            recipient = ((String)recipient).trim();
        }
        return ExchangeHelper.resolveEndpoint(exchange, recipient);
    }

    protected void doStart() throws Exception {
        if (producerCache == null) {
            producerCache = new ProducerCache(this, camelContext);
            // add it as a service so we can manage it
            camelContext.addService(producerCache);
        }
        ServiceHelper.startService(producerCache);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopService(producerCache);
    }

    public boolean isIgnoreInvalidEndpoints() {
        return ignoreInvalidEndpoints;
    }
    
    public void setIgnoreInvalidEndpoints(boolean ignoreInvalidEndpoints) {
        this.ignoreInvalidEndpoints = ignoreInvalidEndpoints;
    }

    public boolean isParallelProcessing() {
        return parallelProcessing;
    }

    public void setParallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;
    }

    public boolean isStopOnException() {
        return stopOnException;
    }

    public void setStopOnException(boolean stopOnException) {
        this.stopOnException = stopOnException;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public AggregationStrategy getAggregationStrategy() {
        return aggregationStrategy;
    }

    public void setAggregationStrategy(AggregationStrategy aggregationStrategy) {
        this.aggregationStrategy = aggregationStrategy;
    }
}
