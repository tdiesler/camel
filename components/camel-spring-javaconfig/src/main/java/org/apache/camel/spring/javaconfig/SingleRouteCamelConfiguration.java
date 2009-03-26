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
package org.apache.camel.spring.javaconfig;

import java.util.Collections;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.config.java.annotation.Bean;

/**
 * A useful base class for writing
 * <a href="http://www.springsource.org/javaconfig">Spring JavaConfig</a>
 * configurations to configure a CamelContext with a single {@link RouteBuilder} instance

 * @version $Revision$
 */
public abstract class SingleRouteCamelConfiguration extends CamelConfiguration {

    @Bean
    public List<RouteBuilder> routes() {
        return Collections.singletonList(route());
    }

    /**
     * Creates the single {@link RouteBuilder} to use in this configuration
     */
    public abstract RouteBuilder route();
}
