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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.CastUtils;

/**
 * A processor that sorts the expression using a comparator
 */
public class SortProcessor implements Processor {

    private final Expression expression;
    private final Comparator<Object> comparator;

    public SortProcessor(Expression expression, Comparator<Object> comparator) {
        this.expression = expression;
        this.comparator = comparator;
    }

    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        List<Object> list = CastUtils.cast(expression.evaluate(exchange, List.class));
        Collections.sort(list, comparator);

        if (exchange.getPattern().isOutCapable()) {
            Message out = exchange.getOut();
            out.copyFrom(in);
            out.setBody(list);
        } else {
            in.setBody(list);
        }
    }

    public String toString() {
        return "Sort[" + expression + "]";
    }
}


