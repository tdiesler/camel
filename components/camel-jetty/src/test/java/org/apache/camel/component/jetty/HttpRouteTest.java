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
package org.apache.camel.component.jetty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class HttpRouteTest extends CamelTestSupport {
    protected static final String POST_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<test>Hello World</test>";
    protected String expectedBody = "<hello>world!</hello>";

    @Test
    public void testEndpoint() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:a");
        mockEndpoint.expectedBodiesReceived(expectedBody);

        invokeHttpEndpoint();

        mockEndpoint.assertIsSatisfied();
        List<Exchange> list = mockEndpoint.getReceivedExchanges();
        Exchange exchange = list.get(0);
        assertNotNull("exchange", exchange);

        Message in = exchange.getIn();
        assertNotNull("in", in);

        Map<String, Object> headers = in.getHeaders();

        log.info("Headers: " + headers);

        assertTrue("Should be more than one header but was: " + headers, headers.size() > 0);
    }

    @Test
    public void testHelloEndpoint() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = new URL("http://localhost:9080/hello").openStream();
        int c;
        while ((c = is.read()) >= 0) {
            os.write(c);
        }

        String data = new String(os.toByteArray());
        assertEquals("<b>Hello World</b>", data);
    }

    @Test
    public void testEchoEndpoint() throws Exception {
        String out = template.requestBody("http://localhost:9080/echo", "HelloWorld", String.class);
        assertEquals("Get a wrong output ", "HelloWorld", out);
    }

    @Test
    public void testPostParameter() throws Exception {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("request", "PostParameter"));
        nameValuePairs.add(new BasicNameValuePair("others", "bloggs"));
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:9080/parameter");
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
        post.setEntity(entity);
        HttpResponse httpResponse = client.execute(post);
        InputStream response = httpResponse.getEntity().getContent();
        String out = context.getTypeConverter().convertTo(String.class, response);
        assertEquals("Get a wrong output ", "PostParameter", out);
    }

    @Test
    public void testPostXMLMessage() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:9080/postxml");
        StringEntity entity = new StringEntity(POST_MESSAGE, "UTF-8");
        entity.setContentType("application/xml" + HTTP.CHARSET_PARAM + "UTF-8");
        post.setEntity(entity);
        HttpResponse httpResponse = client.execute(post);
        InputStream response = httpResponse.getEntity().getContent();
        String out = context.getTypeConverter().convertTo(String.class, response);
        assertEquals("Get a wrong output ", "OK", out);
    }

    @Test
    public void testPostParameterInURI() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:9080/parameter?request=PostParameter&others=bloggs");
        StringEntity entity = new StringEntity(POST_MESSAGE, "UTF-8");
        entity.setContentType("application/xml" + HTTP.CHARSET_PARAM + "UTF-8");
        post.setEntity(entity);
        HttpResponse httpResponse = client.execute(post);
        InputStream response = httpResponse.getEntity().getContent();
        String out = context.getTypeConverter().convertTo(String.class, response);
        assertEquals("Get a wrong output ", "PostParameter", out);
    }

    @Test
    public void testPutParameterInURI() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPut put = new HttpPut("http://localhost:9080/parameter?request=PutParameter&others=bloggs");
        StringEntity entity = new StringEntity(POST_MESSAGE, "UTF-8");
        entity.setContentType("application/xml" + HTTP.CHARSET_PARAM + "UTF-8");
        put.setEntity(entity);
        HttpResponse httpResponse = client.execute(put);
        InputStream response = httpResponse.getEntity().getContent();
        String out = context.getTypeConverter().convertTo(String.class, response);
        assertEquals("Get a wrong output ", "PutParameter", out);
    }

    protected void invokeHttpEndpoint() throws IOException {
        template.requestBodyAndHeader("http://localhost:9080/test", expectedBody, "Content-Type", "application/xml");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                // enable stream cache
                context.setStreamCaching(true);

                from("jetty:http://localhost:9080/test").to("mock:a");

                Processor proc = new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        try {
                            HttpMessage message = (HttpMessage) exchange.getIn();
                            HttpSession session = message.getRequest().getSession();
                            assertNotNull("we should get session here", session);
                        } catch (Exception e) {
                            exchange.getOut().setFault(true);
                            exchange.getOut().setBody(e);
                        }
                        exchange.getOut().setBody("<b>Hello World</b>");
                    }
                };

                Processor printProcessor = new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Message out = exchange.getOut();
                        out.copyFrom(exchange.getIn());
                        log.info("The body's object is " + exchange.getIn().getBody());
                        log.info("Process body = " + exchange.getIn().getBody(String.class));
                        InputStreamCache cache = out.getBody(InputStreamCache.class);
                        cache.reset();
                    }
                };
                from("jetty:http://localhost:9080/hello?sessionSupport=true").process(proc);

                from("jetty:http://localhost:9080/echo").process(printProcessor).process(printProcessor);

                Processor procParameters = new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);
                        String value = req.getParameter("request");
                        String requestValue = exchange.getIn().getHeader("request", String.class);
                        if (value != null) {
                            assertEquals("We should get the same request header value from message", value, requestValue);
                            exchange.getOut().setBody(value);
                        } else {
                            exchange.getOut().setBody("Can't get a right parameter");
                        }
                    }
                };

                from("jetty:http://localhost:9080/parameter").process(procParameters);

                from("jetty:http://localhost:9080/postxml").process(new Processor() {

                    public void process(Exchange exchange) throws Exception {
                        String value = exchange.getIn().getBody(String.class);
                        assertEquals("The response message is wrong", value, POST_MESSAGE);
                        exchange.getOut().setBody("OK");
                    }

                });

            }
        };
    }
}


