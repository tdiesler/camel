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
package org.apache.camel.component.msmq;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.msmq.native_support.ByteArray;
import org.apache.camel.component.msmq.native_support.MsmqMessage;
import org.apache.camel.component.msmq.native_support.MsmqQueue;
import org.apache.camel.component.msmq.native_support.msmq_native_support;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link Producer} implementation for MSMQ
 * 
 * @version $Revision: 630591 $
 */
public class MsmqProducer extends DefaultProducer<DefaultExchange> {
	private static final transient Log LOG = LogFactory
			.getLog(MsmqProducer.class);

	private final MsmqQueue queue;
	private       boolean   deliveryPersistent = false;
	private       int       timeToLive = msmq_native_support.INFINITE;
	private       int       priority = 3;

	public MsmqProducer(MsmqEndpoint endpoint) {
		super(endpoint);
		this.queue = new MsmqQueue();
		String deliveryPersistentParameter = (String) endpoint.getParameters().get("deliveryPersistent");
		if(deliveryPersistentParameter != null) {
			if(deliveryPersistentParameter.equals("true"))
				this.deliveryPersistent = true;
		}
		String timeToLiveParameter = (String) endpoint.getParameters().get("timeToLive");
		if(timeToLiveParameter != null) {
			timeToLive = Integer.parseInt(timeToLiveParameter);
		}
		String priorityParameter = (String) endpoint.getParameters().get("priority");
		if(priorityParameter != null) {
			priority = Integer.parseInt(priorityParameter);
		}
	}

	public void process(Exchange exchange) throws Exception {
		if (!queue.isOpen())
			openConnection();
		byte[] body = exchange.getIn().getBody(byte[].class);
		if (body == null) {
			LOG.warn("No payload for exchange: " + exchange);
		} else {
			if (ExchangeHelper.isInCapable(exchange)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Writing body : " + body);
				}
				int length = body.length;
				MsmqMessage msmqMessage = new MsmqMessage();
				ByteArray ba = new ByteArray(length);
				for(int i=0; i<length; ++i)
					ba.setitem(i, body[i]);
				msmqMessage.setMsgBody(ba.cast());
				msmqMessage.setBodySize(length);
				if(deliveryPersistent)
					msmqMessage.setDelivery(msmq_native_support.MQMSG_DELIVERY_RECOVERABLE);
				msmqMessage.setTimeToBeReceived(timeToLive);
				msmqMessage.setPriority(priority);
				queue.sendMessage(msmqMessage);
			}
		}
	}

	@Override
	protected void doStart() throws Exception {
	}

	@Override
	protected void doStop() throws Exception {
		if(queue.isOpen())
			queue.close();
	}

	private void openConnection() {
		queue.open(((MsmqEndpoint) getEndpoint()).getRemaining(), msmq_native_support.MQ_SEND_ACCESS);
	}

}
