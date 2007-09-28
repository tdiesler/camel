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

import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ValidationException;
import biz.c24.io.api.transform.Transform;
import org.apache.camel.Exchange;
import org.apache.camel.util.ObjectHelper;

/**
 * Transforms an Artix data object into some output format
 *
 * @version $Revision: 1.1 $
 */
public class ArtixTransform extends ArtixSink {
    private Transform transform;

    public static ArtixTransform transform(Class<?> transformType) {
        Transform transformer = (Transform) ObjectHelper.newInstance(transformType);
        return transform(transformer);
    }

    public static ArtixTransform transform(Transform transformer) {
        return new ArtixTransform(transformer);
    }

    public ArtixTransform(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected ComplexDataObject transformDataObject(Exchange exchange, ComplexDataObject dataObject) throws ValidationException {
        Transform transformer = getTransform();
        ComplexDataObject[][] objects = {{ dataObject }};
        ComplexDataObject[][] answer = transformer.transform(objects);
        return answer[0][0];
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

}