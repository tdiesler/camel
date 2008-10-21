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
package org.apache.camel.artix.ds;

import biz.c24.io.api.data.saxon.DocumentNode;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.transform.Transform;
import org.apache.camel.Converter;
import net.sf.saxon.Configuration;

/**
 * Helper converters for <a href="http://activemq.apache.org/camel/artix-data-services.html">Artix Data Services</a>
 * 
 * @version $Revision$
 */
@Converter
public class ArtixDSConverter {


    /**
     * A converter to provide a Processor for invoking the given ADS
     * transformation class
     *
     * @param transformer
     * @return a Processor capable of performing the transformation on a Message Exchange
     */
    @Converter
    public static ArtixDSTransform toProcessor(Transform transformer) {
        return new ArtixDSTransform(transformer);
    }

    /**
     * Converts a data object into a Saxon document info so that it can be used in Saxon's
     * XQuery processor
     *
     * @param dataObject
     */
    @Converter
    public static DocumentNode toDocumentNode(ComplexDataObject dataObject) {
        return toDocumentNode(new Configuration(), dataObject);
    }

    /**
     * Converts a data object into a Saxon document info so that it can be used in Saxon's
     * XQuery processor
     *
     * @param config
     * @param dataObject
     */
    public static DocumentNode toDocumentNode(Configuration config, ComplexDataObject dataObject) {
        return new DocumentNode(config, dataObject, true, true);
    }
}
