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
package org.apache.camel.component.salesforce.internal.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.StreamCache;
import org.apache.camel.component.salesforce.SalesforceEndpoint;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.dto.bulk.BatchInfo;
import org.apache.camel.component.salesforce.api.dto.bulk.ContentType;
import org.apache.camel.component.salesforce.api.dto.bulk.JobInfo;
import org.apache.camel.component.salesforce.internal.client.BulkApiClient;
import org.apache.camel.component.salesforce.internal.client.DefaultBulkApiClient;
import org.apache.camel.converter.stream.StreamCacheConverter;
import org.apache.camel.util.ServiceHelper;

import static org.apache.camel.component.salesforce.SalesforceEndpointConfig.BATCH_ID;
import static org.apache.camel.component.salesforce.SalesforceEndpointConfig.CONTENT_TYPE;
import static org.apache.camel.component.salesforce.SalesforceEndpointConfig.JOB_ID;
import static org.apache.camel.component.salesforce.SalesforceEndpointConfig.RESULT_ID;
import static org.apache.camel.component.salesforce.SalesforceEndpointConfig.SOBJECT_QUERY;

public class BulkApiProcessor extends AbstractSalesforceProcessor {

    private BulkApiClient bulkClient;

    public BulkApiProcessor(SalesforceEndpoint endpoint) throws SalesforceException {
        super(endpoint);

        this.bulkClient = new DefaultBulkApiClient(
                endpointConfigMap.get(SalesforceEndpointConfig.API_VERSION), session, httpClient);
    }

    @Override
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        boolean done = false;

