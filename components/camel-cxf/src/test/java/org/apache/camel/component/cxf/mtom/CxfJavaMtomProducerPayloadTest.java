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
package org.apache.camel.component.cxf.mtom;

import java.awt.Image;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.junit.Test;

public class CxfJavaMtomProducerPayloadTest extends CxfMtomConsumerTest {
    protected static final String MTOM_ENDPOINT_URI_MTOM_ENABLE = 
        MTOM_ENDPOINT_URI + "&defaultOperationName=Detail";
    
    @SuppressWarnings("unchecked")
    @Test
    public void testInvokingService() throws Exception {   
        if (MtomTestHelper.isAwtHeadless(null, log)) {
            return;
        }

        final Holder<byte[]> photo = new Holder<>("RequestFromCXF".getBytes("UTF-8"));
        final Holder<Image> image = new Holder<>(getImage("/java.jpg"));
        
        Exchange exchange = context.createProducerTemplate().send(MTOM_ENDPOINT_URI_MTOM_ENABLE, new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new Object[] {photo, image});
                
            }
            
        });
        
        Message out = exchange.getOut();
        assertEquals("We should get 2 attachements here.", 2, out.getAttachments().size());
        assertEquals("Get a wrong Content-Type header", "application/xop+xml", out.getHeader("Content-Type"));
        // Get the parameter list
        List<?> parameter = out.getBody(List.class);
        // Get the operation name
        final Holder<byte[]> responsePhoto = (Holder<byte[]>)parameter.get(1);
        assertNotNull("The photo should not be null", responsePhoto.value);
        assertEquals("Should get the right response", new String(responsePhoto.value, "UTF-8"),
                     "ResponseFromCamel");
        
        final Holder<Image> responseImage = (Holder<Image>)parameter.get(2);
        assertNotNull("We should get the image here", responseImage.value);
        
        
    }

}
