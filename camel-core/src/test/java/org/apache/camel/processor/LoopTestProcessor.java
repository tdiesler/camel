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

import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class LoopTestProcessor implements Processor {
    private int count = 0;
    private int index = 0;

    public LoopTestProcessor() {
    }

    public LoopTestProcessor(int count) {
        setCount(count);
    }

    public void setCount(int count) {
        this.count = count;
        reset();
    }

    public void reset() {
        this.index = 0;
    }

    public void process(Exchange exchange) {
    	Integer c = exchange.getProperty(LoopProcessor.PROP_ITER_COUNT, Integer.class);
    	Integer i = exchange.getProperty(LoopProcessor.PROP_ITER_INDEX, Integer.class);
    	if (c == null || c.intValue() != this.count) {
    	    exchange.setException(new CamelException(
                "Invalid count value.  Expected " + this.count + " but was " + c));
        }
        if (i == null || i.intValue() != this.index++) {
            exchange.setException(new CamelException(
                "Invalid index value.  Expected " + this.index + " but was " + i));
        }
    }
}
