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
package org.apache.camel.spring.interceptor;

import javax.sql.DataSource;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.spring.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Unit test to demonstrate the transactional client pattern.
 */
public class MixedTransactionPropagationTest extends SpringTestSupport {

    protected JdbcTemplate jdbc;

    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(
            "/org/apache/camel/spring/interceptor/MixedTransactionPropagationTest.xml");
    }

    protected int getExpectedRouteCount() {
        return 0;
    }

    @Override
    protected void setUp() throws Exception {
        this.disableJMX();
        super.setUp();

        final DataSource ds = getMandatoryBean(DataSource.class, "dataSource");
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("create table books (title varchar(50))");
        jdbc.update("insert into books (title) values (?)", new Object[] {"Camel in Action"});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        jdbc.execute("drop table books");
        this.enableJMX();
    }

    public void testOkay() throws Exception {
        template.sendBody("direct:okay", "Hello World");

        int count = jdbc.queryForInt("select count(*) from books");
        assertEquals("Number of books", 3, count);
    }

    public void testFail() throws Exception {
        try {
            template.sendBody("direct:fail", "Hello World");
        } catch (RuntimeCamelException e) {
            // expected as we fail
            assertIsInstanceOf(RuntimeCamelException.class, e.getCause());
            assertTrue(e.getCause().getCause() instanceof IllegalArgumentException);
            assertEquals("We don't have Donkeys, only Camels", e.getCause().getCause().getMessage());
        }

        int count = jdbc.queryForInt("select count(*) from books");
        assertEquals("Number of books", 1, count);
    }

    public void testMixed() throws Exception {
        template.sendBody("direct:mixed", "Hello World");

        int count = jdbc.queryForInt("select count(*) from books");
        assertEquals("Number of books", 4, count);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new SpringRouteBuilder() {
            public void configure() throws Exception {
                from("direct:okay")
                    .transacted("PROPAGATION_REQUIRED")
                    .setBody(constant("Tiger in Action")).beanRef("bookService")
                    .setBody(constant("Elephant in Action")).beanRef("bookService");

                from("direct:fail")
                    .transacted("PROPAGATION_REQUIRED")
                    .setBody(constant("Tiger in Action")).beanRef("bookService")
                    .setBody(constant("Donkey in Action")).beanRef("bookService");

                // START SNIPPET: e1
                from("direct:mixed")
                    // using required
                    .transacted("PROPAGATION_REQUIRED")
                    // all these steps will be okay
                    .setBody(constant("Tiger in Action")).beanRef("bookService")
                    .setBody(constant("Elephant in Action")).beanRef("bookService")
                    .setBody(constant("Lion in Action")).beanRef("bookService")
                    // continue on route 2
                    .to("direct:mixed2");

                from("direct:mixed2")
                    // using a different propagation which is requires new
                    .transacted("PROPAGATION_REQUIRES_NEW")
                    // tell Camel that if this route fails then only rollback this last route
                    // by using (rollback only *last*)
                    .onException(Exception.class).markRollbackOnlyLast().end()
                    // this step will be okay
                    .setBody(constant("Giraffe in Action")).beanRef("bookService")
                    // this step will fail with donkey
                    .setBody(constant("Donkey in Action")).beanRef("bookService");
                // END SNIPPET: e1
            }
        };
    }

}