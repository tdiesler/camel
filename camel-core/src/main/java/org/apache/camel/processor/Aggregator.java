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

import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.processor.aggregate.AggregationCollection;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.DefaultAggregationCollection;
import org.apache.camel.processor.aggregate.PredicateAggregationCollection;

/**
 * An implementation of the <a
 * href="http://camel.apache.org/aggregator.html">Aggregator</a>
 * pattern where a batch of messages are processed (up to a maximum amount or
 * until some timeout is reached) and messages for the same correlation key are
 * combined together using some kind of {@link AggregationStrategy}
 * (by default the latest message is used) to compress many message exchanges
 * into a smaller number of exchanges.
 * <p/>
 * A good example of this is stock market data; you may be receiving 30,000
 * messages/second and you may want to throttle it right down so that multiple
 * messages for the same stock are combined (or just the latest message is used
 * and older prices are discarded). Another idea is to combine line item messages
 * together into a single invoice message.
 *
 * @version $Revision$
 */
public class Aggregator extends BatchProcessor implements Traceable {

    private Expression correlationExpression;

    public Aggregator(Processor processor, Expression correlationExpression, AggregationStrategy aggregationStrategy) {
        this(processor, new DefaultAggregationCollection(correlationExpression, aggregationStrategy));
        this.correlationExpression = correlationExpression;
    }

    public Aggregator(Processor processor, Expression correlationExpression, AggregationStrategy aggregationStrategy,
                      Predicate aggregationCompletedPredicate) {
        this(processor, new PredicateAggregationCollection(correlationExpression, aggregationStrategy, aggregationCompletedPredicate));
        this.correlationExpression = correlationExpression;
    }

    public Aggregator(Processor processor, AggregationCollection collection) {
        super(processor, collection);
    }

    @Override
    public String toString() {
        return "Aggregator[to: " + getProcessor() + "]";
    }

    public String getTraceLabel() {
        return "aggregate[" + correlationExpression + "]";
    }
}
