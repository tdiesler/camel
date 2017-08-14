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
package org.apache.camel.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.TestSupport;
import org.apache.camel.converter.IOConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

/**
 *
 */
public class ResourceHelperTest extends TestSupport {

    public void testLoadFile() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "file:src/test/resources/log4j2.properties");
        assertNotNull(is);

        String text = context.getTypeConverter().convertTo(String.class, is);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));
        is.close();

        context.stop();
    }

    public void testLoadFileWithSpace() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        createDirectory("target/my space");
        FileUtil.copyFile(new File("src/test/resources/log4j2.properties"), new File("target/my space/log4j2.properties"));

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "file:target/my%20space/log4j2.properties");
        assertNotNull(is);

        String text = context.getTypeConverter().convertTo(String.class, is);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));
        is.close();

        context.stop();
    }

    public void testLoadClasspath() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "classpath:log4j2.properties");
        assertNotNull(is);

        String text = context.getTypeConverter().convertTo(String.class, is);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));
        is.close();

        context.stop();
    }

    public void testLoadRegistry() throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("myBean", "This is a log4j logging configuration file");

        CamelContext context = new DefaultCamelContext(registry);
        context.start();

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "ref:myBean");
        assertNotNull(is);

        String text = context.getTypeConverter().convertTo(String.class, is);
        assertNotNull(text);
        assertTrue(text.contains("log4j"));
        is.close();

        context.stop();
    }

    public void testLoadClasspathDefault() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "log4j2.properties");
        assertNotNull(is);

        String text = context.getTypeConverter().convertTo(String.class, is);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));
        is.close();

        context.stop();
    }

    public void testLoadFileNotFound() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        try {
            ResourceHelper.resolveMandatoryResourceAsInputStream(context, "file:src/test/resources/notfound.txt");
            fail("Should not find file");
        } catch (FileNotFoundException e) {
            assertTrue(e.getMessage().contains("notfound.txt"));
        }

        context.stop();
    }

    public void testLoadClasspathNotFound() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        try {
            ResourceHelper.resolveMandatoryResourceAsInputStream(context, "classpath:notfound.txt");
            fail("Should not find file");
        } catch (FileNotFoundException e) {
            assertEquals("Cannot find resource: classpath:notfound.txt in classpath for URI: classpath:notfound.txt", e.getMessage());
        }

        context.stop();
    }

    public void testLoadFileAsUrl() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        URL url = ResourceHelper.resolveMandatoryResourceAsUrl(context.getClassResolver(), "file:src/test/resources/log4j2.properties");
        assertNotNull(url);

        String text = context.getTypeConverter().convertTo(String.class, url);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));

        context.stop();
    }

    public void testLoadClasspathAsUrl() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        URL url = ResourceHelper.resolveMandatoryResourceAsUrl(context.getClassResolver(), "classpath:log4j2.properties");
        assertNotNull(url);

        String text = context.getTypeConverter().convertTo(String.class, url);
        assertNotNull(text);
        assertTrue(text.contains("rootLogger"));

        context.stop();
    }

    public void testLoadCustomUrlasInputStream() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        String handlerPackageSystemProp = "java.protocol.handler.pkgs";
        String customUrlHandlerPackage = "org.apache.camel.urlhandler";

        registerSystemProperty(handlerPackageSystemProp, customUrlHandlerPackage, "|");

        InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "custom://hello");
        assertNotNull(is);

        assertEquals("hello", IOConverter.toString(IOHelper.buffered(new InputStreamReader(is, "UTF-8"))));

        context.stop();
    }

    public void testLoadCustomUrlasInputStreamFail() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        try {
            InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "custom://hello");
            assertNotNull(is);
        } catch (Exception e) {
            assertEquals("unknown protocol: custom", e.getMessage());
        }

        context.stop();
    }

    public void testLoadCustomUrl() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        String handlerPackageSystemProp = "java.protocol.handler.pkgs";
        String customUrlHandlerPackage = "org.apache.camel.urlhandler";
        registerSystemProperty(handlerPackageSystemProp, customUrlHandlerPackage, "|");

        URL url = ResourceHelper.resolveResourceAsUrl(context.getClassResolver(), "custom://hello");
        assertNotNull(url);

        String text = context.getTypeConverter().convertTo(String.class, url);
        assertNotNull(text);
        assertTrue(text.contains("hello"));

        context.stop();
    }

    public void testLoadCustomUrlFail() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();

        try {
            URL url = ResourceHelper.resolveResourceAsUrl(context.getClassResolver(), "custom://hello");
        } catch (Exception e) {
            assertEquals("unknown protocol: custom", e.getMessage());
        }

        context.stop();
    }

    public void testIsHttp() throws Exception {
        assertFalse(ResourceHelper.isHttpUri("direct:foo"));
        assertFalse(ResourceHelper.isHttpUri(""));
        assertFalse(ResourceHelper.isHttpUri(null));

        assertTrue(ResourceHelper.isHttpUri("http://camel.apache.org"));
        assertTrue(ResourceHelper.isHttpUri("https://camel.apache.org"));
    }

    public void testGetScheme() throws Exception {
        assertEquals("file:", ResourceHelper.getScheme("file:myfile.txt"));
        assertEquals("classpath:", ResourceHelper.getScheme("classpath:myfile.txt"));
        assertEquals("http:", ResourceHelper.getScheme("http:www.foo.com"));
        assertEquals(null, ResourceHelper.getScheme("www.foo.com"));
        assertEquals(null, ResourceHelper.getScheme("myfile.txt"));
    }

    public void testAppendParameters() throws Exception {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("foo", 123);
        params.put("bar", "yes");

        // should clear the map after usage
        assertEquals("http://localhost:8080/data?foo=123&bar=yes", ResourceHelper.appendParameters("http://localhost:8080/data", params));
        assertEquals(0, params.size());
    }

}
