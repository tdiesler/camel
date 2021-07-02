package sample.camel.test;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckTest {

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
}
