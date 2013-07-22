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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.camel.StreamCache;
import org.apache.camel.util.IOHelper;

/**
 * A {@link StreamCache} for {@link java.io.ByteArrayInputStream}
 */
public class ByteArrayInputStreamCache extends FilterInputStream implements StreamCache {

    private final int length;

    public ByteArrayInputStreamCache(ByteArrayInputStream in) {
        super(in);
        this.length = in.available();
    }

    public void reset() {
        try {
            super.reset();
        } catch (IOException e) {
            // ignore
        }
    }


    public void writeTo(OutputStream os) throws IOException {
        IOHelper.copyAndCloseInput(in, os);
    }

    public boolean inMemory() {
        return true;
    }

    @Override
    public long length() {
        return length;
    }
}
