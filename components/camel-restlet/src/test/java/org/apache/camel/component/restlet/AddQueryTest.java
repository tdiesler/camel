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
package org.apache.camel.component.restlet;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @version $Revision$
 */
public class AddQueryTest extends Assert {
    
    @Test
    public void testAddQuery() throws Exception {
        assertEquals("http://a/b/c?a=b", RestletProducer.addQueryToUri("http://a/b/c", "a=b"));
        assertEquals("http://a/b/c?a=b&c=b", RestletProducer.addQueryToUri("http://a/b/c", "a=b&c=b"));
        assertEquals("http://a/b/c?a=b&c=b&l=m", RestletProducer.addQueryToUri("http://a/b/c?c=b&l=m", "a=b"));
        assertEquals("http://a/b/c?a=b&i=j&c=b&l=m", RestletProducer.addQueryToUri("http://a/b/c?c=b&l=m", "a=b&i=j"));


    }

}
