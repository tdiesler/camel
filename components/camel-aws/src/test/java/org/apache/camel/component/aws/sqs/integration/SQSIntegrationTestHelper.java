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
package org.apache.camel.component.aws.sqs.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import org.apache.camel.test.junit4.CamelTestSupport;

public class SQSIntegrationTestHelper extends CamelTestSupport {

    private static final String AWS_ACCESS_KEY_PARAM = "AWS_ACCESS_KEY";
    private static final String AWS_SECRET_KEY_PARAM = "AWS_SECRET_KEY";
    private static final String AWS_REGION_PARAM = "AWS_REGION";

    public static String urlCredentials() {
        return String.format("accessKey={{env:%s}}&secretKey={{env:%s}}&region={{env:%s}}",
                SQSIntegrationTestHelper.AWS_ACCESS_KEY_PARAM, SQSIntegrationTestHelper.AWS_SECRET_KEY_PARAM,
                SQSIntegrationTestHelper.AWS_REGION_PARAM);
    }


    public static void deleteQueues(String... names) {
        Set namesSet = new HashSet(Arrays.asList(names));
        AWSCredentials credentials = new BasicAWSCredentials(System.getenv(AWS_ACCESS_KEY_PARAM), System.getenv(AWS_SECRET_KEY_PARAM));
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AmazonSQS client = null;
        try {
            String region = Regions.valueOf(System.getenv(AWS_REGION_PARAM)).getName();
            client = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
            ListQueuesResult lqr = client.listQueues();
            for (String url : lqr.getQueueUrls()) {
                for (Iterator<String> iter = namesSet.iterator(); iter.hasNext();) {
                    String name = iter.next();
                    if (url.endsWith("/" + name)) {
                        client.deleteQueue(new DeleteQueueRequest().withQueueUrl(url));
                        iter.remove();
                        break;
                    }
                }
                if (namesSet.isEmpty()) {
                    break;
                }
            }
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
}
