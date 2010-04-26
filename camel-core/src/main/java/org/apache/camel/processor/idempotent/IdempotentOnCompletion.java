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

import org.apache.camel.Exchange;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * On completion strategy for {@link org.apache.camel.processor.idempotent.IdempotentConsumer}.
 * <p/>
 * This strategy adds the message id to the idempotent repository in cast the exchange
 * was processed successfully. In case of failure the message id is <b>not</b> added.
 *
 * @version $Revision$
 */
public class IdempotentOnCompletion implements Synchronization {
    private static final transient Log LOG = LogFactory.getLog(IdempotentOnCompletion.class);
    private final IdempotentRepository<String> idempotentRepository;
    private final String messageId;
    private final boolean eager;

    public IdempotentOnCompletion(IdempotentRepository<String> idempotentRepository, String messageId, boolean eager) {
        this.idempotentRepository = idempotentRepository;
        this.messageId = messageId;
        this.eager = eager;
    }

    public void onComplete(Exchange exchange) {
        if (ExchangeHelper.isFailureHandled(exchange)) {
            // the exchange did not process successfully but was failure handled by the dead letter channel
            // and thus moved to the dead letter queue. We should thus not consider it as complete.
            onFailedMessage(exchange, messageId);
        } else {
            onCompletedMessage(exchange, messageId);
        }
    }

    public void onFailure(Exchange exchange) {
        onFailedMessage(exchange, messageId);
    }

    /**
     * A strategy method to allow derived classes to overload the behavior of
     * processing a completed message
     *
     * @param exchange the exchange
     * @param messageId the message ID of this exchange
     */
    protected void onCompletedMessage(Exchange exchange, String messageId) {
        if (!eager) {
            // if not eager we should add the key when its complete
            idempotentRepository.add(messageId);
        }
        idempotentRepository.confirm(messageId);
    }

    /**
     * A strategy method to allow derived classes to overload the behavior of
     * processing a failed message
     *
     * @param exchange the exchange
     * @param messageId the message ID of this exchange
     */
    protected void onFailedMessage(Exchange exchange, String messageId) {
        idempotentRepository.remove(messageId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removed from repository as exchange failed: " + exchange + " with id: " + messageId);
        }
    }

    @Override
    public String toString() {
        return "IdempotentOnCompletion[" + messageId + ']';
    }
}
