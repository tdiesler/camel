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
package org.apache.camel.component.salesforce.api.dto.analytics.reports;

import org.apache.camel.component.salesforce.api.dto.AbstractDTOBase;
import org.codehaus.jackson.annotate.JsonAnySetter;

/**
 * Report results summary value DTO for data cells and aggregates.
 */
public class SummaryValue extends AbstractDTOBase {

    public static final String VALUE_FIELD = "value";

    private String label;

    private Object value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Helper method for JSON un-marshaling.
     * @param name field name, MUST be "value"
     * @param value field value
     * @throws java.lang.IllegalArgumentException if field name is not "value"
     */
    @JsonAnySetter
    public void setAny(String name, Object value) throws IllegalArgumentException {
        if (!VALUE_FIELD.equals(name)) {
            throw new IllegalArgumentException(name);
        }
        this.value = value;
    }
}
