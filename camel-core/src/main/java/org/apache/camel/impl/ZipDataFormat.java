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
package org.apache.camel.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.converter.stream.OutputStreamBuilder;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;

/**
 * "Deflate" compression data format.
 * See {@link org.apache.camel.model.dataformat.ZipFileDataFormat} for Zip file compression.
 */
public class ZipDataFormat implements DataFormat {

    private int compressionLevel;

    public ZipDataFormat() {
        this.compressionLevel = Deflater.BEST_SPEED;
    }

    public ZipDataFormat(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public void marshal(final Exchange exchange, final Object graph, final OutputStream stream) throws Exception {
        // ask for a mandatory type conversion to avoid a possible NPE beforehand as we do copy from the InputStream
        InputStream is = exchange.getContext().getTypeConverter().mandatoryConvertTo(InputStream.class, exchange, graph);

        DeflaterOutputStream zipOutput = new DeflaterOutputStream(stream, new Deflater(compressionLevel));
        try {
            IOHelper.copy(is, zipOutput);
        } finally {
            IOHelper.close(is, zipOutput);
        }
    }

    public Object unmarshal(final Exchange exchange, final InputStream inputStream) throws Exception {
        InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
        OutputStreamBuilder osb = OutputStreamBuilder.withExchange(exchange);

        try {
            IOHelper.copy(inflaterInputStream, osb);
            return osb.build();
        } finally {
            // must close input streams
            IOHelper.close(osb, inflaterInputStream, inputStream);
        }
    }

}
