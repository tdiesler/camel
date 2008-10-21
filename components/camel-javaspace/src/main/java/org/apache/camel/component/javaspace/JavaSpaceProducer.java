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
package org.apache.camel.component.javaspace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.server.UID;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.bean.BeanInvocation;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link Producer} implementation for JavaSpaces
 * 
 * @version $Revision$
 */
public class JavaSpaceProducer extends DefaultProducer<DefaultExchange> {
	private static final transient Log LOG = LogFactory
			.getLog(JavaSpaceProducer.class);

	private final boolean transactional;
	private final long transactionTimeout;
	private JavaSpace javaSpace = null;
	private TransactionHelper transactionHelper = null;

	public JavaSpaceProducer(JavaSpaceEndpoint endpoint) throws Exception {
		super(endpoint);
		this.transactional = endpoint.isTransactional();
		this.transactionTimeout = endpoint.getTransactionTimeout();
	}

	public void process(Exchange exchange) throws Exception {
		Object body = exchange.getIn().getBody();
		Entry entry = null;
		if (!(body instanceof Entry)) {
			entry = new InEntry();

			if (body instanceof BeanInvocation) {
				((InEntry) entry).correlationId = (new UID()).toString();
			}

			if (body instanceof byte[]) {
				((InEntry) entry).binary = true;
				((InEntry) entry).buffer = (byte[]) body;
			} else {
				((InEntry) entry).binary = false;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(body);
				((InEntry) entry).buffer = bos.toByteArray();
			}
		} else {
			entry = (Entry) body;
		}
		if (entry == null) {
			LOG.warn("No payload for exchange: " + exchange);
		} else {
			Transaction tnx = null;
			if (transactionHelper != null)
				tnx = transactionHelper.getJiniTransaction(transactionTimeout).transaction;
			if (ExchangeHelper.isInCapable(exchange)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Writing body : " + entry);
				}
				javaSpace.write(entry, tnx, Lease.FOREVER);
			}
			if (ExchangeHelper.isOutCapable(exchange)) {
				OutEntry tmpl = new OutEntry();
				tmpl.correlationId = ((InEntry) entry).correlationId;

				OutEntry replyCamelEntry = null;
				while (replyCamelEntry == null) {
					replyCamelEntry = (OutEntry) javaSpace.take(tmpl, tnx, 100);
				}

				Object obj = null;
				if (((OutEntry) replyCamelEntry).binary) {
					obj = ((OutEntry) replyCamelEntry).buffer;
				} else {
					ByteArrayInputStream bis = new ByteArrayInputStream(
							((OutEntry) replyCamelEntry).buffer);
					ObjectInputStream ois = new ObjectInputStream(bis);
					obj = ois.readObject();
				}
				exchange.getOut().setBody(obj);
			}
			if (tnx != null)
				tnx.commit();
		}
	}

	@Override
	protected void doStart() throws Exception {

		Utility.setSecurityPolicy("policy.all", "policy_producer.all");

		javaSpace = JiniSpaceAccessor.findSpace(((JavaSpaceEndpoint) this
				.getEndpoint()).getRemaining(), ((JavaSpaceEndpoint) this
				.getEndpoint()).getSpaceName());
		if (transactional)
			transactionHelper = TransactionHelper
					.getInstance(((JavaSpaceEndpoint) this.getEndpoint())
							.getRemaining());

		(new File("policy_producer.all")).delete();
	}

	@Override
	protected void doStop() throws Exception {
	}

}
