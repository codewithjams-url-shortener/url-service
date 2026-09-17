package io.urlshortener.urlservice.property;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binds the {@code url-shortener.*} configuration properties governing short-code generation
 * and short URL construction. Validated once at startup, so consumers can trust these values
 * without re-checking them at call time.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "url-shortener")
public class UrlShortenerProperties {

	/**
	 * Base URL that generated short codes are resolved against to form a public short URL.
	 */
	private String baseUrl;

	/**
	 * Length, in characters, of generated (non-custom-alias) short codes.
	 */
	@Positive
	private Integer shortCodeLength;

}
