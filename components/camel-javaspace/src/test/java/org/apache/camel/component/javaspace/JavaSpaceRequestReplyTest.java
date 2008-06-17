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

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision: 630574 $
 */
public class JavaSpaceRequestReplyTest extends ContextTestSupport {

	private ClassPathXmlApplicationContext spring;

	public void testJavaSpaceRequestReply() throws Exception {
		Endpoint<?> endpoint = context.getEndpoint("direct:input");
		ITestPojo proxy = ProxyHelper.createProxy(endpoint, ITestPojo.class);
		Request req = new Request();
		long start = System.currentTimeMillis();		
		for(int i=0; i<1000; ++i) {
			req.setPayload("REQUEST "+i);
			Reply reply = proxy.method(req);
			Assert.assertTrue(reply.getPayload().equals("REPLY for REQUEST " + i));
		}
		long stop = System.currentTimeMillis();
		System.out.println(stop - start);
	}

	@Override
	protected CamelContext createCamelContext() throws Exception {
		spring = new ClassPathXmlApplicationContext(
				"org/apache/camel/component/javaspace/spring.xml");
		SpringCamelContext ctx = SpringCamelContext.springCamelContext(spring);
		return ctx;
	};

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {

		return new RouteBuilder() {
			public void configure() {
				
				from("direct:input").to("javaspace:jini://localhost?spaceName=mySpace");
				
				from("javaspace:jini://localhost?concurrentConsumers=10&spaceName=mySpace").to("pojo:pojo");
				
				from("javaspace:jini://localhost?concurrentConsumers=10&spaceName=mySpace").to("pojo:pojo");
				
				from("javaspace:jini://localhost?concurrentConsumers=10&spaceName=mySpace").to("pojo:pojo");
			}
		};
	}
}
