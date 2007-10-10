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
package org.apache.camel.ads.converter;

import biz.c24.io.api.transform.Transform;
import org.apache.camel.Converter;
import org.apache.camel.Processor;
import org.apache.camel.component.artixds.ArtixTransform;

/**
 * Helper converters for <a href="http://www.iona.com/products/artix/data_services.htm">Artix Data Services</a>
 * 
 * @version $Revision: 1.1 $
 */
@Converter
public class AdsConverter {
    /**
     * A converter to provide a Processor for invoking the given ADS
     * transformation class
     *
     * @param transformer
     * @return a Processor capable of performing the transformation on a Message Exchange
     */
    @Converter
    public static ArtixTransform toProcessor(Transform transformer) {
        return new ArtixTransform(transformer);
    }
}
