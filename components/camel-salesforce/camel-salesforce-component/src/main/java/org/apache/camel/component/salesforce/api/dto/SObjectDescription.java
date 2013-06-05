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
package org.apache.camel.component.salesforce.api.dto;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class SObjectDescription extends SObject {

    @XStreamImplicit
    private List<SObjectField> fields;

    private SObjectDescriptionUrls urls;

    @XStreamImplicit
    private List<ChildRelationShip> childRelationships;

    @XStreamImplicit
    private List<RecordTypeInfo> recordTypeInfos;

    public List<SObjectField> getFields() {
        return fields;
    }

    public void setFields(List<SObjectField> fields) {
        this.fields = fields;
    }

    public SObjectDescriptionUrls getUrls() {
        return urls;
    }

    public void setUrls(SObjectDescriptionUrls urls) {
        this.urls = urls;
    }

    public List<ChildRelationShip> getChildRelationships() {
        return childRelationships;
    }

    public void setChildRelationships(List<ChildRelationShip> childRelationships) {
        this.childRelationships = childRelationships;
    }

    public List<RecordTypeInfo> getRecordTypeInfos() {
        return recordTypeInfos;
    }

    public void setRecordTypeInfos(List<RecordTypeInfo> recordTypeInfos) {
        this.recordTypeInfos = recordTypeInfos;
    }
}
