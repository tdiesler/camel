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
package org.apache.camel.converter.soap;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import com.example.customerservice.GetCustomersByName;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.soap.name.ElementNameStrategy;
import org.apache.camel.converter.soap.name.TypeNameStrategy;
import org.apache.camel.test.CamelTestSupport;

public class SoapServerTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate producer;

    public void testSuccess() throws IOException, InterruptedException {
        sendAndCheckReply("request.xml", "response.xml");
    }

    public void testFault() throws IOException, InterruptedException {
        sendAndCheckReply("requestFault.xml", "responseFault.xml");
    }

    private void sendAndCheckReply(String requestResource, String responseResource) throws IOException {
        context.setTracing(true);
        InputStream requestIs = this.getClass().getResourceAsStream(requestResource);
        InputStream responseIs = this.getClass().getResourceAsStream(responseResource);
        Object reply = producer.requestBody(requestIs);
        String replySt = context.getTypeConverter().convertTo(String.class, reply);
        Assert.assertEquals(TestUtil.readStream(responseIs), replySt);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            String jaxbPackage = GetCustomersByName.class.getPackage().getName();

            @Override
            public void configure() throws Exception {
                ElementNameStrategy elNameStrat = new TypeNameStrategy();
                SoapJaxbDataFormat soapDataFormat = new SoapJaxbDataFormat(jaxbPackage, elNameStrat);
                CustomerServerBean serverBean = new CustomerServerBean();
                from("direct:start").onException(Exception.class) // 
                        .handled(true) //
                        .marshal(soapDataFormat) //
                        .end() //
                    .unmarshal(soapDataFormat) //
                    .bean(serverBean) //
                    .marshal(soapDataFormat);
            }
        };
    }

}
