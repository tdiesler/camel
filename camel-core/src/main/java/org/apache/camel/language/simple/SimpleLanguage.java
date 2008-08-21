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
package org.apache.camel.language.simple;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.language.IllegalSyntaxException;
import org.apache.camel.util.ObjectHelper;

/**
 * A <a href="http://activemq.apache.org/camel/simple.html">simple language</a>
 * which maps simple property style notations to access headers and bodies.
 * Examples of supported expressions are:
 * <ul>
 * <li>id to access the inbound message Id</li>
 * <li>in.header.foo or header.foo to access an inbound header called 'foo'</li>
 * <li>in.body or body to access the inbound body</li>
 * <li>out.header.foo to access an outbound header called 'foo'</li>
 * <li>out.body to access the inbound body</li>
 * <li>property.foo to access the exchange property called 'foo'</li>
 * <li>sys.foo to access the system property called 'foo'</li>
 * </ul>
 *
 * @version $Revision$
 */
public class SimpleLanguage extends AbstractSimpleLanguage {

    public static Expression simple(String expression) {
        SimpleLanguage language = new SimpleLanguage();
        return language.createExpression(expression);
    }

    protected Expression<Exchange> createSimpleExpression(String expression) {
        if (ObjectHelper.isEqualToAny(expression, "body", "in.body")) {
            return ExpressionBuilder.bodyExpression();
        } else if (ObjectHelper.equal(expression, "out.body")) {
            return ExpressionBuilder.outBodyExpression();
        } else if (ObjectHelper.equal(expression, "id")) {
            return ExpressionBuilder.messageIdExpression();
        }

        // in header expression
        String remainder = ifStartsWithReturnRemainder("in.header.", expression);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("header.", expression);
        }
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("headers.", expression);
        }
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("in.headers.", expression);
        }
        if (remainder != null) {
            return ExpressionBuilder.headerExpression(remainder);
        }

        // out header expression
        remainder = ifStartsWithReturnRemainder("out.header.", expression);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("out.headers.", expression);
        }
        if (remainder != null) {
            return ExpressionBuilder.outHeaderExpression(remainder);
        }

        // property
        remainder = ifStartsWithReturnRemainder("property.", expression);
        if (remainder != null) {
            return ExpressionBuilder.propertyExpression(remainder);
        }

        // system property
        remainder = ifStartsWithReturnRemainder("sys.", expression);
        if (remainder != null) {
            return ExpressionBuilder.propertyExpression(remainder);
        }

        throw new IllegalSyntaxException(this, expression);
    }

}
