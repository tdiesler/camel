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
package org.apache.camel.component.sql;

import java.util.Map;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @version 
 */
public class SqlComponent extends DefaultComponent {
    private DataSource dataSource;

    public SqlComponent() {
    }

    public SqlComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        String dataSourceRef = getAndRemoveParameter(parameters, "dataSourceRef", String.class);
        if (dataSourceRef != null) {
            dataSource = CamelContextHelper.mandatoryLookup(getCamelContext(), dataSourceRef, DataSource.class);
        }
        String parameterPlaceholderSubstitute = getAndRemoveParameter(parameters, "placeholder", String.class, "#");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        IntrospectionSupport.setProperties(jdbcTemplate, parameters, "template.");

        String query = remaining.replaceAll(parameterPlaceholderSubstitute, "?");

        String onConsume = getAndRemoveParameter(parameters, "consumer.onConsume", String.class);
        if (onConsume == null) {
            onConsume = getAndRemoveParameter(parameters, "onConsume", String.class);
        }
        if (onConsume != null) {
            onConsume = onConsume.replaceAll(parameterPlaceholderSubstitute, "?");
        }
        String onConsumeBatchComplete = getAndRemoveParameter(parameters, "consumer.onConsumeBatchComplete", String.class);
        if (onConsumeBatchComplete == null) {
            onConsumeBatchComplete = getAndRemoveParameter(parameters, "onConsumeBatchComplete", String.class);
        }
        if (onConsumeBatchComplete != null) {
            onConsumeBatchComplete = onConsumeBatchComplete.replaceAll(parameterPlaceholderSubstitute, "?");
        }

        SqlEndpoint endpoint = new SqlEndpoint(uri, this, jdbcTemplate, query);
        endpoint.setOnConsume(onConsume);
        endpoint.setOnConsumeBatchComplete(onConsumeBatchComplete);
        return endpoint;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
