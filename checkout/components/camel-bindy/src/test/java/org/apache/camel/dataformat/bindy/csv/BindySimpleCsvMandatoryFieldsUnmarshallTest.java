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
package org.apache.camel.dataformat.bindy.csv;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.test.JavaConfigContextLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

import static org.junit.Assert.fail;

@ContextConfiguration(locations = "org.apache.camel.dataformat.bindy.csv.BindySimpleCsvMandatoryFieldsUnmarshallTest$ContextConfig", loader = JavaConfigContextLoader.class)
public class BindySimpleCsvMandatoryFieldsUnmarshallTest extends AbstractJUnit4SpringContextTests {

    private static final transient Log LOG = LogFactory
        .getLog(BindySimpleCsvMandatoryFieldsUnmarshallTest.class);

    @EndpointInject(uri = "mock:result1")
    protected MockEndpoint resultEndpoint1;

    @EndpointInject(uri = "mock:result2")
    protected MockEndpoint resultEndpoint2;

    @Produce(uri = "direct:start1")
    protected ProducerTemplate template1;

    @Produce(uri = "direct:start2")
    protected ProducerTemplate template2;
  
    String header = "order nr,client ref,first name, last name,instrument code,instrument name,order type, instrument type, quantity,currency,date\r\n";
    String record1 = ""; // empty records
    String record2 = ",,blabla,,,,,,,,"; // optional fields
    String record3 = "1,A1,Charles,Moulliard,ISIN,LU123456789,,,,,"; // mandatory fields present (A1, Charles, Moulliard)
    String record4 = "1,A1,Charles,,ISIN,LU123456789,,,,,"; // mandatory field missing
    String record5 = ",,,,,,,,,,"; // record with no data
    String record6 = ",,,,,,,,,,,,,,"; // too much data in the record (only 11 are accepted by the model
    
    @DirtiesContext
    @Test
    public void testEmptyRecord() throws Exception {
        resultEndpoint1.expectedMessageCount(0);

        try {
            template1.sendBody(record1);
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            Assert.isInstanceOf(Exception.class, e.getCause());
            // LOG.info(">> Error : " + e);
        }

        resultEndpoint1.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testEmptyFields() throws Exception {
        resultEndpoint1.expectedMessageCount(1);
        template1.sendBody(record2);

        resultEndpoint1.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testOneOptionalField() throws Exception {
        resultEndpoint1.expectedMessageCount(1);

        template1.sendBody(record2);
        resultEndpoint1.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testSeveralOptionalFields() throws Exception {
        resultEndpoint1.expectedMessageCount(1);

        template1.sendBody(record3);
        resultEndpoint1.assertIsSatisfied();
    }
    
    @DirtiesContext
    @Test
    public void testTooMuchFields() throws Exception {
        resultEndpoint1.expectedMessageCount(0);
        
        try {
            template1.sendBody(record6);
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            // expected
            Assert.isInstanceOf(IllegalArgumentException.class, e.getCause());
        }

        resultEndpoint1.assertIsSatisfied();
    }
    
    @DirtiesContext
    @Test
    public void testMandatoryFields() throws Exception {
        resultEndpoint2.expectedMessageCount(1);

        template2.sendBody(header + record3);
        resultEndpoint2.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testMissingMandatoryFields() throws Exception {
        resultEndpoint2.expectedMessageCount(1);

        try {
            template2.sendBody(header + record4);
            resultEndpoint2.assertIsSatisfied();
        } catch (CamelExecutionException e) {
            // LOG.info(">> Error : " + e);
        }
    }

    @Configuration
    public static class ContextConfig extends SingleRouteCamelConfiguration {
        BindyCsvDataFormat formatOptional = 
            new BindyCsvDataFormat("org.apache.camel.dataformat.bindy.model.simple.oneclass");
        BindyCsvDataFormat formatMandatory =
            new BindyCsvDataFormat("org.apache.camel.dataformat.bindy.model.simple.oneclassmandatory");

        @Override
        @Bean
        public RouteBuilder route() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:start1").unmarshal(formatOptional).to("mock:result1");
                    from("direct:start2").unmarshal(formatMandatory).to("mock:result2");
                }
            };
        }
    }
}

    