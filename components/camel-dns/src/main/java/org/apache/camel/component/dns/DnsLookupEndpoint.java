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
package org.apache.camel.component.dns;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

/**
 * An endpoint to manage lookup operations, using the API from dnsjava.
 */
public class DnsLookupEndpoint extends DefaultEndpoint {

    public DnsLookupEndpoint(Component component) {
        super("dns://lookup", component);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    public Producer createProducer() throws Exception {
        return new DefaultProducer(this) {
            public void process(Exchange exchange) throws Exception {
                String dnsName = exchange.getIn().getHeader(DnsConstants.DNS_NAME, String.class);
                ObjectHelper.notEmpty(dnsName, "Header " + DnsConstants.DNS_NAME);

                Object type = exchange.getIn().getHeader(DnsConstants.DNS_TYPE);
                Integer dnsType = null;
                if (type != null) {
                    dnsType = Type.value(String.valueOf(type));
                }
                Object dclass = exchange.getIn().getHeader(DnsConstants.DNS_CLASS);
                Integer dnsClass = null;
                if (dclass != null) {
                    dnsClass = DClass.value(String.valueOf(dclass));
                }

                Lookup lookup;
                if (dnsType != null && dnsClass != null) {
                    lookup = new Lookup(dnsName, dnsType, dnsClass);
                } else {
                    if (dnsType != null) {
                        lookup = new Lookup(dnsName, dnsType);
                    } else {
                        lookup = new Lookup(dnsName);
                    }
                }

                lookup.run();
                if (lookup.getAnswers() != null) {
                    exchange.getIn().setBody(lookup.getAnswers());
                } else {
                    exchange.getIn().setBody(lookup.getErrorString());
                }
            }
        };
    }

    public boolean isSingleton() {
        return false;
    }
}
