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
package org.apache.camel.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class CaseInsensitiveMapTest extends TestCase {

    public void testLookupCaseAgnostic() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");

        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));
        assertEquals("cheese", map.get("FOO"));
    }

    public void testLookupCaseAgnosticAddHeader() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");

        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));
        assertEquals("cheese", map.get("FOO"));
        assertNull(map.get("unknown"));

        map.put("bar", "beer");

        assertEquals("beer", map.get("bar"));
        assertEquals("beer", map.get("Bar"));
        assertEquals("beer", map.get("BAR"));
        assertNull(map.get("unknown"));
    }

    public void testLookupCaseAgnosticAddHeader2() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");

        assertEquals("cheese", map.get("FOO"));
        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));
        assertNull(map.get("unknown"));

        map.put("bar", "beer");

        assertEquals("beer", map.get("BAR"));
        assertEquals("beer", map.get("bar"));
        assertEquals("beer", map.get("Bar"));
        assertNull(map.get("unknown"));
    }

    public void testLookupCaseAgnosticAddHeaderRemoveHeader() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");

        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));
        assertEquals("cheese", map.get("FOO"));
        assertNull(map.get("unknown"));

        map.put("bar", "beer");

        assertEquals("beer", map.get("bar"));
        assertEquals("beer", map.get("Bar"));
        assertEquals("beer", map.get("BAR"));
        assertNull(map.get("unknown"));

        map.remove("bar");
        assertNull(map.get("bar"));
        assertNull(map.get("unknown"));
    }

    public void testSetWithDifferentCase() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");
        map.put("Foo", "bar");

        assertEquals("bar", map.get("FOO"));
        assertEquals("bar", map.get("foo"));
        assertEquals("bar", map.get("Foo"));
    }

    public void testRemoveWithDifferentCase() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        map.put("foo", "cheese");
        map.put("Foo", "bar");

        assertEquals("bar", map.get("FOO"));
        assertEquals("bar", map.get("foo"));
        assertEquals("bar", map.get("Foo"));

        map.remove("FOO");

        assertEquals(null, map.get("foo"));
        assertEquals(null, map.get("Foo"));
        assertEquals(null, map.get("FOO"));

        assertTrue(map.isEmpty());
    }

    public void testPutAll() {
        Map<String, Object> map = new CaseInsensitiveMap();
        assertNull(map.get("foo"));

        Map<String, Object> other = new HashMap<String, Object>();
        other.put("Foo", "cheese");
        other.put("bar", 123);

        map.putAll(other);

        assertEquals("cheese", map.get("FOO"));
        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));

        assertEquals(123, map.get("BAR"));
        assertEquals(123, map.get("bar"));
        assertEquals(123, map.get("BaR"));
    }

    public void testConstructFromOther() {
        Map<String, Object> other = new HashMap<String, Object>();
        other.put("Foo", "cheese");
        other.put("bar", 123);

        Map<String, Object> map = new CaseInsensitiveMap(other);

        assertEquals("cheese", map.get("FOO"));
        assertEquals("cheese", map.get("foo"));
        assertEquals("cheese", map.get("Foo"));

        assertEquals(123, map.get("BAR"));
        assertEquals(123, map.get("bar"));
        assertEquals(123, map.get("BaR"));
    }

    public void testKeySet() {
        Map<String, Object> map = new CaseInsensitiveMap();
        map.put("Foo", "cheese");
        map.put("BAR", 123);
        map.put("baZ", "beer");

        Set keys = map.keySet();

        // we should be able to lookup no matter what case
        assertTrue(keys.contains("Foo"));
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("FOO"));

        assertTrue(keys.contains("BAR"));
        assertTrue(keys.contains("bar"));
        assertTrue(keys.contains("Bar"));

        assertTrue(keys.contains("baZ"));
        assertTrue(keys.contains("baz"));
        assertTrue(keys.contains("Baz"));
        assertTrue(keys.contains("BAZ"));
    }

    public void testRetainKeysCopyToAnotherMap() {
        Map<String, Object> map = new CaseInsensitiveMap();
        map.put("Foo", "cheese");
        map.put("BAR", 123);
        map.put("baZ", "beer");

        Map<String, Object> other = new HashMap<String, Object>(map);

        // we should retain the cases of the original keys
        // when its copied to another map
        assertTrue(other.containsKey("Foo"));
        assertFalse(other.containsKey("foo"));
        assertFalse(other.containsKey("FOO"));

        assertTrue(other.containsKey("BAR"));
        assertFalse(other.containsKey("bar"));
        assertFalse(other.containsKey("Bar"));

        assertTrue(other.containsKey("baZ"));
        assertFalse(other.containsKey("baz"));
        assertFalse(other.containsKey("Baz"));
        assertFalse(other.containsKey("BAZ"));
    }

    public void testValues() {
        Map<String, Object> map = new CaseInsensitiveMap();
        map.put("Foo", "cheese");
        map.put("BAR", "123");
        map.put("baZ", "Beer");

        Iterator it = map.values().iterator();

        // should be String values
        assertEquals("String", it.next().getClass().getSimpleName());
        assertEquals("String", it.next().getClass().getSimpleName());
        assertEquals("String", it.next().getClass().getSimpleName());

        Collection values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("cheese"));
        assertTrue(values.contains("123"));
        assertTrue(values.contains("Beer"));
    }

}