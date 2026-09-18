package io.urlshortener.urlservice.generator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes a raw management token for storage and comparison, without ever needing the raw token itself again.
 */
@Slf4j
@UtilityClass
public class TokenHasher {

	/**
	 * Hashes a raw token with SHA-256.
	 *
	 * @param rawToken the plaintext token to hash.
	 * @return the hex-encoded SHA-256 hash of {@code rawToken}.
	 */
	public String hash(final String rawToken) {
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
