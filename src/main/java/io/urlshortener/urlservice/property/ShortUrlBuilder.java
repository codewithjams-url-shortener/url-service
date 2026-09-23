package io.urlshortener.urlservice.property;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Resolves a short code into its public short URL, against the configured base URL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortUrlBuilder {

	/**
	 * Source of the base URL that short codes are resolved against.
	 */
	private final UrlShortenerProperties shortenerProperties;

	/**
	 * Builds the public short URL for a given short code.
	 *
	 * @param shortCode the short code to resolve.
	 * @return the short code resolved against the configured base URL.
	 */
	public URI build(final String shortCode) {
		return URI.create(shortenerProperties.getBaseUrl()).resolve(shortCode);
	}

}
