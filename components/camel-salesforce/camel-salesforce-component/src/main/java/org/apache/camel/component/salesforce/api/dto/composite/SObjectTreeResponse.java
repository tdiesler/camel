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
package org.apache.camel.component.salesforce.api.dto.composite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import org.apache.camel.component.salesforce.api.dto.RestError;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Response from the SObject tree Composite API invocation.
 */
@XStreamAlias("Result") // you might be wondering why `Result` and not `SObjectTreeResponse as in documentation, well,
                        // the difference between documentation and practice is usually found in practice
public final class SObjectTreeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean hasErrors;

    @XStreamImplicit
    private final List<ReferenceId> results;

    @JsonCreator
    public SObjectTreeResponse(@JsonProperty("hasErrors") final boolean hasErrors,
            @JsonProperty("results") final List<ReferenceId> results) {
        this.hasErrors = hasErrors;
        if (results == null) {
            this.results = Collections.emptyList();
        } else {
            this.results = results;
        }
    }

    public List<RestError> getAllErrors() {
        final List<RestError> allErrors = new ArrayList<>();

        for (final ReferenceId result : results) {
            allErrors.addAll(result.getErrors());
        }

        return Collections.unmodifiableList(allErrors);
    }

    public List<ReferenceId> getResults() {
        return results;
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
