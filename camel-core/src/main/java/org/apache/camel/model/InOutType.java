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
package org.apache.camel.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;

/**
 * Represents an XML &lt;inOut/&gt; element
 *
 * @version $Revision$
 */
@XmlRootElement(name = "inOut")
@XmlAccessorType(XmlAccessType.FIELD)
public class InOutType extends SendType<InOutType> {

    public InOutType() {
    }

    public InOutType(String uri) {
        setUri(uri);
    }

    public InOutType(Endpoint endpoint) {
        setEndpoint(endpoint);
    }

    @Override
    public String toString() {
        return "InOut[" + getLabel() + "]";
    }

    @Override
    public String getShortName() {
        return "inOut";
    }

    @Override
    public ExchangePattern getPattern() {
        return ExchangePattern.InOut;
    }
}