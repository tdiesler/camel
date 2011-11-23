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
package org.apache.camel.converter.stream;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.StreamCache;
import org.apache.camel.util.IOHelper;

public class FileInputStreamCache extends InputStream implements StreamCache, Closeable {
    private InputStream stream;
    private File file;

    public FileInputStreamCache(File file) throws FileNotFoundException {
        this.file = file;
        this.stream = new BufferedInputStream(new FileInputStream(file));
    }
    
    @Override
    public void close() {
        if (stream != null) {
            IOHelper.close(stream);
        }
    }

    @Override
    public void reset() {
        try {
            // reset by closing and creating a new stream based on the file
            close();
            // reset by creating a new stream based on the file
            stream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeCamelException("Cannot reset stream from file " + file, e);
        }            
    }

    public void writeTo(OutputStream os) throws IOException {
        IOHelper.copy(getInputStream(), os);
    }

    @Override
    public int available() throws IOException {
        return getInputStream().available();
    }

    @Override
    public int read() throws IOException {
        return getInputStream().read();
    }

    protected InputStream getInputStream() {
        return stream;
    }
}
