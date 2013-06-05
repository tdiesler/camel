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
package org.apache.camel.component.sql;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * @version 
 */
public class SqlProducerSeparatorTest extends CamelTestSupport {

    private EmbeddedDatabase db;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY).addScript("sql/createAndPopulateDatabase.sql").build();

        jdbcTemplate = new JdbcTemplate(db);

        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        
        db.shutdown();
    }

    @Test
    public void testSeparator() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", "4;'Food, Inc';'LGPL'");

        mock.assertIsSatisfied();

        assertEquals(4, jdbcTemplate.queryForInt("select count(*) from projects"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                getContext().getComponent("sql", SqlComponent.class).setDataSource(db);

                from("direct:start")
                    .to("sql:insert into projects (id, project, license) values (#, #, #)?separator=;")
                    .to("mock:result");
            }
        };
    }
}
