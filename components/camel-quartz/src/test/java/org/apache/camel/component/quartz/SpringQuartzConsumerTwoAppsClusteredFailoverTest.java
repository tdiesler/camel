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
package org.apache.camel.component.quartz;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.TestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests a Quartz based cluster setup of two Camel Apps being triggered through {@link QuartzConsumer}.
 * 
 * @version
 */
public class SpringQuartzConsumerTwoAppsClusteredFailoverTest extends TestSupport {

    @Test
    public void testQuartzPersistentStoreClusteredApp() throws Exception {
        // boot up the database the two apps are going to share inside a clustered quartz setup
        AbstractXmlApplicationContext db = new ClassPathXmlApplicationContext("org/apache/camel/component/quartz/SpringQuartzConsumerClusteredAppDatabase.xml");
        db.start();

        // now launch the first clustered app which will acquire the quartz database lock and become the master
        AbstractXmlApplicationContext app = new ClassPathXmlApplicationContext("org/apache/camel/component/quartz/SpringQuartzConsumerClusteredAppOne.xml");
        app.start();

        // as well as the second one which will run in slave mode as it will not be able to acquire the same lock
        AbstractXmlApplicationContext app2 = new ClassPathXmlApplicationContext("org/apache/camel/component/quartz/SpringQuartzConsumerClusteredAppTwo.xml");
        app2.start();

        CamelContext camel = app.getBean("camelContext", CamelContext.class);

        MockEndpoint mock = camel.getEndpoint("mock:result", MockEndpoint.class);
        mock.expectedMinimumMessageCount(3);
        mock.expectedMessagesMatches(new ClusteringPredicate(true));

        // let the route run a bit...
        Thread.sleep(5000);

        mock.assertIsSatisfied();

        // now let's simulate a crash of the first app (the quartz instance 'app-one')
        log.warn("The first app is going to crash NOW!");
        app.close();

        log.warn("Crashed...");
        log.warn("Crashed...");
        log.warn("Crashed...");

        // wait long enough until the second app takes it over...
        Thread.sleep(20000);
        // inside the logs one can then clearly see how the route of the second app ('app-two') starts consuming:
        // 2013-09-28 19:50:43,900 [main           ] WARN  ntTwoAppsClusteredFailoverTest - Crashed...
        // 2013-09-28 19:50:43,900 [main           ] WARN  ntTwoAppsClusteredFailoverTest - Crashed...
        // 2013-09-28 19:50:43,900 [main           ] WARN  ntTwoAppsClusteredFailoverTest - Crashed...
        // 2013-09-28 19:50:58,892 [_ClusterManager] INFO  LocalDataSourceJobStore        - ClusterManager: detected 1 failed or restarted instances.
        // 2013-09-28 19:50:58,892 [_ClusterManager] INFO  LocalDataSourceJobStore        - ClusterManager: Scanning for instance "app-one"'s failed in-progress jobs.
        // 2013-09-28 19:50:58,913 [eduler_Worker-1] INFO  triggered                      - Exchange[ExchangePattern: InOnly, BodyType: String, Body: clustering PONGS!]

        CamelContext camel2 = app2.getBean("camelContext2", CamelContext.class);

        MockEndpoint mock2 = camel2.getEndpoint("mock:result", MockEndpoint.class);
        mock2.expectedMinimumMessageCount(3);
        mock2.expectedMessagesMatches(new ClusteringPredicate(false));

        mock2.assertIsSatisfied();

        // close the second app as we're done now
        app2.close();

        // and as the last step shutdown the database...
        db.close();
    }
    
    private static class ClusteringPredicate implements Predicate {

        private final String expectedPayload;
        
        ClusteringPredicate(boolean pings) {
            expectedPayload = pings ? "clustering PINGS!" : "clustering PONGS!";
        }
        
        @Override
        public boolean matches(Exchange exchange) {
            return exchange.getIn().getBody().equals(expectedPayload);
        }

    }

}
