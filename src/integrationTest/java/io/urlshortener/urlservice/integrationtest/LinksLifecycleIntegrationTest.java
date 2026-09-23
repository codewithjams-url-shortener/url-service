package io.urlshortener.urlservice.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import io.urlshortener.urlservice.integrationtest.config.DeployedEnvironmentConfig;
import io.urlshortener.urlservice.integrationtest.config.IntegrationTestBootstrap;
import io.urlshortener.urlservice.integrationtest.config.LocalEnvironmentConfig;
import io.urlshortener.urlservice.integrationtest.constant.LinksApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Integration tests for the {@code /links} API contract, shared between two flavors selected via
 * {@code spring.profiles.active}:
 *
 * <ul>
 *     <li>{@code local} ({@link LocalEnvironmentConfig}) - launches the built url-service jar as a
 *     separate process against a Testcontainers-managed Floci instance, and hits it over real HTTP.</li>
 *     <li>{@code deployed} ({@link DeployedEnvironmentConfig}) - hits an already-deployed url-service
 *     instance over real HTTP, at the URL given by the {@code integration-test.base-url} property.</li>
 * </ul>
 *
 * <p>Only one profile is active per run, so only one of the two {@code @Profile}-gated configurations
 * ever contributes its {@link RestClient} bean. The test bodies never know which flavor is running.
 * Both configurations are picked up by {@code @SpringBootApplication}'s component scan since they live
 * in the same package as {@link IntegrationTestBootstrap} - no explicit {@code @Import} needed.
 *
 * <p>Requests and responses are handled as raw JSON ({@link JsonNode}) rather than url-service's own
 * generated DTOs, so this suite has no compile-time dependency on the application it is testing - it
 * verifies the wire contract, not compile-time type compatibility with the app's internals. Request
 * bodies are loaded from {@code src/integrationTest/resources/requests} rather than inlined as Java text
 * blocks, so the JSON itself stays valid, reviewable JSON rather than Java-escaped string literals.
 */
