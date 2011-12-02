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
package org.apache.camel.component.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;

public class SolrDeleteTest extends SolrComponentTestSupport {

    @Test
    public void testDeleteById() throws Exception {

        //insert, commit and verify
        solrInsertTestEntry();
        solrCommit();
        assertEquals("wrong number of entries found", 1, executeSolrQuery("id:" + TEST_ID).getResults().getNumFound());

        //delete
        template.sendBodyAndHeader("direct:start", TEST_ID, SolrConstants.OPERATION, SolrConstants.OPERATION_DELETE_BY_ID);
        solrCommit();

        //verify
        assertEquals("wrong number of entries found", 0, executeSolrQuery("id:" + TEST_ID).getResults().getNumFound());
    }

    @Test
    public void testDeleteByQuery() throws Exception {

        //insert, commit and verify
        solrInsertTestEntry();
        solrCommit();
        assertEquals("wrong number of entries found", 1, executeSolrQuery("id:" + TEST_ID).getResults().getNumFound());

        //delete
        template.sendBodyAndHeader("direct:start", "id:" + TEST_ID, SolrConstants.OPERATION, SolrConstants.OPERATION_DELETE_BY_QUERY);
        solrCommit();

        //verify
        assertEquals("wrong number of entries found", 0, executeSolrQuery("id:" + TEST_ID).getResults().getNumFound());
    }
}
