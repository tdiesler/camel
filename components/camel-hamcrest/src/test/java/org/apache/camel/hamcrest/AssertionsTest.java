/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.hamcrest;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.1 $
 */
public class AssertionsTest extends TestCase {
    private static final transient Log LOG = LogFactory.getLog(AssertionsTest.class);

    public void testInstanceOf() throws Exception {
        String value = Assertions.assertInstanceOf("abc", String.class);
        assertEquals("value", "abc", value);
    }

    public void testInstanceOfFails() throws Exception {
        try {
            String value = Assertions.assertInstanceOf(123, String.class);
            fail("Should not return a value: " + value);
        }
        catch (AssertionError e) {
            LOG.info("Caught expected exception: " + e, e);
        }
    }
}
