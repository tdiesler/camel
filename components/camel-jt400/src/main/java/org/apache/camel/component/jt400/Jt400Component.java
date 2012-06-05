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
package org.apache.camel.component.jt400;

import java.util.Map;

import com.ibm.as400.access.AS400ConnectionPool;
import org.apache.camel.CamelException;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.EndpointHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.apache.camel.Component} to provide integration with AS/400 objects.
 * 
 * Current implementation supports working with data queues (*DTAQ) and Program calls (*PGM)
 */
public class Jt400Component extends DefaultComponent {
    
    /**
     * Name of the connection pool URI option.
     */
    static final String CONNECTION_POOL = "connectionPool";

    /**
     * Logging tool used by this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Jt400Component.class);

    private static final String DATA_QUEUE = "DTAQ";
    private static final String PGM = "PGM";

    /**
     * Default connection pool used by the component. Note that this pool is
     * lazily initialized. This is because in a scenario where the user always
     * provides a pool, it would be wasteful for Camel to initialize and keep an
     * idle pool.
     */
    private AS400ConnectionPool connectionPool;

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> properties) throws Exception {
        String type = remaining.substring(remaining.lastIndexOf(".") + 1).toUpperCase();
        AS400ConnectionPool connectionPool;
        if (properties.containsKey(CONNECTION_POOL)) {
            LOG.trace("AS400ConnectionPool instance specified in the URI - will look it up.");
            
            // We have chosen to handle the connectionPool option ourselves, so
            // we must remove it from the given parameter list (see
            // http://camel.apache.org/writing-components.html)
            String poolId = properties.remove(CONNECTION_POOL).toString();
            connectionPool = EndpointHelper.resolveReferenceParameter(getCamelContext(), poolId, AS400ConnectionPool.class, true);
        } else {
            LOG.trace("No AS400ConnectionPool instance specified in the URI - one will be provided.");
            connectionPool = getConnectionPool();
        }

        if (DATA_QUEUE.equals(type)) {
            return new Jt400DataQueueEndpoint(uri, this, connectionPool);
        }
        
        if (PGM.equals(type)) {
            return new Jt400PgmEndpoint(uri, this, connectionPool);
        }
        
        throw new CamelException(String.format("AS/400 Object type %s is not supported", type));
    }

    /**
     * Returns the default connection pool used by this component.
     * 
     * @return the default connection pool used by this component
     */
    public synchronized AS400ConnectionPool getConnectionPool() {
        if (connectionPool == null) {
            LOG.info("Instantiating the default connection pool ...");
            connectionPool = new AS400ConnectionPool();
        }
        return connectionPool;
    }

    public void setConnectionPool(AS400ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    protected void doShutdown() throws Exception {
        super.doShutdown();
        if (connectionPool != null) {
            LOG.info("Shutting down the default connection pool " + connectionPool + " ...");
            connectionPool.close();
            connectionPool = null;
        }
    }
    
}
