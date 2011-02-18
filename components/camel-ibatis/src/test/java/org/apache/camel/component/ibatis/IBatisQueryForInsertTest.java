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
package org.apache.camel.component.ibatis;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * @version 
 */
public class IBatisQueryForInsertTest extends IBatisTestSupport {

    @Test
    public void testInsert() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        Account account = new Account();
        account.setId(444);
        account.setFirstName("Willem");
        account.setLastName("Jiang");
        account.setEmailAddress("Faraway@gmail.com");

        template.sendBody("direct:start", account);

        assertMockEndpointsSatisfied();

        // there should be 3 rows now
        Integer rows = template.requestBody("ibatis:count?statementType=QueryForObject", null, Integer.class);
        assertEquals("There should be 3 rows", 3, rows.intValue());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .to("ibatis:insertAccount?statementType=Insert")
                    .to("mock:result");
            }
        };
    }

}