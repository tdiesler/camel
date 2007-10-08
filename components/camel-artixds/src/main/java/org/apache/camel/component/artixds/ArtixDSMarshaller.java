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
package org.apache.camel.component.artixds;

import java.io.IOException;
import java.io.OutputStream;

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.presentation.Sink;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.spi.Marshaller;
import org.apache.camel.util.CamelContextHelper;

/**
 * @version $Revision: 1.1 $
 */
public class ArtixDSMarshaller implements Marshaller, CamelContextAware {
    private Sink sink;
    private CamelContext camelContext;

    public void marshal(Object graph, OutputStream stream) throws IOException {
        ComplexDataObject dataObject = CamelContextHelper.convertTo(camelContext, graph, ComplexDataObject.class);
        sink.setOutputStream(stream);
        sink.writeObject(dataObject);
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }
}
