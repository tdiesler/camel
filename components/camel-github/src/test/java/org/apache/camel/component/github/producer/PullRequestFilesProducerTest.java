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
package org.apache.camel.component.github.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.github.GitHubComponent;
import org.apache.camel.component.github.GitHubComponentTestBase;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequestFilesProducerTest extends GitHubComponentTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(PullRequestFilesProducerTest.class);
    private int latestPullRequestNumber;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                context.addComponent("github", new GitHubComponent());
                from("direct:validPullRequest")
                        .process(new MockPullFilesProducerProcessor())
                        .to("github://pullRequestFiles?" + GITHUB_CREDENTIALS_STRING);
            } // end of configure


        };
    }


    @Test
    public void testPullRequestFilesProducer() throws Exception {
        PullRequest pullRequest = pullRequestService.addPullRequest("testPullRequestFilesProducer");
        latestPullRequestNumber = pullRequest.getNumber();

        CommitFile file = new CommitFile();
        file.setFilename("testfile");

        List<CommitFile> commitFiles = new ArrayList<CommitFile>();
        commitFiles.add(file);
        pullRequestService.setFiles(latestPullRequestNumber, commitFiles);

        Endpoint filesProducerEndpoint = getMandatoryEndpoint("direct:validPullRequest");
        Exchange exchange = filesProducerEndpoint.createExchange();

        Exchange resp = template.send(filesProducerEndpoint, exchange);

        assertEquals(resp.getOut().getBody(), commitFiles);
    }


    public class MockPullFilesProducerProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            Message in = exchange.getIn();
            Map<String, Object> headers = in.getHeaders();
            headers.put("GitHubPullRequest", latestPullRequestNumber);
        }
    }


}
