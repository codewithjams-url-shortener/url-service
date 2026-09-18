package io.urlshortener.urlservice.config;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers Jackson modules needed by the OpenAPI-generated DTOs beyond Spring Boot's defaults.
 */
@Configuration
public class JacksonConfig {

	/**
	 * Teaches Jackson how to (de)serialize {@code JsonNullable<T>} fields, used by generated DTOs
	 * (e.g. {@code PatchLinkRequest.expiresAt}) to distinguish an omitted field from one explicitly
	 * set to {@code null}. Spring Boot's Jackson autoconfiguration auto-registers any {@code Module}
	 * bean into the application's {@code ObjectMapper}.
	 *
	 * @return the {@link JsonNullableModule}.
	 */
	@Bean
	public JsonNullableModule jsonNullableModule() {
		return new JsonNullableModule();
	}

}
