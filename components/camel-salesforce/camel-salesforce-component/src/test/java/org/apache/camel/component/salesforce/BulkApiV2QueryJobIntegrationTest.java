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
package org.apache.camel.component.salesforce;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.dto.bulk.BatchInfo;
import org.apache.camel.component.salesforce.api.dto.bulk.ContentType;
import org.apache.camel.component.salesforce.api.dto.bulk.JobInfo;
import org.apache.camel.component.salesforce.api.dto.bulkv2.JobStateEnum;
import org.apache.camel.component.salesforce.api.dto.bulkv2.OperationEnum;
import org.apache.camel.component.salesforce.api.dto.bulkv2.QueryJob;
import org.apache.camel.component.salesforce.api.dto.bulkv2.QueryJobs;
import org.apache.camel.component.salesforce.dto.generated.Account;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.apache.camel.component.salesforce.api.dto.bulkv2.JobStateEnum.CLOSED;
import static org.apache.camel.component.salesforce.api.dto.bulkv2.JobTypeEnum.CLASSIC;
import static org.apache.camel.component.salesforce.api.dto.bulkv2.JobTypeEnum.V2Query;


@SuppressWarnings("BusyWait")
public class BulkApiV2QueryJobIntegrationTest extends AbstractSalesforceTestBase {

    @Test
    public void testQueryLifecycle() throws Exception {
        QueryJob job = new QueryJob();
        job.setOperation(OperationEnum.QUERY);
        job.setQuery("SELECT Id, LastName FROM Contact");

        job = template().requestBody("salesforce:bulk2CreateQueryJob", job, QueryJob.class);
        assertNotNull(job.getId(), "JobId");

        job = template().requestBodyAndHeader("salesforce:bulk2GetQueryJob", "", "jobId",
                job.getId(), QueryJob.class);

        // wait for job to finish
        while (job.getState() != JobStateEnum.JOB_COMPLETE) {
            Thread.sleep(2000);
            job = template().requestBodyAndHeader("salesforce:bulk2GetQueryJob", "", "jobId",
                    job.getId(), QueryJob.class);
        }

        InputStream is = template().requestBodyAndHeader("salesforce:bulk2GetQueryJobResults",
                "", "jobId", job.getId(), InputStream.class);
        Assertions.assertThat(is).isNotNull().describedAs("Query Job results");
        List<String> results = IOUtils.readLines(is, StandardCharsets.UTF_8);
        Assertions.assertThat(results.size()).isGreaterThan(0).describedAs("Query Job results");
    }




    public QueryJob testQueryAllLifecycleCommon() throws Exception {
        QueryJob job = new QueryJob();
        job.setOperation(OperationEnum.QUERY_ALL);
        job.setQuery("SELECT Id, LastName FROM Contact");

        job = template().requestBody("salesforce:bulk2CreateQueryJob", job, QueryJob.class);
        Assertions.assertThat(job.getId()).isNotNull().describedAs("JobId");
        job = template().requestBodyAndHeader("salesforce:bulk2GetQueryJob", "", "jobId",
                job.getId(), QueryJob.class);

        // wait for job to finish
        while (job.getState() != JobStateEnum.JOB_COMPLETE) {
            Thread.sleep(2000);
            job = template().requestBodyAndHeader("salesforce:bulk2GetQueryJob", "", "jobId",
                    job.getId(), QueryJob.class);
        }
        return job;
    }

    @Test
    public void testQueryAllLifecycle() throws Exception {

        QueryJob job = testQueryAllLifecycleCommon();
        InputStream is = template().requestBodyAndHeader("salesforce:bulk2GetQueryJobResults",
                "", "jobId", job.getId(), InputStream.class);
        Assertions.assertThat(is).isNotNull().describedAs("Query Job results");
        List<String> results = IOUtils.readLines(is, StandardCharsets.UTF_8);
        Assertions.assertThat(results.size()).isGreaterThan(0).describedAs("Query Job results");
    }

    @Test
    public void testGetQueryJobResultsWithQueryParameters() throws Exception {
        QueryJob job = testQueryAllLifecycleCommon();
        InputStream is = template().requestBodyAndHeader("salesforce:bulk2GetQueryJobResults?maxRecords=10",
                "", "jobId", job.getId(), InputStream.class);
        Assertions.assertThat(is).isNotNull().describedAs("Query Job results");
        List<String> results = IOUtils.readLines(is, StandardCharsets.UTF_8);
        // Remove the headers
        results.remove(0);
        //Assertions
        Assertions.assertThat(results.size()).isGreaterThan(0).describedAs("Query Job results");
        Assertions.assertThat(results.size()).isLessThanOrEqualTo(10).describedAs("Query Job results max results");
    }

    @Test
    public void testGetQueryJobResultsWithQueryParametersInHeaders() throws Exception {
        QueryJob job = testQueryAllLifecycleCommon();

        Map<String, Object> headers = new HashMap<>();
        headers.put("jobId", job.getId());
        headers.put("maxRecords", "10");

        InputStream is = template().requestBodyAndHeaders("salesforce:bulk2GetQueryJobResults", "", headers, InputStream.class);
        Assertions.assertThat(is).isNotNull().describedAs("Query Job results");
        List<String> results = IOUtils.readLines(is, StandardCharsets.UTF_8);
        // Remove the headers entry
        results.remove(0);
        //Assertions
        Assertions.assertThat(results.size()).isGreaterThan(0).describedAs("Query Job results");
        Assertions.assertThat(results.size()).isLessThanOrEqualTo(10).describedAs("Query Job results max results");
    }


