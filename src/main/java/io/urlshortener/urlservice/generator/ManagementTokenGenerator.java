package io.urlshortener.urlservice.generator;

import io.urlshortener.urlservice.constant.GeneratorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically random management tokens, used to authorize later management
 * operations (update/delete) on a created link.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManagementTokenGenerator {

	/**
	 * Shared random source used to generate raw token bytes.
	 */
	private final SecureRandom secureRandom;

	/**
	 * Generates a new management token.
	 *
	 * @return a new {@link ManagementToken} with a random, URL-safe raw token and its SHA-256 hash.
	 */
	public ManagementToken generate() {
		final byte[] rawBytes = new byte[GeneratorConstants.TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(rawBytes);
		final String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);
		final String tokenHash = TokenHasher.hash(rawToken);
		return new ManagementToken(rawToken, tokenHash);
	}

}
