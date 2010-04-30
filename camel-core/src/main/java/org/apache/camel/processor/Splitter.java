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
import java.util.concurrent.ExecutorService;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.util.CollectionHelper;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * Implements a dynamic <a
 * href="http://camel.apache.org/splitter.html">Splitter</a> pattern
 * where an expression is evaluated to iterate through each of the parts of a
 * message and then each part is then send to some endpoint.
 *
 * @version $Revision$
 */
public class Splitter extends MulticastProcessor implements Processor, Traceable {
    private final Expression expression;

    public Splitter(CamelContext camelContext, Expression expression, Processor destination, AggregationStrategy aggregationStrategy) {
        this(camelContext, expression, destination, aggregationStrategy, false, null, false, false);
    }

    public Splitter(CamelContext camelContext, Expression expression, Processor destination, AggregationStrategy aggregationStrategy,
                    boolean parallelProcessing, ExecutorService executorService, boolean streaming, boolean stopOnException) {
        super(camelContext, Collections.singleton(destination), aggregationStrategy, parallelProcessing, executorService, streaming, stopOnException);

        this.expression = expression;
        notNull(expression, "expression");
        notNull(destination, "destination");
    }

    @Override
    public String toString() {
        return "Splitter[on: " + expression + " to: " + getProcessors().iterator().next() + " aggregate: " + getAggregationStrategy() + "]";
    }

    @Override
    public String getTraceLabel() {
        return "split[" + expression + "]";
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        AggregationStrategy strategy = getAggregationStrategy();

        // if original aggregation strategy then store exchange
        // on it as the original exchange
        UseOriginalAggregationStrategy original = null;
        if (strategy instanceof UseOriginalAggregationStrategy) {
            original = (UseOriginalAggregationStrategy) strategy;
            original.setOriginal(exchange);
        }

        super.process(exchange);

        if (original != null) {
            // and remove the reference when we are done (due to thread local stuff)
            original.setOriginal(null);
        }
    }

    @Override
    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) {
        Object value = expression.evaluate(exchange, Object.class);

        if (isStreaming()) {
            return createProcessorExchangePairsIterable(exchange, value);
        } else {
            return createProcessorExchangePairsList(exchange, value);
        }
    }

    @SuppressWarnings("unchecked")
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
                        if (part instanceof Message) {
                            newExchange.setIn((Message)part);
                        } else {
                            Message in = newExchange.getIn();
                            in.setBody(part);
                        }
                        return createProcessorExchangePair(getProcessors().iterator().next(), newExchange);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("Remove is not supported by this iterator");
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
            if (part instanceof Message) {
                newExchange.setIn((Message)part);
            } else {
                Message in = newExchange.getIn();
                in.setBody(part);
            }
            result.add(createProcessorExchangePair(getProcessors().iterator().next(), newExchange));
        }
        return result;
    }

    @Override
    protected void updateNewExchange(Exchange exchange, int index, Iterable<ProcessorExchangePair> allPairs) {
        super.updateNewExchange(exchange, index, allPairs);
        exchange.setProperty(Exchange.SPLIT_INDEX, index);
        if (allPairs instanceof Collection) {
            exchange.setProperty(Exchange.SPLIT_SIZE, ((Collection<?>)allPairs).size());
        }
    }

    public Expression getExpression() {
        return expression;
    }
}
