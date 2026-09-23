package io.urlshortener.urlservice.integrationtest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

/**
 * Wires the shared {@link io.urlshortener.urlservice.integrationtest.LinksLifecycleIntegrationTest
 * LinksLifecycleIntegrationTest} to talk to an already-deployed url-service instance over real HTTP. Active only under
 * the {@code deployed} profile<br/>
 * {@link LocalEnvironmentConfig} supplies the equivalent bean for the {@code local} profile.
 */
@Configuration
@Profile("deployed")
public class DeployedEnvironmentConfig {

	@Bean
	public RestClient linksApiClient(@Value("${integration-test.base-url}") final String baseUrl) {
		return RestClient.builder()
				.baseUrl(baseUrl)
				.build();
	}

}
