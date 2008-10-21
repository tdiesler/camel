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

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;

/**
 * @version $Revision$
 */
public class TransactionHelper {
	private static TransactionHelper me = null;

	private String             uri = null;
	private TransactionManager trManager = null;

	public TransactionHelper(String uri) {
		this.uri = uri;
	}

	public static TransactionHelper getInstance(String uri) {
		if (me == null) {
			me = new TransactionHelper(uri);
		}
		return me;
	}

	/**
	 * getJiniTransaction Returns a transaction manager proxy.
	 * 
	 * @param timeout -
	 *            The length of time our transaction should live before timing
	 *            out.
	 * @return Transaction.Created
	 * @throws Exception
	 */
	public Transaction.Created getJiniTransaction(long timeout)
			throws Exception {
		if (null == trManager) {
			trManager = findTransactionManager(uri);
		}
		Transaction.Created tCreated = TransactionFactory.create(trManager,	timeout);
		return tCreated;
	}

	private TransactionManager findTransactionManager(String uri) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		// Creating service template to find transaction manager service by
		// matching fields.
		Class<?>[] classes = new Class<?>[] { net.jini.core.transaction.server.TransactionManager.class };
		// Name sn = new Name("*");
		ServiceTemplate tmpl = new ServiceTemplate(null, classes,
				new Entry[] {});

		// Creating a lookup locator.
		LookupLocator locator = null;
		try {
			locator = new LookupLocator(uri);
		} catch (MalformedURLException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}

		ServiceRegistrar sr = null;
		try {
			sr = locator.getRegistrar();
		} catch (ClassNotFoundException ex1) {
			ex1.printStackTrace();
		} catch (IOException ex1) {
			ex1.printStackTrace();
		}

		TransactionManager tm = null;
		try {
			tm = (TransactionManager) sr.lookup(tmpl);
		} catch (RemoteException ex2) {
			ex2.printStackTrace();
		}
		return tm;
	}
}
