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
/*
 * Camel Api Route test generated by camel-component-util-maven-plugin
 * Generated on: Wed Jul 09 19:57:10 PDT 2014
 */
package org.apache.camel.component.linkedin;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.linkedin.internal.CompaniesResourceApiMethod;
import org.apache.camel.component.linkedin.internal.LinkedInApiCollection;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for {@link org.apache.camel.component.linkedin.api.CompaniesResource} APIs.
 */
public class CompaniesResourceIntegrationTest extends AbstractLinkedInTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CompaniesResourceIntegrationTest.class);
    private static final String PATH_PREFIX = LinkedInApiCollection.getCollection().getApiName(CompaniesResourceApiMethod.class).getName();
    private static final Long TEST_COMPANY_ID = 1337L;

    // TODO provide parameter values for addCompanyUpdateCommentAsCompany
    @Ignore
    @Test
    public void testAddCompanyUpdateCommentAsCompany() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is String
        headers.put("CamelLinkedIn.update_key", null);
        // parameter type is org.apache.camel.component.linkedin.api.model.UpdateComment
        headers.put("CamelLinkedIn.updatecomment", null);

        requestBodyAndHeaders("direct://ADDCOMPANYUPDATECOMMENTASCOMPANY", null, headers);
    }

    // TODO provide parameter values for addShare
    @Ignore
    @Test
    public void testAddShare() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is org.apache.camel.component.linkedin.api.model.Share
        headers.put("CamelLinkedIn.share", null);

        requestBodyAndHeaders("direct://ADDSHARE", null, headers);
    }

    @Test
    public void testGetCompanies() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // use defaults
        // parameter type is String
//        headers.put("CamelLinkedIn.fields", null);
        // parameter type is String
        headers.put("CamelLinkedIn.email_domain", "linkedin.com");
        // parameter type is Boolean
//        headers.put("CamelLinkedIn.is_company_admin", null);

        final org.apache.camel.component.linkedin.api.model.Companies result = requestBodyAndHeaders("direct://GETCOMPANIES", null, headers);

        assertNotNull("getCompanies result", result);
        LOG.debug("getCompanies: " + result);
    }

    @Test
    public void testGetCompanyById() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", TEST_COMPANY_ID);
        // use default value
/*
        // parameter type is String
        headers.put("CamelLinkedIn.fields", null);
*/

        final org.apache.camel.component.linkedin.api.model.Company result = requestBodyAndHeaders("direct://GETCOMPANYBYID", null, headers);

        assertNotNull("getCompanyById result", result);
        LOG.debug("getCompanyById: " + result);
    }

    @Test
    public void testGetCompanyByName() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is String
        headers.put("CamelLinkedIn.universal_name", "linkedin");
        // use default fields
/*
        // parameter type is String
        headers.put("CamelLinkedIn.fields", null);
*/

        final org.apache.camel.component.linkedin.api.model.Company result = requestBodyAndHeaders("direct://GETCOMPANYBYNAME", null, headers);

        assertNotNull("getCompanyByName result", result);
        LOG.debug("getCompanyByName: " + result);
    }

    // TODO provide parameter values for getCompanyUpdateComments
    @Ignore
    @Test
    public void testGetCompanyUpdateComments() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is String
        headers.put("CamelLinkedIn.update_key", null);
        // parameter type is String
        headers.put("CamelLinkedIn.fields", null);
        // parameter type is Boolean
        headers.put("CamelLinkedIn.secure_urls", null);

        final org.apache.camel.component.linkedin.api.model.UpdateComments result = requestBodyAndHeaders("direct://GETCOMPANYUPDATECOMMENTS", null, headers);

        assertNotNull("getCompanyUpdateComments result", result);
        LOG.debug("getCompanyUpdateComments: " + result);
    }

    // TODO provide parameter values for getCompanyUpdateLikes
    @Ignore
    @Test
    public void testGetCompanyUpdateLikes() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is String
        headers.put("CamelLinkedIn.update_key", null);
        // parameter type is String
        headers.put("CamelLinkedIn.fields", null);
        // parameter type is Boolean
        headers.put("CamelLinkedIn.secure_urls", null);

        final org.apache.camel.component.linkedin.api.model.Likes result = requestBodyAndHeaders("direct://GETCOMPANYUPDATELIKES", null, headers);

        assertNotNull("getCompanyUpdateLikes result", result);
        LOG.debug("getCompanyUpdateLikes: " + result);
    }

    @Test
    public void testGetCompanyUpdates() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", TEST_COMPANY_ID);
        // use defaults
