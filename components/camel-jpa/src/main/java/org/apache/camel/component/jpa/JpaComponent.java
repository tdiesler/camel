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
package org.apache.camel.component.jpa;

import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A JPA Component
 *
 * @version $Revision$
 */
public class JpaComponent extends DefaultComponent {
    private EntityManagerFactory entityManagerFactory;
    private PlatformTransactionManager transactionManager;

    // Properties
    //-------------------------------------------------------------------------
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    @Override
    protected Endpoint createEndpoint(String uri, String path, Map<String, Object> options) throws Exception {
        JpaEndpoint endpoint = new JpaEndpoint(uri, this);

        // lets interpret the next string as a class
        if (path != null) {
            // provide the class loader of this component to work in OSGi environments as camel-jpa must be able
            // to resolve the entity classes
            Class<?> type = getCamelContext().getClassResolver().resolveClass(path, JpaComponent.class.getClassLoader());
            if (type != null) {
                endpoint.setEntityType(type);
            }
        }

        return endpoint;
    }
}
