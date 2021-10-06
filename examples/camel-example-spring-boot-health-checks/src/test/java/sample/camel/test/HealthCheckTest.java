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

package sample.camel.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckTest {
	//CHECKSTYLE:OFF
    @LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;


	@Test
	public void actuatorHealth() throws Exception {
		final JsonNode jsonNode = this.restTemplate.getForObject("http://localhost:" + port + "/actuator/health/camel", JsonNode.class);

		Assertions.assertThat(jsonNode.get("status")).isNotNull();
		Assertions.assertThat(jsonNode.get("status").asText()).isEqualTo("UP");

		Assertions.assertThat(jsonNode.get("details")).isNotNull();
		Assertions.assertThat(jsonNode.get("details").get("name")).isNotNull();
		Assertions.assertThat(jsonNode.get("details").get("name").asText()).isEqualTo("SampleHealthChecks");

		Assertions.assertThat(jsonNode.get("details").get("status")).isNotNull();
		Assertions.assertThat(jsonNode.get("details").get("status").asText()).isEqualTo("Started");
	}

	@Test
	public void actuatorCamelRoutes() throws Exception {
		final JsonNode jsonNode = this.restTemplate.getForObject("http://localhost:" + port + "/actuator/camelroutes", JsonNode.class);

		Assertions.assertThat(jsonNode.isArray()).isTrue();

		Assertions.assertThat(jsonNode.get(0).get("id").asText()).isEqualTo("foo");
		Assertions.assertThat(jsonNode.get(1).get("id").asText()).isEqualTo("bar");
		Assertions.assertThat(jsonNode.get(2).get("id").asText()).isEqualTo("slow");
	}

	@Test
	public void actuatorCamelRoutesDetail() throws Exception {
		final String routeId = "foo";
		final JsonNode jsonNode = this.restTemplate.getForObject("http://localhost:" + port + "/actuator/camelroutes/" + routeId + "/detail", JsonNode.class);

		Assertions.assertThat(jsonNode.get("id").asText()).isEqualTo(routeId);
		Assertions.assertThat(jsonNode.get("details")).isNotNull();
	}
	//CHECKSTYLE:ON
}
