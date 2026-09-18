package io.urlshortener.urlservice.generator;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHasherTest {

	@Test
	void hash_shouldReturnSha256HexDigest_whenGivenARawToken() throws NoSuchAlgorithmException {
		// Arrange
		final String rawToken = "some-raw-token-value";

		// Act
		final String hash = TokenHasher.hash(rawToken);

		// Assert
		final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		final byte[] expectedHashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
		final String expectedHash = HexFormat.of().formatHex(expectedHashBytes);
		assertThat(hash).isEqualTo(expectedHash);
	}

	@Test
	void hash_shouldReturnTheSameHash_whenGivenTheSameRawTokenTwice() {
		// Arrange
		final String rawToken = "some-raw-token-value";

		// Act
		final String firstHash = TokenHasher.hash(rawToken);
		final String secondHash = TokenHasher.hash(rawToken);

		// Assert
		assertThat(firstHash).isEqualTo(secondHash);
	}

	@Test
	void hash_shouldReturnDifferentHashes_whenGivenDifferentRawTokens() {
		// Arrange
		final String firstToken = "raw-token-one";
		final String secondToken = "raw-token-two";

		// Act
		final String firstHash = TokenHasher.hash(firstToken);
		final String secondHash = TokenHasher.hash(secondToken);

		// Assert
		assertThat(firstHash).isNotEqualTo(secondHash);
	}

}
