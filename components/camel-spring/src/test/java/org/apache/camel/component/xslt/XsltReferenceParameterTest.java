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
package org.apache.camel.component.xslt;

import javax.xml.transform.TransformerFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.TestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XsltBuilder;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.util.jndi.JndiContext;

/**
 * Unit test for testing reference parameters. 
 */
public class XsltReferenceParameterTest extends TestSupport {

    private static final String TEST_URI_1 = 
        "xslt:org/apache/camel/component/xslt/transform.xsl?converter=#testConverter&transformerFactory=#testTransformerFactory";
    private static final String TEST_URI_2 =
        "xslt:org/apache/camel/component/xslt/transform.xsl?converter=testConverter&transformerFactory=testTransformerFactory";
    
    private TestConverter testConverter;
    private TransformerFactory testTransformerFactory;
    
    private XsltBuilder builder1;
    private XsltBuilder builder2;
    
    public void setUp() throws Exception {
        JndiRegistry registry = new JndiRegistry(new JndiContext());
        RouteBuilder builder = createRouteBuilder();
        CamelContext context = new DefaultCamelContext(registry);
        
        testConverter = new TestConverter();
        testTransformerFactory = TransformerFactory.newInstance();
        
        registry.bind("testConverter", testConverter);
        registry.bind("testTransformerFactory", testTransformerFactory);
        
        ProcessorEndpoint pep1 = (ProcessorEndpoint)context.getEndpoint(TEST_URI_1);
        ProcessorEndpoint pep2 = (ProcessorEndpoint)context.getEndpoint(TEST_URI_2);
        
        builder1 = (XsltBuilder)pep1.getProcessor();
        builder2 = (XsltBuilder)pep2.getProcessor();
        
        context.addRoutes(builder);
        context.start();
    }

    public void testConverterReference() {
        assertSame(testConverter, builder1.getConverter());
        assertSame(testConverter, builder2.getConverter());
    }
    
    public void testTransformerFactoryReference() {
        assertSame(testTransformerFactory, builder1.getConverter().getTransformerFactory());
        assertSame(testTransformerFactory, builder2.getConverter().getTransformerFactory());
    }
    
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:a").to(TEST_URI_1);
                from("direct:b").to(TEST_URI_2);
            }
        };
    }
    
    private static class TestConverter extends XmlConverter {
    }

}