@SpringBootTest(
		classes = IntegrationTestBootstrap.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class LinksLifecycleIntegrationTest {

	private static final String LINK_WITH_LONG_URL_TEMPLATE = loadRequestTemplate("link-with-long-url.json");

	private static final String LINK_WITH_LONG_URL_AND_ALIAS_TEMPLATE =
			loadRequestTemplate("link-with-long-url-and-alias.json");

	@Autowired
	private RestClient linksApiClient;

	@Test
	void createLink_shouldReturnCreatedLink_whenFetchedByShortCode() {

		// Arrange
		final String longUrl = "https://example.com/some-page";

		// Act
		final JsonNode created = createLink(longUrl);
		final JsonNode fetched = getLink(created.get(LinksApiConstants.FIELD_SHORT_CODE).asText());

		// Assert
		assertThat(fetched)
				.as("GET /links/%s should return a body", created.get(LinksApiConstants.FIELD_SHORT_CODE).asText())
				.isNotNull();
		assertThat(fetched.get(LinksApiConstants.FIELD_SHORT_CODE).asText())
				.as("shortCode returned by GET should match the one returned at creation")
				.isEqualTo(created.get(LinksApiConstants.FIELD_SHORT_CODE).asText());
		assertThat(fetched.get(LinksApiConstants.FIELD_LONG_URL).asText())
				.as("longUrl returned by GET should match the longUrl submitted at creation")
				.isEqualTo(longUrl);

	}

	@Test
	void updateLink_shouldReturnUpdatedLink_whenManagementTokenMatches() {

		// Arrange
		final JsonNode created = createLink("https://example.com/original");
		final String shortCode = created.get(LinksApiConstants.FIELD_SHORT_CODE).asText();
		final String managementToken = created.get(LinksApiConstants.FIELD_MANAGEMENT_TOKEN).asText();
		final String newLongUrl = "https://example.com/updated";

		// Act
		final JsonNode updated = patchLink(shortCode, managementToken, LINK_WITH_LONG_URL_TEMPLATE.formatted(newLongUrl));

		// Assert
		assertThat(updated.get(LinksApiConstants.FIELD_SHORT_CODE).asText())
				.as("shortCode should be unchanged after a successful PATCH")
				.isEqualTo(shortCode);
		assertThat(updated.get(LinksApiConstants.FIELD_LONG_URL).asText())
				.as("longUrl should reflect the value just submitted via PATCH")
				.isEqualTo(newLongUrl);

	}

	@Test
	void updateLink_shouldReturnForbidden_whenManagementTokenDoesNotMatch() {

		// Arrange
		final JsonNode created = createLink("https://example.com/protected");
		final String shortCode = created.get(LinksApiConstants.FIELD_SHORT_CODE).asText();
		final String wrongToken = "not-" + created.get(LinksApiConstants.FIELD_MANAGEMENT_TOKEN).asText();
		final String requestBody = LINK_WITH_LONG_URL_TEMPLATE.formatted("https://example.com/hijacked");

		// Act
		final Throwable thrown = catchThrowable(() -> patchLink(shortCode, wrongToken, requestBody));

		// Assert
		assertHttpStatus(thrown, HttpStatus.FORBIDDEN,
				"PATCH with a management token that doesn't match the one issued at creation "
						+ "should be rejected with 403 Forbidden");

	}

	@Test
	void deleteLink_shouldRemoveLink_whenManagementTokenMatches() {

		// Arrange
		final JsonNode created = createLink("https://example.com/to-delete");
		final String shortCode = created.get(LinksApiConstants.FIELD_SHORT_CODE).asText();
		final String managementToken = created.get(LinksApiConstants.FIELD_MANAGEMENT_TOKEN).asText();

		// Act
		final ResponseEntity<Void> response = deleteLink(shortCode, managementToken);
		final Throwable thrownOnGetAfterDelete = catchThrowable(() -> getLink(shortCode));

		// Assert
		assertThat(response.getStatusCode())
				.as("DELETE /links/%s with a matching management token should return 204 No Content", shortCode)
				.isEqualTo(HttpStatus.NO_CONTENT);
		assertHttpStatus(thrownOnGetAfterDelete, HttpStatus.NOT_FOUND,
				"GET /links/%s after a successful DELETE should return 404 Not Found, since the link no longer exists"
						.formatted(shortCode));

	}

	@Test
	void createLink_shouldReturnConflict_whenCustomAliasAlreadyInUse() {

		// Arrange
		final String alias = uniqueAlias();
		createLinkWithBody(LINK_WITH_LONG_URL_AND_ALIAS_TEMPLATE.formatted("https://example.com/first", alias));
		final String secondRequestBody =
				LINK_WITH_LONG_URL_AND_ALIAS_TEMPLATE.formatted("https://example.com/second", alias);

		// Act
		final Throwable thrown = catchThrowable(() -> createLinkWithBody(secondRequestBody));

		// Assert
		assertHttpStatus(thrown, HttpStatus.CONFLICT,
				("Creating a link with customAlias '%s', already in use by another link, " +
						"should be rejected with 409 Conflict").formatted(alias));

	}

	private JsonNode createLinkWithBody(final String requestBody) {
		return linksApiClient.post()
				.uri("/links")
				.contentType(MediaType.APPLICATION_JSON)
				.body(requestBody)
				.retrieve()
				.body(JsonNode.class);
	}

	private JsonNode createLink(final String longUrl) {
		return createLinkWithBody(LINK_WITH_LONG_URL_TEMPLATE.formatted(longUrl));
	}

	private JsonNode getLink(final String shortCode) {
		return linksApiClient.get()
				.uri("/links/{shortCode}", shortCode)
				.retrieve()
				.body(JsonNode.class);
	}

	private JsonNode patchLink(final String shortCode, final String managementToken, final String requestBody) {
		return linksApiClient.patch()
				.uri("/links/{shortCode}", shortCode)
				.header(LinksApiConstants.HEADER_MANAGEMENT_TOKEN, managementToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(requestBody)
				.retrieve()
				.body(JsonNode.class);
	}

	private ResponseEntity<Void> deleteLink(final String shortCode, final String managementToken) {
		return linksApiClient.delete()
				.uri("/links/{shortCode}", shortCode)
				.header(LinksApiConstants.HEADER_MANAGEMENT_TOKEN, managementToken)
				.retrieve()
				.toBodilessEntity();
	}

	private static String uniqueAlias() {
		return "clash" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

	private static void assertHttpStatus(final Throwable thrown, final HttpStatus expectedStatus,
										  final String description) {
		assertThat(thrown)
				.as("%s (expected an HttpClientErrorException to be thrown)", description)
				.isInstanceOf(HttpClientErrorException.class);
		final HttpStatusCode actualStatus = ((HttpClientErrorException) thrown).getStatusCode();
		assertThat(actualStatus)
				.as(description)
				.isEqualTo(expectedStatus);
	}

	private static String loadRequestTemplate(final String fileName) {
		final ClassPathResource resource = new ClassPathResource("requests/" + fileName);
		try {
			return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load request template: " + fileName, e);
		}
	}

}
