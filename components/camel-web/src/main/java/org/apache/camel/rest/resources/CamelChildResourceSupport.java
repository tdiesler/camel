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
package org.apache.camel.rest.resources;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import com.sun.jersey.api.view.ImplicitProduces;

/**
 * A useful base class for any sub resource of the root {@link org.apache.camel.rest.resources.CamelContextResource}
 *
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Constants.HTML_MIME_TYPES)
public class CamelChildResourceSupport {
    protected final CamelContext camelContext;
    protected final ProducerTemplate template;

    public CamelChildResourceSupport(CamelContextResource contextResource) {
        camelContext = contextResource.getCamelContext();
        template = contextResource.getTemplate();
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public ProducerTemplate getTemplate() {
        return template;
    }
}
