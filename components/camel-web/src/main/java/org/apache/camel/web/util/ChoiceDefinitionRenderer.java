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
package org.apache.camel.web.util;

import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.OtherwiseDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.WhenDefinition;

public final class ChoiceDefinitionRenderer {

    private ChoiceDefinitionRenderer() {
        // Utility class, no public or protected default constructor
    }    

    public static void render(StringBuilder buffer, ProcessorDefinition processor) {
        ChoiceDefinition choice = (ChoiceDefinition)processor;
        buffer.append(".").append(choice.getShortName()).append("()");
        for (WhenDefinition when : choice.getWhenClauses()) {
            ProcessorDefinitionRenderer.render(buffer, when);
        }
        OtherwiseDefinition other = choice.getOtherwise();
        if (other != null) {
            ProcessorDefinitionRenderer.render(buffer, other);
        }
        buffer.append(".end()");
    }
}
