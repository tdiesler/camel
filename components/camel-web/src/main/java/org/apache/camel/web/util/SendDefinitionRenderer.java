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

import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.SendDefinition;
import org.apache.camel.model.WireTapDefinition;

/**
 *
 */
public class SendDefinitionRenderer {

    public static void render(StringBuilder buffer, ProcessorDefinition processor) {
        buffer.append(".");
        SendDefinition send = (SendDefinition)processor;
        if (send instanceof WireTapDefinition || send.getPattern() == null) {
            // for wireTap and simple to
            buffer.append(send.getShortName());
        } else {
            // for inOnly and inOut
            if (send.getPattern().name().equals("InOnly")) {
                buffer.append("inOnly");
            } else if (send.getPattern().name().equals("InOut")) {
                buffer.append("inOut");
            }
        }
        buffer.append("(\"").append(send.getUri()).append("\")");
    }
}
