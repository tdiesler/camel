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
package org.apache.camel.component.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.util.ObjectHelper;

/**
 * Default {@link org.apache.camel.component.properties.PropertiesResolver} which can resolve properties
 * from file and classpath.
 * <p/>
 * You can denote <tt>classpath:</tt> or <tt>file:</tt> as prefix in the uri to select whether the file
 * is located in the classpath or on the file system.
 *
 * @version $Revision$
 */
public class DefaultPropertiesResolver implements PropertiesResolver {

    public Properties resolveProperties(CamelContext context, String... uri) throws Exception {
        Properties answer = new Properties();

        for (String path : uri) {
            if (path.startsWith("file:")) {
                Properties prop = loadPropertiesFromFilePath(context, path);
                answer.putAll(prop);
            } else {
                // default to classpath
                Properties prop = loadPropertiesFromClasspath(context, path);
                answer.putAll(prop);
            }
        }

        return answer;
    }

    protected Properties loadPropertiesFromClasspath(CamelContext context, String path) throws IOException {
        if (path.startsWith("classpath:")) {
            path = ObjectHelper.after(path, "classpath:");
        }
        InputStream is = context.getClassResolver().loadResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Properties file " + path + " not found in classpath");
        }
        Properties answer = new Properties();
        answer.load(is);
        return answer;
    }

    protected Properties loadPropertiesFromFilePath(CamelContext context, String path) throws IOException {
        if (path.startsWith("file:")) {
            path = ObjectHelper.after(path, "file:");
        }
        InputStream is = new FileInputStream(path);
        Properties answer = new Properties();
        answer.load(is);
        return answer;
    }

}
