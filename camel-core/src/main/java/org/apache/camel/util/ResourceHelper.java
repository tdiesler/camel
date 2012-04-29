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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.camel.spi.ClassResolver;

/**
 * Helper class for loading resources on the classpath or file system.
 */
public final class ResourceHelper {

    private ResourceHelper() {
        // utility class
    }

    /**
     * Determines whether the URI has a scheme (e.g. file:, classpath: or http:)
     *
     * @param uri the URI
     * @return <tt>true</tt> if the URI starts with a scheme
     */
    public static boolean hasScheme(String uri) {
        if (uri == null) {
            return false;
        }

        return uri.startsWith("file:") || uri.startsWith("classpath:") || uri.startsWith("http:");
    }

    /**
     * Resolves the mandatory resource.
     * <p/>
     * If possible recommended to use {@link #resolveMandatoryResourceAsUrl(org.apache.camel.spi.ClassResolver, String)}
     *
     * @param classResolver the class resolver to load the resource from the classpath
     * @param uri URI of the resource
     * @return the resource as an {@link InputStream}.  Remember to close this stream after usage.
     * @throws java.io.IOException is thrown if the resource file could not be found or loaded as {@link InputStream}
     */
    public static InputStream resolveMandatoryResourceAsInputStream(ClassResolver classResolver, String uri) throws IOException {
        if (uri.startsWith("file:")) {
            uri = ObjectHelper.after(uri, "file:");
            return new FileInputStream(uri);
        } else if (uri.startsWith("http:")) {
            URL url = new URL(uri);
            URLConnection con = url.openConnection();
            con.setUseCaches(false);
            try {
                return con.getInputStream();
            } catch (IOException e) {
                // close the http connection to avoid
                // leaking gaps in case of an exception
                if (con instanceof HttpURLConnection) {
                    ((HttpURLConnection) con).disconnect();
                }
                throw e;
            }
        } else if (uri.startsWith("classpath:")) {
            uri = ObjectHelper.after(uri, "classpath:");
        }

        // load from classpath by default
        InputStream is = classResolver.loadResourceAsStream(uri);
        if (is == null) {
            throw new FileNotFoundException("Cannot find resource in classpath for URI: " + uri);
        } else {
            return is;
        }
    }

    /**
     * Resolves the mandatory resource.
     *
     * @param classResolver the class resolver to load the resource from the classpath
     * @param uri uri of the resource
     * @return the resource as an {@link InputStream}.  Remember to close this stream after usage.
     * @throws java.io.FileNotFoundException is thrown if the resource file could not be found
     * @throws java.net.MalformedURLException if the URI is malformed
     */
    public static URL resolveMandatoryResourceAsUrl(ClassResolver classResolver, String uri) throws FileNotFoundException, MalformedURLException {
        if (uri.startsWith("file:")) {
            // check if file exists first
            String name = ObjectHelper.after(uri, "file:");
            File file = new File(name);
            if (!file.exists()) {
                throw new FileNotFoundException("File " + file + " not found");
            }
            return new URL(uri);
        } else if (uri.startsWith("http:")) {
            return new URL(uri);
        } else if (uri.startsWith("classpath:")) {
            uri = ObjectHelper.after(uri, "classpath:");
        }

        // load from classpath by default
        URL url = classResolver.loadResourceAsURL(uri);
        if (url == null) {
            throw new FileNotFoundException("Cannot find resource in classpath for URI: " + uri);
        } else {
            return url;
        }
    }

    /**
     * Is the given uri a http uri?
     *
     * @param uri the uri
     * @return <tt>true</tt> if the uri starts with <tt>http:</tt> or <tt>https:</tt>
     */
    public static boolean isHttpUri(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith("http:") || uri.startsWith("https:");
    }

    /**
     * Appends the parameters to the given uri
     *
     * @param uri the uri
     * @param parameters the additional parameters (will clear the map)
     * @return a new uri with the additional parameters appended
     * @throws URISyntaxException is thrown if the uri is invalid
     */
    public static String appendParameters(String uri, Map<String, Object> parameters) throws URISyntaxException {
        // add additional parameters to the resource uri
        if (!parameters.isEmpty()) {
            String query = URISupport.createQueryString(parameters);
            URI u = new URI(uri);
            u = URISupport.createURIWithQuery(u, query);
            parameters.clear();
            return u.toString();
        } else {
            return uri;
        }
    }
}