    @Test
    public void testAbort() {
        QueryJob job = new QueryJob();
        job.setOperation(OperationEnum.QUERY);
        job.setQuery("SELECT Id, LastName FROM Contact");

        job = template().requestBody("salesforce:bulk2CreateQueryJob", job, QueryJob.class);
        assertNotNull(job.getId(), "JobId");

        template().sendBodyAndHeader("salesforce:bulk2AbortQueryJob", "", "jobId", job.getId());

        job = template().requestBody("salesforce:bulk2GetQueryJob", job, QueryJob.class);
        Assertions.assertThat(job.getState() == JobStateEnum.ABORTED || job.getState() == JobStateEnum.FAILED)
                .isTrue().describedAs("Expected job to be aborted or failed.");
    }

    @Test
    public void testDelete() throws InterruptedException {
        QueryJob job = new QueryJob();
        job.setOperation(OperationEnum.QUERY);
        job.setQuery("SELECT Id, LastName FROM Contact");

        job = template().requestBody("salesforce:bulk2CreateQueryJob", job, QueryJob.class);
        Assertions.assertThat(job.getId()).as("JobId").isNotNull();

        job = template().requestBody("salesforce:bulk2GetQueryJob", job, QueryJob.class);
        int i = 0;
        while (job.getState() != JobStateEnum.JOB_COMPLETE) {
            i++;
            if (i == 5) {
                throw new IllegalStateException("Job failed to reach JOB_COMPLETE status.");
            }
            Thread.sleep(2000);
            job = template().requestBody("salesforce:bulk2GetQueryJob", job, QueryJob.class);
        }

        template().sendBodyAndHeader("salesforce:bulk2DeleteQueryJob", "", "jobId", job.getId());

        final QueryJob finalJob = job;

        Throwable thrown  = Assertions.catchThrowable(() -> template().requestBody("salesforce:bulk2GetQueryJob", finalJob, QueryJob.class));
        Assertions.assertThat(thrown).isInstanceOf(CamelExecutionException.class);
        CamelExecutionException ex = (CamelExecutionException) thrown;
        Assertions.assertThat(ex.getCause()).isInstanceOf(SalesforceException.class);
        SalesforceException sfEx = (SalesforceException) ex.getCause();
        Assertions.assertThat(sfEx.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testGetAll() {
        QueryJobs jobs = template().requestBody("salesforce:bulk2GetAllQueryJobs", "",
                QueryJobs.class);
        Assertions.assertThat(jobs).isNotNull();
    }
    @Test
    public void testGetAllQueryJobsWithQueryParameters() {
        QueryJobs jobs = template().requestBody("salesforce:bulk2GetAllQueryJobs?isPkChunkingEnabled=false&jobType=V2Query&concurrencyMode=parallel", "", QueryJobs.class);
        assertNotNull(jobs);
        jobs.getRecords()
                .parallelStream()
                .forEach(job -> assertTrue(job.getJobType().equals(V2Query)));
    }

    @Test
    public void testGetAllQueryJobsWithQueryParametersInHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("isPkChunkingEnabled", "false");
        headers.put("jobType", "V2Query");
        headers.put("concurrencyMode", "parallel");
        QueryJobs jobs = template().requestBodyAndHeaders("salesforce:bulk2GetAllQueryJobs", "", headers, QueryJobs.class);
        assertNotNull(jobs);
        jobs.getRecords()
                .parallelStream()
                .forEach(job -> assertTrue(job.getJobType().equals(V2Query)));
    }

    @Test
    public void testGetAllWithMixedV1AndV2Results() {

        // create a QUERY test Job with BulkV1 API
        JobInfo jobInfo = new JobInfo();
        jobInfo.setOperation(org.apache.camel.component.salesforce.api.dto.bulk.OperationEnum.QUERY);
        jobInfo.setContentType(ContentType.XML);
        jobInfo.setObject(Account.class.getSimpleName());
        jobInfo = template().requestBody("salesforce://createJob", jobInfo, JobInfo.class);
        assertNotNull("Missing JobId", jobInfo.getId());
        // test createQuery
        BatchInfo batchInfo = template().requestBody("salesforce:createBatchQuery?sObjectQuery=SELECT Id, Name FROM Account WHERE Name LIKE '%25Bulk API%25'", jobInfo, BatchInfo.class);
        assertNotNull("Null batch query", batchInfo);
        assertNotNull("Null batch query id", batchInfo.getId());

        // Close job with BulkV1 API
        JobInfo jobInfoClosed = template().requestBody("salesforce://closeJob", jobInfo, JobInfo.class);
        assertSame("Job should be CLOSED", org.apache.camel.component.salesforce.api.dto.bulk.JobStateEnum.CLOSED, jobInfoClosed.getState());

        // get all jobs with BulkV2 Api
        QueryJobs jobs = template().requestBody("salesforce:bulk2GetAllQueryJobs", "", QueryJobs.class);
        assertNotNull(jobs);
        jobs.getRecords()
                .parallelStream()
                .filter(job -> job.getId().equals(jobInfoClosed.getId()))
                .forEach(job -> {
                    assertTrue(job.getJobType().equals(CLASSIC));
                    assertTrue(job.getState().equals(CLOSED));
                });

    }

    /**
     *  Bulk API 2.0 is available in API version 41.0 and later.
     *  Query jobs in Bulk API 2.0 are available in API version 47.0 and later.
     */
    protected String salesforceApiVersionToUse() {
        return "47.0";
    }
}