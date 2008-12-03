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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.util.CollectionHelper;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * Implements a dynamic <a
 * href="http://activemq.apache.org/camel/splitter.html">Splitter</a> pattern
 * where an expression is evaluated to iterate through each of the parts of a
 * message and then each part is then send to some endpoint.
 *
 * @version $Revision$
 */
public class Splitter extends MulticastProcessor implements Processor {
    public static final String SPLIT_SIZE = "org.apache.camel.splitSize";
    public static final String SPLIT_COUNTER = "org.apache.camel.splitCounter";

    private final Expression expression;

    public Splitter(Expression expression, Processor destination, AggregationStrategy aggregationStrategy) {
        this(expression, destination, aggregationStrategy, false, null, false);
    }

    public Splitter(Expression expression, Processor destination, AggregationStrategy aggregationStrategy,
            boolean parallelProcessing, Executor executor, boolean streaming) {
        super(Collections.singleton(destination), aggregationStrategy, parallelProcessing, executor, streaming);

        this.expression = expression;
        notNull(expression, "expression");
        notNull(destination, "destination");
    }

    @Override
    public String toString() {
        return "Splitter[on: " + expression + " to: " + getProcessors().iterator().next() + " aggregate: " + getAggregationStrategy() + "]";
    }

    @Override
    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) {
        Object value = expression.evaluate(exchange);

        if (isStreaming()) {
            return createProcessorExchangePairsIterable(exchange, value);
        } else {
            return createProcessorExchangePairsList(exchange, value);
        }
    }

    private Iterable<ProcessorExchangePair> createProcessorExchangePairsIterable(final Exchange exchange, Object value) {
        final Iterator iterator = ObjectHelper.createIterator(value);
        return new Iterable() {

            public Iterator iterator() {
                return new Iterator() {

                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public Object next() {
                        Object part = iterator.next();
                        Exchange newExchange = exchange.copy();
                        Message in = newExchange.getIn();
                        in.setBody(part);
                        return new ProcessorExchangePair(getProcessors().iterator().next(), newExchange);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("remove is not supported by this iterator");
                    }
                };
            }

        };
    }

    private Iterable<ProcessorExchangePair> createProcessorExchangePairsList(Exchange exchange, Object value) {
        List<ProcessorExchangePair> result;
        Integer collectionSize = CollectionHelper.size(value);
        if (collectionSize != null) {
            result = new ArrayList<ProcessorExchangePair>(collectionSize);
        } else {
            result = new ArrayList<ProcessorExchangePair>();
        }
        Iterator<Object> iter = ObjectHelper.createIterator(value);
        while (iter.hasNext()) {
            Object part = iter.next();
            Exchange newExchange = exchange.copy();
            Message in = newExchange.getIn();
            in.setBody(part);
            result.add(new ProcessorExchangePair(getProcessors().iterator().next(), newExchange));
        }
        return result;
    }

    @Override
    protected void updateNewExchange(Exchange exchange, int i, Iterable<ProcessorExchangePair> allPairs) {
        exchange.getIn().setHeader(SPLIT_COUNTER, i);
        if (allPairs instanceof Collection) {
            exchange.getIn().setHeader(SPLIT_SIZE, ((Collection) allPairs).size());
        }
    }

    public Expression getExpression() {
        return expression;
    }
}
