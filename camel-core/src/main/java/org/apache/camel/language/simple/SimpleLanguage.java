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

import org.apache.camel.Expression;
import org.apache.camel.ExpressionIllegalSyntaxException;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.util.ObjectHelper;

/**
 * A <a href="http://camel.apache.org/simple.html">simple language</a>
 * which maps simple property style notations to access headers and bodies.
 * Examples of supported expressions are:
 * <ul>
 * <li>id to access the inbound message id</li>
 * <li>in.body or body to access the inbound body</li>
 * <li>out.body to access the inbound body</li>
 * <li>in.header.foo or header.foo to access an inbound header called 'foo'</li>
 * <li>out.header.foo to access an outbound header called 'foo'</li>
 * <li>property.foo to access the exchange property called 'foo'</li>
 * <li>sys.foo to access the system property called 'foo'</li>
 * <li>exception.messsage to access the exception message</li>
 * <li>date:&lt;command&gt;:&lt;pattern&gt; for date formatting using the {@link java.text.SimpleDateFormat} patterns.
 *     Supported commands are: <tt>now</tt> for current timestamp,
 *     <tt>in.header.xxx</tt> or <tt>header.xxx</tt> to use the Date object in the in header.
 *     <tt>out.header.xxx</tt> to use the Date object in the out header.
 * </li>
 * <li>bean:&lt;bean expression&gt; to invoke a bean using the
 * {@link org.apache.camel.language.bean.BeanLanguage BeanLanguage}</li>
 * <li>properties:&lt;[locations]&gt;:&lt;key&gt; for using property placeholders using the
 *     {@link org.apache.camel.component.properties.PropertiesComponent}.
 *     The locations parameter is optional and you can enter multiple locations separated with comma.
 * </li>
* </ul>
 * <p/>
 * The simple language now also includes file language out of the box which means the following expression is also
 * supported:
 * <ul>
 *   <li><tt>file:name</tt> to access the file name (is relative, see note below))</li>
 *   <li><tt>file:name.noext</tt> to access the file name with no extension</li>
 *   <li><tt>file:ext</tt> to access the file extension</li>
 *   <li><tt>file:onlyname</tt> to access the file name (no paths)</li>
 *   <li><tt>file:onlyname.noext</tt> to access the file name (no paths) with no extension </li>
 *   <li><tt>file:parent</tt> to access the parent file name</li>
 *   <li><tt>file:path</tt> to access the file path name</li>
 *   <li><tt>file:absolute</tt> is the file regarded as absolute or relative</li>
 *   <li><tt>file:absolute.path</tt> to access the absolute file path name</li>
 *   <li><tt>file:length</tt> to access the file length as a Long type</li>
 *   <li><tt>file:modified</tt> to access the file last modified as a Date type</li>
 *   <li><tt>date:&lt;command&gt;:&lt;pattern&gt;</tt> for date formatting using the {@link java.text.SimpleDateFormat} patterns.
 *     Additional Supported commands are: <tt>file</tt> for the last modified timestamp of the file.
 *     All the commands from {@link SimpleLanguage} is also available.
 *   </li>
 * </ul>
 * The <b>relative</b> file is the filename with the starting directory clipped, as opposed to <b>path</b> that will
 * return the full path including the starting directory.
 * <br/>
 * The <b>only</b> file is the filename only with all paths clipped.
 *
 * @version $Revision$
 */
public class SimpleLanguage extends SimpleLanguageSupport {

    private static final SimpleLanguage SIMPLE = new SimpleLanguage();

    public static Expression simple(String expression) {
        return SIMPLE.createExpression(expression);
    }

    protected Expression createSimpleExpression(String expression, boolean strict) {
        if (ObjectHelper.isEqualToAny(expression, "body", "in.body")) {
            return ExpressionBuilder.bodyExpression();
        } else if (ObjectHelper.equal(expression, "out.body")) {
            return ExpressionBuilder.outBodyExpression();
        } else if (ObjectHelper.equal(expression, "id")) {
            return ExpressionBuilder.messageIdExpression();
        } else if (ObjectHelper.equal(expression, "exception.message")) {
            return ExpressionBuilder.exchangeExceptionMessageExpression();
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
            return ExpressionBuilder.systemPropertyExpression(remainder);
        }

        // file: prefix
        remainder = ifStartsWithReturnRemainder("file:", expression);
        if (remainder != null) {
            Expression fileExpression = createSimpleFileExpression(remainder);
            if (expression != null) {
                return fileExpression;
            }
        }

        // date: prefix
        remainder = ifStartsWithReturnRemainder("date:", expression);
        if (remainder != null) {
            String[] parts = remainder.split(":");
            if (parts.length < 2) {
                throw new ExpressionIllegalSyntaxException("Valid syntax: ${date:command:pattern} was: " + expression);
            }
            String command = ObjectHelper.before(remainder, ":");
            String pattern = ObjectHelper.after(remainder, ":");
            return ExpressionBuilder.dateExpression(command, pattern);
        }

        // bean: prefix
        remainder = ifStartsWithReturnRemainder("bean:", expression);
        if (remainder != null) {
            return ExpressionBuilder.beanExpression(remainder);
        }

        // properties: prefix
        remainder = ifStartsWithReturnRemainder("properties:", expression);
        if (remainder != null) {
            String[] parts = remainder.split(":");
            if (parts.length > 2) {
                throw new ExpressionIllegalSyntaxException("Valid syntax: ${properties:[locations]:key} was: " + expression);
            }

            String locations = null;
            String key = remainder;
            if (parts.length == 2) {
                locations = ObjectHelper.before(remainder, ":");
                key = ObjectHelper.after(remainder, ":");
            }
            return ExpressionBuilder.propertiesComponentExpression(key, locations);
        }

        if (strict) {
            throw new ExpressionIllegalSyntaxException(expression);
        } else {
            return ExpressionBuilder.constantExpression(expression);
        }
    }
    
    public Expression createSimpleFileExpression(String remainder) {
        if (ObjectHelper.equal(remainder, "name")) {
            return ExpressionBuilder.fileNameExpression();
        } else if (ObjectHelper.equal(remainder, "name.noext")) {
            return ExpressionBuilder.fileNameNoExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "onlyname")) {
            return ExpressionBuilder.fileOnlyNameExpression();
        } else if (ObjectHelper.equal(remainder, "onlyname.noext")) {
            return ExpressionBuilder.fileOnlyNameNoExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "ext")) {
            return ExpressionBuilder.fileExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "parent")) {
            return ExpressionBuilder.fileParentExpression();
        } else if (ObjectHelper.equal(remainder, "path")) {
            return ExpressionBuilder.filePathExpression();
        } else if (ObjectHelper.equal(remainder, "absolute")) {
            return ExpressionBuilder.fileAbsoluteExpression();
        } else if (ObjectHelper.equal(remainder, "absolute.path")) {
            return ExpressionBuilder.fileAbsolutePathExpression();
        } else if (ObjectHelper.equal(remainder, "length")) {
            return ExpressionBuilder.fileSizeExpression();
        } else if (ObjectHelper.equal(remainder, "modified")) {
            return ExpressionBuilder.fileLastModifiedExpression();
        }
        return null;
    }

    public boolean isSingleton() {
        return true;
    }
}
