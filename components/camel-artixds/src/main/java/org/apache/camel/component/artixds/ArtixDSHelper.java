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

import java.lang.reflect.InvocationTargetException;

import biz.c24.io.api.data.Element;
import org.apache.camel.RuntimeCamelException;

/**
 * @version $Revision: 1.1 $
 */
public class ArtixDSHelper {
    public static Element getElement(String modelClassName) {
        try {
            Class<Element> elementType = (Class<Element>) Class.forName(modelClassName);
            return getElement(elementType);
        }
        catch (RuntimeCamelException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }

    public static Element getElement(Class<?> elementType) {
        try {
            return (Element) elementType.getMethod("getInstance", null).invoke(null, null);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeCamelException(e.getTargetException());
        }
        catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
    }
}
