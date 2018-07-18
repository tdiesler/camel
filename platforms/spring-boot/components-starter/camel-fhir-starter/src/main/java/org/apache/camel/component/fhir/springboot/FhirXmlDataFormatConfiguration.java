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
package org.apache.camel.component.fhir.springboot;

import java.util.List;
import javax.annotation.Generated;
import org.apache.camel.spring.boot.DataFormatConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The FHIR XML data format is used to marshall/unmarshall from/to FHIR objects
 * to/from XML.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "camel.dataformat.fhirxml")
public class FhirXmlDataFormatConfiguration
        extends
            DataFormatConfigurationPropertiesCommon {

    /**
     * The version of FHIR to use. Possible values are:
     * DSTU2,DSTU2_HL7ORG,DSTU2_1,DSTU3,R4
     */
    private String fhirVersion = "DSTU3";
    /**
     * Sets the pretty print flag, meaning that the parser will encode resources
     * with human-readable spacing and newlines between elements instead of
     * condensing output as much as possible.
     */
    private Boolean prettyPrint = false;
    /**
     * Sets the server's base URL used by this parser. If a value is set,
     * resource references will be turned into relative references if they are
     * provided as absolute URLs but have a base matching the given base.
     */
    private String serverBaseUrl;
    /**
     * If set to true (default is false) the ID of any resources being encoded
     * will not be included in the output. Note that this does not apply to
     * contained resources, only to root resources. In other words, if this is
     * set to true, contained resources will still have local IDs but the
     * outer/containing ID will not have an ID.
     */
    private Boolean omitResourceId = false;
    /**
     * If provided, tells the parse which resource types to apply link
     * setEncodeElements(Set) encode elements to. Any resource types not
     * specified here will be encoded completely, with no elements excluded. The
     * option is a java.util.Set<java.lang.String> type.
     */
    private String encodeElementsAppliesToResourceTypes;
    /**
     * If set to true (default is false), the values supplied to link
     * setEncodeElements(Set) will not be applied to the root resource
     * (typically a Bundle), but will be applied to any sub-resources contained
     * within it (i.e. search result resources in that bundle)
     */
    private Boolean encodeElementsAppliesToChildResourcesOnly = false;
    /**
     * If provided, specifies the elements which should be encoded, to the
     * exclusion of all others. Valid values for this field would include:
     * Patient - Encode patient and all its children Patient.name - Encode only
     * the patient's name Patient.name.family - Encode only the patient's family
     * name .text - Encode the text element on any resource (only the very first
     * position may contain a wildcard) .(mandatory) - This is a special case
     * which causes any mandatory fields (min 0) to be encoded. The option is a
     * java.util.Set<java.lang.String> type.
     */
    private String encodeElements;
    /**
     * If provided, specifies the elements which should NOT be encoded. Valid
     * values for this field would include: Patient - Don't encode patient and
     * all its children Patient.name - Don't encode the patient's name
     * Patient.name.family - Don't encode the patient's family name .text -
     * Don't encode the text element on any resource (only the very first
     * position may contain a wildcard) DSTU2 note: Note that values including
     * meta, such as Patient.meta will work for DSTU2 parsers, but values with
     * subelements on meta such as Patient.meta.lastUpdated will only work in
     * DSTU3 mode. The option is a java.util.Set<java.lang.String> type.
     */
    private String dontEncodeElements;
    /**
     * If set to true (which is the default), resource references containing a
     * version will have the version removed when the resource is encoded. This
     * is generally good behaviour because in most situations, references from
     * one resource to another should be to the resource by ID, not by ID and
     * version. In some cases though, it may be desirable to preserve the
     * version in resource links. In that case, this value should be set to
     * false. This method provides the ability to globally disable reference
     * encoding. If finer-grained control is needed, use link
     * setDontStripVersionsFromReferencesAtPaths(List)
     */
    private Boolean stripVersionsFromReferences = false;
    /**
     * If set to true (which is the default), the Bundle.entry.fullUrl will
     * override the Bundle.entry.resource's resource id if the fullUrl is
     * defined. This behavior happens when parsing the source data into a Bundle
     * object. Set this to false if this is not the desired behavior (e.g. the
     * client code wishes to perform additional validation checks between the
     * fullUrl and the resource id).
     */
    private Boolean overrideResourceIdWithBundleEntryFullUrl = false;
    /**
     * If set to true (default is false) only elements marked by the FHIR
     * specification as being summary elements will be included.
     */
    private Boolean summaryMode = false;
    /**
     * If set to true (default is false), narratives will not be included in the
     * encoded values.
     */
    private Boolean suppressNarratives = false;
    /**
     * If supplied value(s), any resource references at the specified paths will
     * have their resource versions encoded instead of being automatically
     * stripped during the encoding process. This setting has no effect on the
     * parsing process. This method provides a finer-grained level of control
     * than link setStripVersionsFromReferences(Boolean) and any paths specified
     * by this method will be encoded even if link
     * setStripVersionsFromReferences(Boolean) has been set to true (which is
     * the default)
     */
    private List<String> dontStripVersionsFromReferencesAtPaths;
    /**
     * Whether the data format should set the Content-Type header with the type
     * from the data format if the data format is capable of doing so. For
     * example application/xml for data formats marshalling to XML, or
     * application/json for data formats marshalling to JSon etc.
     */
    private Boolean contentTypeHeader = false;

    public String getFhirVersion() {
        return fhirVersion;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }

    public Boolean getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(Boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public Boolean getOmitResourceId() {
        return omitResourceId;
    }

    public void setOmitResourceId(Boolean omitResourceId) {
        this.omitResourceId = omitResourceId;
    }

    public String getEncodeElementsAppliesToResourceTypes() {
        return encodeElementsAppliesToResourceTypes;
    }

    public void setEncodeElementsAppliesToResourceTypes(
            String encodeElementsAppliesToResourceTypes) {
        this.encodeElementsAppliesToResourceTypes = encodeElementsAppliesToResourceTypes;
    }

    public Boolean getEncodeElementsAppliesToChildResourcesOnly() {
        return encodeElementsAppliesToChildResourcesOnly;
    }

    public void setEncodeElementsAppliesToChildResourcesOnly(
            Boolean encodeElementsAppliesToChildResourcesOnly) {
        this.encodeElementsAppliesToChildResourcesOnly = encodeElementsAppliesToChildResourcesOnly;
    }

    public String getEncodeElements() {
        return encodeElements;
    }

    public void setEncodeElements(String encodeElements) {
        this.encodeElements = encodeElements;
    }

    public String getDontEncodeElements() {
        return dontEncodeElements;
    }

    public void setDontEncodeElements(String dontEncodeElements) {
        this.dontEncodeElements = dontEncodeElements;
    }

    public Boolean getStripVersionsFromReferences() {
        return stripVersionsFromReferences;
    }

    public void setStripVersionsFromReferences(
            Boolean stripVersionsFromReferences) {
        this.stripVersionsFromReferences = stripVersionsFromReferences;
    }

    public Boolean getOverrideResourceIdWithBundleEntryFullUrl() {
        return overrideResourceIdWithBundleEntryFullUrl;
    }

    public void setOverrideResourceIdWithBundleEntryFullUrl(
            Boolean overrideResourceIdWithBundleEntryFullUrl) {
        this.overrideResourceIdWithBundleEntryFullUrl = overrideResourceIdWithBundleEntryFullUrl;
    }

    public Boolean getSummaryMode() {
        return summaryMode;
    }

    public void setSummaryMode(Boolean summaryMode) {
        this.summaryMode = summaryMode;
    }

    public Boolean getSuppressNarratives() {
        return suppressNarratives;
    }

    public void setSuppressNarratives(Boolean suppressNarratives) {
        this.suppressNarratives = suppressNarratives;
    }

    public List<String> getDontStripVersionsFromReferencesAtPaths() {
        return dontStripVersionsFromReferencesAtPaths;
    }

    public void setDontStripVersionsFromReferencesAtPaths(
            List<String> dontStripVersionsFromReferencesAtPaths) {
        this.dontStripVersionsFromReferencesAtPaths = dontStripVersionsFromReferencesAtPaths;
    }

    public Boolean getContentTypeHeader() {
        return contentTypeHeader;
    }

    public void setContentTypeHeader(Boolean contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader;
    }
}