        try {
            switch (operationName) {
            case CREATE_JOB:
                JobInfo jobBody = exchange.getIn().getMandatoryBody(JobInfo.class);
                bulkClient.createJob(jobBody, new BulkApiClient.JobInfoResponseCallback() {
                    @Override
                    public void onResponse(JobInfo jobInfo, SalesforceException ex) {
                        processResponse(exchange, jobInfo, ex, callback);
                    }
                });

                break;

            case GET_JOB:
                jobBody = exchange.getIn().getBody(JobInfo.class);
                String jobId;
                if (jobBody != null) {
                    jobId = jobBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getJob(jobId, new BulkApiClient.JobInfoResponseCallback() {
                    @Override
                    public void onResponse(JobInfo jobInfo, SalesforceException ex) {
                        processResponse(exchange, jobInfo, ex, callback);
                    }
                });

                break;

            case CLOSE_JOB:
                jobBody = exchange.getIn().getBody(JobInfo.class);
                if (jobBody != null) {
                    jobId = jobBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.closeJob(jobId, new BulkApiClient.JobInfoResponseCallback() {
                    @Override
                    public void onResponse(JobInfo jobInfo, SalesforceException ex) {
                        processResponse(exchange, jobInfo, ex, callback);
                    }
                });

                break;

            case ABORT_JOB:
                jobBody = exchange.getIn().getBody(JobInfo.class);
                if (jobBody != null) {
                    jobId = jobBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.abortJob(jobId, new BulkApiClient.JobInfoResponseCallback() {
                    @Override
                    public void onResponse(JobInfo jobInfo, SalesforceException ex) {
                        processResponse(exchange, jobInfo, ex, callback);
                    }
                });

                break;

            case CREATE_BATCH:
                // since request is in the body, use headers or endpoint params
                ContentType contentType = ContentType.fromValue(
                        getParameter(CONTENT_TYPE, exchange, IGNORE_BODY, NOT_OPTIONAL));
                jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);

                InputStream request;
                try {
                    request = exchange.getIn().getMandatoryBody(InputStream.class);
                } catch (CamelException e) {
                    String msg = "Error preparing batch request: " + e.getMessage();
                    throw new SalesforceException(msg, e);
                }

                bulkClient.createBatch(request, jobId, contentType, new BulkApiClient.BatchInfoResponseCallback() {
                    @Override
                    public void onResponse(BatchInfo batchInfo, SalesforceException ex) {
                        processResponse(exchange, batchInfo, ex, callback);
                    }
                });

                break;

            case GET_BATCH:
                BatchInfo batchBody = exchange.getIn().getBody(BatchInfo.class);
                String batchId;
                if (batchBody != null) {
                    jobId = batchBody.getJobId();
                    batchId = batchBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    batchId = getParameter(BATCH_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getBatch(jobId, batchId, new BulkApiClient.BatchInfoResponseCallback() {
                    @Override
                    public void onResponse(BatchInfo batchInfo, SalesforceException ex) {
                        processResponse(exchange, batchInfo, ex, callback);
                    }
                });

                break;

            case GET_ALL_BATCHES:
                jobBody = exchange.getIn().getBody(JobInfo.class);
                if (jobBody != null) {
                    jobId = jobBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getAllBatches(jobId, new BulkApiClient.BatchInfoListResponseCallback() {
                    @Override
                    public void onResponse(List<BatchInfo> batchInfoList, SalesforceException ex) {
                        processResponse(exchange, batchInfoList, ex, callback);
                    }
                });

                break;

            case GET_REQUEST:
                batchBody = exchange.getIn().getBody(BatchInfo.class);
                if (batchBody != null) {
                    jobId = batchBody.getJobId();
                    batchId = batchBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    batchId = getParameter(BATCH_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }

                bulkClient.getRequest(jobId, batchId, new BulkApiClient.StreamResponseCallback() {
                    @Override
                    public void onResponse(InputStream inputStream, SalesforceException ex) {
                        // read the request stream into a StreamCache temp file
                        // ensures the connection is read
                        StreamCache body = null;
                        if (inputStream != null) {
                            try {
                                body = StreamCacheConverter.convertToStreamCache(inputStream, exchange);
                            } catch (IOException e) {
                                String msg = "Error retrieving batch request: " + e.getMessage();
                                ex = new SalesforceException(msg, e);
                            } finally {
                                // close the input stream to release the Http connection
                                try {
                                    inputStream.close();
                                } catch (IOException ignore) {
                                }
                            }
                        }
                        processResponse(exchange, body, ex, callback);
                    }
                });

                break;

            case GET_RESULTS:
                batchBody = exchange.getIn().getBody(BatchInfo.class);
                if (batchBody != null) {
                    jobId = batchBody.getJobId();
                    batchId = batchBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    batchId = getParameter(BATCH_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getResults(jobId, batchId, new BulkApiClient.StreamResponseCallback() {
                    @Override
                    public void onResponse(InputStream inputStream, SalesforceException ex) {
                        // read the result stream into a StreamCache temp file
                        // ensures the connection is read
                        StreamCache body = null;
                        if (inputStream != null) {
                            try {
                                body = StreamCacheConverter.convertToStreamCache(inputStream, exchange);
                            } catch (IOException e) {
                                String msg = "Error retrieving batch results: " + e.getMessage();
                                ex = new SalesforceException(msg, e);
                            } finally {
                                // close the input stream to release the Http connection
                                try {
                                    inputStream.close();
                                } catch (IOException ignore) {
                                }
                            }
                        }
                        processResponse(exchange, body, ex, callback);
                    }
                });

                break;

            case CREATE_BATCH_QUERY:
                jobBody = exchange.getIn().getBody(JobInfo.class);
                String soqlQuery;
                if (jobBody != null) {
                    jobId = jobBody.getId();
                    contentType = jobBody.getContentType();
                    // use SOQL query from header or endpoint config
                    soqlQuery = getParameter(SOBJECT_QUERY, exchange, IGNORE_BODY, NOT_OPTIONAL);
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    contentType = ContentType.fromValue(
                            getParameter(CONTENT_TYPE, exchange, IGNORE_BODY, NOT_OPTIONAL));
                    // reuse SOBJECT_QUERY property
                    soqlQuery = getParameter(SOBJECT_QUERY, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.createBatchQuery(jobId, soqlQuery, contentType,
                        new BulkApiClient.BatchInfoResponseCallback() {
                            @Override
                            public void onResponse(BatchInfo batchInfo, SalesforceException ex) {
                                processResponse(exchange, batchInfo, ex, callback);
                            }
                        });

                break;

            case GET_QUERY_RESULT_IDS:
                batchBody = exchange.getIn().getBody(BatchInfo.class);
                if (batchBody != null) {
                    jobId = batchBody.getJobId();
                    batchId = batchBody.getId();
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    batchId = getParameter(BATCH_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getQueryResultIds(jobId, batchId, new BulkApiClient.QueryResultIdsCallback() {
                    @Override
                    public void onResponse(List<String> ids, SalesforceException ex) {
                        processResponse(exchange, ids, ex, callback);
                    }
                });

                break;

            case GET_QUERY_RESULT:
                batchBody = exchange.getIn().getBody(BatchInfo.class);
                String resultId;
                if (batchBody != null) {
                    jobId = batchBody.getJobId();
                    batchId = batchBody.getId();
                    resultId = getParameter(RESULT_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                } else {
                    jobId = getParameter(JOB_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    batchId = getParameter(BATCH_ID, exchange, IGNORE_BODY, NOT_OPTIONAL);
                    resultId = getParameter(RESULT_ID, exchange, USE_BODY, NOT_OPTIONAL);
                }
                bulkClient.getQueryResult(jobId, batchId, resultId, new BulkApiClient.StreamResponseCallback() {
                    @Override
                    public void onResponse(InputStream inputStream, SalesforceException ex) {
                        StreamCache body = null;
                        if (inputStream != null) {
                            // read the result stream into a StreamCache temp file
                            // ensures the connection is read
                            try {
                                body = StreamCacheConverter.convertToStreamCache(inputStream, exchange);
                            } catch (IOException e) {
                                String msg = "Error retrieving query result: " + e.getMessage();
                                ex = new SalesforceException(msg, e);
                            } finally {
                                // close the input stream to release the Http connection
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
                        }
                        processResponse(exchange, body, ex, callback);
                    }
                });

                break;

            default:
                throw new SalesforceException("Unknow operation name: " + operationName, null);

            }
        } catch (SalesforceException e) {
            exchange.setException(new SalesforceException(
                    String.format("Error processing %s: [%s] \"%s\"",
                            operationName, e.getStatusCode(), e.getMessage()), e));
            callback.done(true);
            done = true;
        } catch (InvalidPayloadException e) {
            exchange.setException(new SalesforceException(
                    String.format("Unexpected Error processing %s: \"%s\"",
                            operationName, e.getMessage()), e));
            callback.done(true);
            done = true;
        } catch (RuntimeException e) {
            exchange.setException(new SalesforceException(
                    String.format("Unexpected Error processing %s: \"%s\"",
                            operationName, e.getMessage()), e));
            callback.done(true);
            done = true;
        }

        // continue routing asynchronously if false
        return done;
    }

    private void processResponse(Exchange exchange, Object body, SalesforceException ex, AsyncCallback callback) {
        final Message out = exchange.getOut();
        if (ex != null) {
            exchange.setException(ex);
        } else {
            out.setBody(body);
        }

        // copy headers and attachments
        out.getHeaders().putAll(exchange.getIn().getHeaders());
        out.getAttachments().putAll(exchange.getIn().getAttachments());

        // signal exchange completion
        callback.done(false);
    }

    @Override
    public void start() throws Exception {
        ServiceHelper.startService(bulkClient);
    }

    @Override
    public void stop() throws Exception {
        // stop the client
        ServiceHelper.stopService(bulkClient);
    }
}
