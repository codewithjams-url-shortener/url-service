package io.urlshortener.urlservice.generator;

import io.urlshortener.urlservice.constant.GeneratorConstants;
import io.urlshortener.urlservice.property.UrlShortenerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates random base62 short codes for links that don't specify a custom alias.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortCodeGenerator {

	/**
	 * Shared random source used to pick each character of the generated short code.
	 */
	private final SecureRandom secureRandom;

	/**
	 * Source of the configured short-code length.
	 */
	private final UrlShortenerProperties shortenerProperties;

	/**
	 * Generates a new random short code.
	 *
	 * @return a random base62 string of the configured length ({@code url-shortener.short-code-length}),
	 *         which is guaranteed positive by validation on {@link UrlShortenerProperties} at startup.
	 */
	public String generateShortCode() {
		final int shortCodeLength = shortenerProperties.getShortCodeLength();
		final StringBuilder shortCode = new StringBuilder();
		for (int i = 0; i < shortCodeLength; ++i) {
			final int charIndex = secureRandom.nextInt(GeneratorConstants.CHARACTER_BOUND);
			final char c = getCharacterFromIndex(charIndex);
			shortCode.append(c);
		}
		return shortCode.toString();
	}

	private char getCharacterFromIndex(final int index) {
		if (index >= 0 && index <= 25) { // a-z
			return (char) ('a' + index);
		} else if (index >= 26 && index <= 51) { // A-Z
			return (char) ('A' + (index - 26));
		} else { // 0-9
			return (char) ('0' + (index - 52));
		}
	}

}
