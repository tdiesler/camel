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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.camel.BatchConsumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.jpa.JpaCallback;

/**
 * @version $Revision$
 */
public class JpaConsumer extends ScheduledPollConsumer implements BatchConsumer {

    private static final transient Log LOG = LogFactory.getLog(JpaConsumer.class);
    private final JpaEndpoint endpoint;
    private final TransactionStrategy template;
    private QueryFactory queryFactory;
    private DeleteHandler deleteHandler;
    private String query;
    private String namedQuery;
    private String nativeQuery;
    private int maxMessagesPerPoll;

    private final class DataHolder {
        private Exchange exchange;
        private Object result;
        private EntityManager manager;
        private DataHolder() {
        }
    }

    public JpaConsumer(JpaEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.template = endpoint.createTransactionStrategy();
    }

    protected void poll() throws Exception {
        template.execute(new JpaCallback() {
            public Object doInJpa(EntityManager entityManager) throws PersistenceException {
                Queue<DataHolder> answer = new LinkedList<DataHolder>();

                Query query = getQueryFactory().createQuery(entityManager);
                configureParameters(query);
                List results = query.getResultList();
                for (Object result : results) {
                    DataHolder holder = new DataHolder();
                    holder.manager = entityManager;
                    holder.result = result;
                    holder.exchange = createExchange(result);
                    answer.add(holder);
                }

                try {
                    processBatch(answer);
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }

                entityManager.flush();
                return null;
            }
        });
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public void processBatch(Queue exchanges) throws Exception {
        if (exchanges.isEmpty()) {
            return;
        }

        int total = exchanges.size();

        // limit if needed
        if (maxMessagesPerPoll > 0 && total > maxMessagesPerPoll) {
            LOG.debug("Limiting to maximum messages to poll " + maxMessagesPerPoll + " as there was " + total + " messages in this poll.");
            total = maxMessagesPerPoll;
        }

        for (int index = 0; index < total && isRunAllowed(); index++) {
            // only loop if we are started (allowed to run)
            DataHolder holder = (DataHolder) exchanges.poll();
            EntityManager entityManager = holder.manager;
            Exchange exchange = holder.exchange;
            Object result = holder.result;

            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            if (lockEntity(result, entityManager)) {

                // process the current exchange
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Processing exchange: " + exchange);
                }
                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }

                getDeleteHandler().deleteObject(entityManager, result);
            }
        }
    }


    // Properties
    // -------------------------------------------------------------------------
    public JpaEndpoint getEndpoint() {
        return endpoint;
    }

    public QueryFactory getQueryFactory() {
        if (queryFactory == null) {
            queryFactory = createQueryFactory();
            if (queryFactory == null) {
                throw new IllegalArgumentException("No queryType property configured on this consumer, nor an entityType configured on the endpoint so cannot consume");
            }
        }
        return queryFactory;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public DeleteHandler getDeleteHandler() {
        if (deleteHandler == null) {
            deleteHandler = createDeleteHandler();
        }
        return deleteHandler;
    }

    public void setDeleteHandler(DeleteHandler deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public String getNamedQuery() {
        return namedQuery;
    }

    public void setNamedQuery(String namedQuery) {
        this.namedQuery = namedQuery;
    }

    public String getNativeQuery() {
        return nativeQuery;
    }

    public void setNativeQuery(String nativeQuery) {
        this.nativeQuery = nativeQuery;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * A strategy method to lock an object with an exclusive lock so that it can
     * be processed
     * 
     * @param entity the entity to be locked
     * @param entityManager entity manager
     * @return true if the entity was locked
     */
    protected boolean lockEntity(Object entity, EntityManager entityManager) {
        if (!getEndpoint().isConsumeDelete() || !getEndpoint().isConsumeLockEntity()) {
            return true;
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Acquiring exclusive lock on entity: " + entity);
            }
            entityManager.lock(entity, LockModeType.WRITE);
            return true;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to achieve lock on entity: " + entity + ". Reason: " + e, e);
            }
            return false;
        }
    }

    protected QueryFactory createQueryFactory() {
        if (query != null) {
            return QueryBuilder.query(query);
        } else if (namedQuery != null) {
            return QueryBuilder.namedQuery(namedQuery);
        } else if (nativeQuery != null) {
            return QueryBuilder.nativeQuery(nativeQuery);
        } else {
            Class<?> entityType = endpoint.getEntityType();
            if (entityType == null) {
                return null;
            } else {
                return QueryBuilder.query("select x from " + entityType.getName() + " x");
            }
        }
    }

    protected DeleteHandler<Object> createDeleteHandler() {
        // look for @Consumed to allow custom callback when the Entity has been consumed
        Class<?> entityType = getEndpoint().getEntityType();
        if (entityType != null) {
            List<Method> methods = ObjectHelper.findMethodsWithAnnotation(entityType, Consumed.class);
            if (methods.size() > 1) {
                throw new IllegalArgumentException("Only one method can be annotated with the @Consumed annotation but found: " + methods);
            } else if (methods.size() == 1) {
                final Method method = methods.get(0);

                return new DeleteHandler<Object>() {
                    public void deleteObject(EntityManager entityManager, Object entityBean) {
                        ObjectHelper.invokeMethod(method, entityBean);
                    }
                };
            }
        }
        if (getEndpoint().isConsumeDelete()) {
            return new DeleteHandler<Object>() {
                public void deleteObject(EntityManager entityManager, Object entityBean) {
                    entityManager.remove(entityBean);
                }
            };
        } else {
            return new DeleteHandler<Object>() {
                public void deleteObject(EntityManager entityManager, Object entityBean) {
                    // do nothing
                }
            };
        }
    }

    protected void configureParameters(Query query) {
        int maxResults = endpoint.getMaximumResults();
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
    }

    protected Exchange createExchange(Object result) {
        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setBody(result);
        exchange.getIn().setHeader(JpaConstants.JPA_TEMPLATE, endpoint.getTemplate());
        return exchange;
    }
}
