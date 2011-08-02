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
package org.apache.camel.processor.idempotent;

import java.util.Map;

import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.management.ManagedAttribute;
import org.apache.camel.management.ManagedOperation;
import org.apache.camel.management.ManagedResource;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.LRUCache;

/**
 * A memory based implementation of {@link org.apache.camel.spi.IdempotentRepository}. 
 * <p/>
 * Care should be taken to use a suitable underlying {@link Map} to avoid this class being a
 * memory leak.
 *
 * @version 
 */
@ManagedResource(description = "Memory based idempotent repository")
public class MemoryIdempotentRepository extends ServiceSupport implements IdempotentRepository<String> {
    private Map<String, Object> cache;
    private int cacheSize;

    public MemoryIdempotentRepository() {
        this.cache = new LRUCache<String, Object>(1000);
    }

    public MemoryIdempotentRepository(Map<String, Object> set) {
        this.cache = set;
    }

    /**
     * Creates a new memory based repository using a {@link LRUCache}
     * with a default of 1000 entries in the cache.
     */
    public static IdempotentRepository<String> memoryIdempotentRepository() {
        return new MemoryIdempotentRepository();
    }

    /**
     * Creates a new memory based repository using a {@link LRUCache}.
     *
     * @param cacheSize  the cache size
     */
    public static IdempotentRepository<String> memoryIdempotentRepository(int cacheSize) {
        return memoryIdempotentRepository(new LRUCache<String, Object>(cacheSize));
    }

    /**
     * Creates a new memory based repository using the given {@link Map} to
     * use to store the processed message ids.
     * <p/>
     * Care should be taken to use a suitable underlying {@link Map} to avoid this class being a
     * memory leak.
     *
     * @param cache  the cache
     */
    public static IdempotentRepository<String> memoryIdempotentRepository(Map<String, Object> cache) {
        return new MemoryIdempotentRepository(cache);
    }

    @ManagedOperation(description = "Adds the key to the store")
    public boolean add(String key) {
        synchronized (cache) {
            if (cache.containsKey(key)) {
                return false;
            } else {
                cache.put(key, key);
                return true;
            }
        }
    }

    @ManagedOperation(description = "Does the store contain the given key")
    public boolean contains(String key) {
        synchronized (cache) {
            return cache.containsKey(key);
        }
    }

    @ManagedOperation(description = "Remove the key from the store")
    public boolean remove(String key) {
        synchronized (cache) {
            return cache.remove(key) != null;
        }
    }

    public boolean confirm(String key) {
        // noop
        return true;
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    @ManagedAttribute(description = "The current cache size")
    public int getCacheSize() {
        return cache.size();
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    protected void doStart() throws Exception {
        if (cacheSize > 0) {
            cache = new LRUCache<String, Object>(cacheSize);
        }
    }

    @Override
    protected void doStop() throws Exception {
        cache.clear();
    }
}