/*
        // parameter type is String
        headers.put("CamelLinkedIn.fields", null);
        // parameter type is org.apache.camel.component.linkedin.api.Eventtype
        headers.put("CamelLinkedIn.event_type", null);
        // parameter type is Long
        headers.put("CamelLinkedIn.start", null);
        // parameter type is Long
        headers.put("CamelLinkedIn.count", null);
*/

        final org.apache.camel.component.linkedin.api.model.Updates result = requestBodyAndHeaders("direct://GETCOMPANYUPDATES", null, headers);

        assertNotNull("getCompanyUpdates result", result);
        LOG.debug("getCompanyUpdates: " + result);
    }

    // TODO provide parameter values for getHistoricalFollowStatistics
    @Ignore
    @Test
    public void testGetHistoricalFollowStatistics() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is Long
        headers.put("CamelLinkedIn.start_timestamp", null);
        // parameter type is Long
        headers.put("CamelLinkedIn.end_timestamp", null);
        // parameter type is org.apache.camel.component.linkedin.api.Timegranularity
        headers.put("CamelLinkedIn.time_granularity", null);

        final org.apache.camel.component.linkedin.api.model.HistoricalFollowStatistics result = requestBodyAndHeaders("direct://GETHISTORICALFOLLOWSTATISTICS", null, headers);

        assertNotNull("getHistoricalFollowStatistics result", result);
        LOG.debug("getHistoricalFollowStatistics: " + result);
    }

    // TODO provide parameter values for getHistoricalStatusUpdateStatistics
    @Ignore
    @Test
    public void testGetHistoricalStatusUpdateStatistics() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", TEST_COMPANY_ID);
        // parameter type is Long
        headers.put("CamelLinkedIn.start_timestamp", null);
        // parameter type is Long
        headers.put("CamelLinkedIn.end_timestamp", null);
        // parameter type is org.apache.camel.component.linkedin.api.Timegranularity
        headers.put("CamelLinkedIn.time_granularity", null);
        // parameter type is String
        headers.put("CamelLinkedIn.statistics_update_key", null);

        final org.apache.camel.component.linkedin.api.model.HistoricalStatusUpdateStatistics result = requestBodyAndHeaders("direct://GETHISTORICALSTATUSUPDATESTATISTICS", null, headers);

        assertNotNull("getHistoricalStatusUpdateStatistics result", result);
        LOG.debug("getHistoricalStatusUpdateStatistics: " + result);
    }

    @Test
    public void testGetNumberOfFollowers() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", TEST_COMPANY_ID);
        // parameter type is java.util.List
        headers.put("CamelLinkedIn.geos", null);
        // parameter type is java.util.List
        headers.put("CamelLinkedIn.companySizes", null);
        // parameter type is java.util.List
        headers.put("CamelLinkedIn.jobFunc", null);
        // parameter type is java.util.List
        headers.put("CamelLinkedIn.industries", null);
        // parameter type is java.util.List
        headers.put("CamelLinkedIn.seniorities", null);

        final org.apache.camel.component.linkedin.api.model.NumFollowers result = requestBodyAndHeaders("direct://GETNUMBEROFFOLLOWERS", null, headers);

        assertNotNull("getNumberOfFollowers result", result);
        LOG.debug("getNumberOfFollowers: " + result);
    }

    // TODO provide parameter values for getStatistics
    @Ignore
    @Test
    public void testGetStatistics() throws Exception {
        // using long message body for single parameter "company_id"
        final org.apache.camel.component.linkedin.api.model.CompanyStatistics result = requestBody("direct://GETSTATISTICS", 0L);

        assertNotNull("getStatistics result", result);
        LOG.debug("getStatistics: " + result);
    }

    @Test
    public void testIsShareEnabled() throws Exception {
        // using long message body for single parameter "company_id"
        final org.apache.camel.component.linkedin.api.model.IsCompanyShareEnabled result = requestBody("direct://ISSHAREENABLED", TEST_COMPANY_ID);

        assertNotNull("isShareEnabled result", result);
        LOG.debug("isShareEnabled: " + result);
    }

    @Test
    public void testIsViewerShareEnabled() throws Exception {
        // using long message body for single parameter "company_id"
        final org.apache.camel.component.linkedin.api.model.IsCompanyShareEnabled result = requestBody("direct://ISVIEWERSHAREENABLED", TEST_COMPANY_ID);

        assertNotNull("isViewerShareEnabled result", result);
        LOG.debug("isViewerShareEnabled: " + result);
    }

    // TODO provide parameter values for likeCompanyUpdate
    @Ignore
    @Test
    public void testLikeCompanyUpdate() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("CamelLinkedIn.company_id", 0L);
        // parameter type is String
        headers.put("CamelLinkedIn.update_key", null);
        // parameter type is org.apache.camel.component.linkedin.api.model.IsLiked
        headers.put("CamelLinkedIn.isliked", null);

        requestBodyAndHeaders("direct://LIKECOMPANYUPDATE", null, headers);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                // test route for addCompanyUpdateCommentAsCompany
                from("direct://ADDCOMPANYUPDATECOMMENTASCOMPANY")
                    .to("linkedin://" + PATH_PREFIX + "/addCompanyUpdateCommentAsCompany");

                // test route for addShare
                from("direct://ADDSHARE")
                    .to("linkedin://" + PATH_PREFIX + "/addShare");

                // test route for getCompanies
                from("direct://GETCOMPANIES")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanies");

                // test route for getCompanyById
                from("direct://GETCOMPANYBYID")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanyById");

                // test route for getCompanyByName
                from("direct://GETCOMPANYBYNAME")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanyByName");

                // test route for getCompanyUpdateComments
                from("direct://GETCOMPANYUPDATECOMMENTS")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanyUpdateComments");

                // test route for getCompanyUpdateLikes
                from("direct://GETCOMPANYUPDATELIKES")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanyUpdateLikes");

                // test route for getCompanyUpdates
                from("direct://GETCOMPANYUPDATES")
                    .to("linkedin://" + PATH_PREFIX + "/getCompanyUpdates");

                // test route for getHistoricalFollowStatistics
                from("direct://GETHISTORICALFOLLOWSTATISTICS")
                    .to("linkedin://" + PATH_PREFIX + "/getHistoricalFollowStatistics");

                // test route for getHistoricalStatusUpdateStatistics
                from("direct://GETHISTORICALSTATUSUPDATESTATISTICS")
                    .to("linkedin://" + PATH_PREFIX + "/getHistoricalStatusUpdateStatistics");

                // test route for getNumberOfFollowers
                from("direct://GETNUMBEROFFOLLOWERS")
                    .to("linkedin://" + PATH_PREFIX + "/getNumberOfFollowers");

                // test route for getStatistics
                from("direct://GETSTATISTICS")
                    .to("linkedin://" + PATH_PREFIX + "/getStatistics?inBody=company_id");

                // test route for isShareEnabled
                from("direct://ISSHAREENABLED")
                    .to("linkedin://" + PATH_PREFIX + "/isShareEnabled?inBody=company_id");

                // test route for isViewerShareEnabled
                from("direct://ISVIEWERSHAREENABLED")
                    .to("linkedin://" + PATH_PREFIX + "/isViewerShareEnabled?inBody=company_id");

                // test route for likeCompanyUpdate
                from("direct://LIKECOMPANYUPDATE")
                    .to("linkedin://" + PATH_PREFIX + "/likeCompanyUpdate");

            }
        };
    }
}
