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
package org.apache.camel.dataformat.beanio.springboot;

import javax.annotation.Generated;
import org.apache.camel.spring.boot.DataFormatConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Camel BeanIO data format support
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.dataformat.beanio")
public class BeanIODataFormatConfiguration
        extends
            DataFormatConfigurationPropertiesCommon {

    /**
     * The BeanIO mapping file. Is by default loaded from the classpath. You can
     * prefix with file: http: or classpath: to denote from where to load the
     * mapping file.
     */
    private String mapping;
    /**
     * The name of the stream to use.
     */
    private String streamName;
    /**
     * Whether to ignore unidentified records.
     */
    private Boolean ignoreUnidentifiedRecords = false;
    /**
     * Whether to ignore unexpected records.
     */
    private Boolean ignoreUnexpectedRecords = false;
    /**
     * Whether to ignore invalid records.
     */
    private Boolean ignoreInvalidRecords = false;
    /**
     * The charset to use. Is by default the JVM platform default charset.
     */
    private String encoding;
    /**
     * To use a custom org.apache.camel.dataformat.beanio.BeanIOErrorHandler as
     * error handler while parsing. Configure the fully qualified class name of
     * the error handler. Notice the options ignoreUnidentifiedRecords
     * ignoreUnexpectedRecords and ignoreInvalidRecords may not be in use when
     * you use a custom error handler.
     */
    private String beanReaderErrorHandlerType;
    /**
     * This options controls whether to unmarshal as a list of objects or as a
     * single object only. The former is the default mode and the latter is only
     * intended in special use-cases where beanio maps the Camel message to a
     * single POJO bean.
     */
    private Boolean unmarshalSingleObject = false;
    /**
     * Whether the data format should set the Content-Type header with the type
     * from the data format if the data format is capable of doing so. For
     * example application/xml for data formats marshalling to XML or
     * application/json for data formats marshalling to JSon etc.
     */
    private Boolean contentTypeHeader = false;

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public Boolean getIgnoreUnidentifiedRecords() {
        return ignoreUnidentifiedRecords;
    }

    public void setIgnoreUnidentifiedRecords(Boolean ignoreUnidentifiedRecords) {
        this.ignoreUnidentifiedRecords = ignoreUnidentifiedRecords;
    }

    public Boolean getIgnoreUnexpectedRecords() {
        return ignoreUnexpectedRecords;
    }

    public void setIgnoreUnexpectedRecords(Boolean ignoreUnexpectedRecords) {
        this.ignoreUnexpectedRecords = ignoreUnexpectedRecords;
    }

    public Boolean getIgnoreInvalidRecords() {
        return ignoreInvalidRecords;
    }

    public void setIgnoreInvalidRecords(Boolean ignoreInvalidRecords) {
        this.ignoreInvalidRecords = ignoreInvalidRecords;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getBeanReaderErrorHandlerType() {
        return beanReaderErrorHandlerType;
    }

    public void setBeanReaderErrorHandlerType(String beanReaderErrorHandlerType) {
        this.beanReaderErrorHandlerType = beanReaderErrorHandlerType;
    }

    public Boolean getUnmarshalSingleObject() {
        return unmarshalSingleObject;
    }

    public void setUnmarshalSingleObject(Boolean unmarshalSingleObject) {
        this.unmarshalSingleObject = unmarshalSingleObject;
    }

    public Boolean getContentTypeHeader() {
        return contentTypeHeader;
    }

    public void setContentTypeHeader(Boolean contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader;
    }
}