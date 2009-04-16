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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.msmq.native_support.ByteArray;
import org.apache.camel.component.msmq.native_support.MsmqMessage;
import org.apache.camel.component.msmq.native_support.MsmqQueue;
import org.apache.camel.component.msmq.native_support.msmq_native_supportConstants;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A
 * 
 * @{link Consumer} implementation for MSMQ
 * @version $Revision: 631882 $
 */
public class MsmqConsumer extends DefaultConsumer<DefaultExchange> {

	@SuppressWarnings("unused")
	private static final transient Log LOG = LogFactory
			.getLog(MsmqConsumer.class);

	private final ConcurrentLinkedQueue<MsmqQueue> queues;
	private final int                              concurrentConsumers;
	private       int                              initialBufferSize = 128;
	private       int                              incrementBufferSize = 128;
	private final ScheduledThreadPoolExecutor      executor;

	public MsmqConsumer(final MsmqEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.queues = new ConcurrentLinkedQueue<MsmqQueue>();
		String concurrentConsumersParameter = (String) endpoint.getParameters().get("concurrentConsumers");
		if(concurrentConsumersParameter != null)
			this.concurrentConsumers = Integer.parseInt(concurrentConsumersParameter);
		else
			this.concurrentConsumers = 1;
		String initialBufferSizeParameter = (String) endpoint.getParameters().get("initialBufferSize");
		if(initialBufferSizeParameter != null) {
			initialBufferSize = Integer.parseInt(initialBufferSizeParameter);
		}
		String incrementBufferSizeParameter = (String) endpoint.getParameters().get("incrementBufferSize");
		if(incrementBufferSizeParameter != null) {
			incrementBufferSize = Integer.parseInt(incrementBufferSizeParameter);
		}
		this.executor = new ScheduledThreadPoolExecutor(this.concurrentConsumers);
	}

	@Override
	protected void doStart() throws Exception {
		openQueues();
		for(int i=0; i<concurrentConsumers; ++i) {
			Task worker = new Task((MsmqEndpoint) this.getEndpoint(), this, queues, this.getProcessor());
			executor.scheduleWithFixedDelay(worker, 0, 1, TimeUnit.NANOSECONDS);
		}
	}

	@Override
	protected void doStop() throws Exception {
		executor.shutdown();
		closeQueues();
	}

	private void openQueues() {
		for(int i=0; i<concurrentConsumers; ++i) {
			queues.add(new MsmqQueue());
		}
	}
	
	private void closeQueues() {
		for (Iterator<MsmqQueue> iterator = queues.iterator(); iterator
				.hasNext();) {
			MsmqQueue queue = (MsmqQueue) iterator.next();
			if (queue.isOpen())
				queue.close();
		}
	}

	public int getIncrementBufferSize() {
		return incrementBufferSize;
	}

	public int getInitialBufferSize() {
		return initialBufferSize;
	}
}

class Task implements Runnable {

	private final MsmqEndpoint                     endpoint;
	private final MsmqConsumer                     consumer;
	private final ConcurrentLinkedQueue<MsmqQueue> queues;
	private final Processor                        processor;
	
	public Task(MsmqEndpoint endpoint, MsmqConsumer consumer, ConcurrentLinkedQueue<MsmqQueue> queues, Processor processor) {
		this.endpoint = endpoint;
		this.consumer = consumer;
		this.queues = queues;
		this.processor = processor;
	}

	public void run() {
		int size = queues.size();
		MsmqQueue queue = size==1?queues.element():null;
		try {
			DefaultExchange exchange = endpoint.createExchange();
			MsmqMessage msmqMessage = new MsmqMessage();
			if(size > 1)
				queue = queues.remove();
			if(!queue.isOpen())
				queue.open(endpoint.getRemaining(), msmq_native_supportConstants.MQ_RECEIVE_ACCESS);

			int initsize = consumer.getInitialBufferSize();
			int incrsize = consumer.getIncrementBufferSize();
			boolean cont = true; 
			ByteArray ba = new ByteArray(initsize);
			msmqMessage.setMsgBody(ba.cast());
			msmqMessage.setBodySize(initsize);
			while(cont) {
				try {
					if (queue.receiveMessage(msmqMessage, 100)) {
						Message message = exchange.getIn();
						int length = (int) msmqMessage.getBodySize();
						byte[] buffer = new byte[length];
						for(int i=0; i<length; ++i)
							buffer[i] = ba.getitem(i);
						message.setBody(buffer, byte[].class);
						processor.process(exchange);
						cont = false;
					}
				}
				catch(RuntimeException ex) {
					if(ex.getMessage().equals("Message body too big")) {
						initsize += incrsize;
						ba = new ByteArray(initsize);
						msmqMessage.setMsgBody(ba.cast());
						msmqMessage.setBodySize(initsize);
					} else
						throw ex;
				}
			}
		} catch (Exception e) {
			throw new RuntimeCamelException(e);
		} finally {
			if(size > 1)
				queues.add(queue);
		}
	}
	
}