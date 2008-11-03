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
package org.apache.camel.spi;

import org.apache.camel.Expression;
import org.apache.camel.Predicate;

/**
 * Represents a language to be used for {@link Expression} or {@link Predicate} instances
 *
 * @version $Revision$
 */
public interface Language {

    /**
     * Creates a predicate based on the given string input
     *
     * @param expression  the expression
     * @return the created predicate
     */
    Predicate createPredicate(String expression);

    /**
     * Creates an expression based on the given string input
     *
     * @param expression  the expression as a string input
     * @return the created expression
     */
    Expression createExpression(String expression);
}
