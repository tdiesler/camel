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
package org.apache.camel.impl.springboot;

import javax.annotation.Generated;
import org.apache.camel.spring.boot.DataFormatConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zip Deflate Compression data format is a message compression and
 * de-compression format (not zip files).
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.dataformat.zip")
public class ZipDataFormatConfiguration
        extends
            DataFormatConfigurationPropertiesCommon {

    /**
     * To specify a specific compression between 0-9. -1 is default compression
     * 0 is no compression and 9 is best compression.
     */
    private Integer compressionLevel = -1;
    /**
     * Whether the data format should set the Content-Type header with the type
     * from the data format if the data format is capable of doing so. For
     * example application/xml for data formats marshalling to XML or
     * application/json for data formats marshalling to JSon etc.
     */
    private Boolean contentTypeHeader = false;

    public Integer getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(Integer compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public Boolean getContentTypeHeader() {
        return contentTypeHeader;
    }

    public void setContentTypeHeader(Boolean contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader;
    }
}