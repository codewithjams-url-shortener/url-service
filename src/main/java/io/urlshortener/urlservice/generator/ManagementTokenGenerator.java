package io.urlshortener.urlservice.generator;

import io.urlshortener.urlservice.constant.GeneratorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

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
		final String tokenHash = hash(rawToken);
		return new ManagementToken(rawToken, tokenHash);
	}

	private String hash(final String rawToken) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is a mandatory algorithm on every standard Java platform -- this is not expected to ever
			// actually throw.
			log.atError()
					.addKeyValue("algorithm", "SHA-256")
					.setCause(e)
					.log("Required hashing algorithm unavailable");
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}

}
