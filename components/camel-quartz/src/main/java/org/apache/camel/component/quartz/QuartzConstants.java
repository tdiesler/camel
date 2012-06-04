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
package org.apache.camel.component.quartz;

/**
 * Quartz constants.
 */
public final class QuartzConstants {

    public static final String QUARTZ_ENDPOINT_URI = "CamelQuartzEndpoint";

    // Note: using the CamelContext management name to ensure its unique in the JVM
    public static final String QUARTZ_CAMEL_CONTEXT_NAME = "CamelQuartzCamelContextName";

    public static final String QUARTZ_CAMEL_CONTEXT = "CamelQuartzCamelContext";

    private QuartzConstants() {
        // Utility class
    }
}
