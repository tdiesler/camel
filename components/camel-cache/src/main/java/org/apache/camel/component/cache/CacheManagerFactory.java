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
package org.apache.camel.component.cache;

import net.sf.ehcache.CacheManager;
import org.apache.camel.support.ServiceSupport;

public abstract class CacheManagerFactory extends ServiceSupport {
    private CacheManager cacheManager;

    public synchronized CacheManager getInstance() {
        if (cacheManager == null) {
            cacheManager = createCacheManagerInstance();
        }
        
        return cacheManager;
    }

    /**
     * Creates {@link CacheManager}.
     * <p/>
     * The default implementation is {@link DefaultCacheManagerFactory}.
     *
     * @return {@link CacheManager}
     */
    protected abstract CacheManager createCacheManagerInstance();

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
        // shutdown cache manager when stopping
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
    }
}
