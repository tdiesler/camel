/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mina;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

import org.apache.camel.Converter;
import org.apache.camel.converter.IOConverter;
import org.apache.mina.common.ByteBuffer;

/**
 * A set of converter methods for working with MINA types
 *
 * @version $Revision: 1.1 $
 */
@Converter
public class MinaConverter {
    @Converter
    public static byte[] toByteArray(ByteBuffer buffer) {
        // TODO is there a neater way?
        byte[] answer = new byte[buffer.remaining()];
        buffer.get(answer);
        return answer;
    }

    @Converter
    public static String toString(ByteBuffer buffer) {
        return IOConverter.toString(toByteArray(buffer));
    }

    @Converter
    public static InputStream toInputStream(ByteBuffer buffer) {
        return buffer.asInputStream();
    }

    @Converter
    public static ObjectInput toObjectInput(ByteBuffer buffer) throws IOException {
        return IOConverter.toObjectInput(toInputStream(buffer));
    }
}
