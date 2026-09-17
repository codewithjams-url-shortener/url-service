package io.urlshortener.urlservice.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ManagementTokenGeneratorTest {

	private ManagementTokenGenerator managementTokenGenerator;

	@BeforeEach
	void setUp() {
		managementTokenGenerator = new ManagementTokenGenerator(new SecureRandom());
	}

	@Test
	void generate_shouldReturnUrlSafeRawToken_whenCalled() {
		// Arrange
		// (managementTokenGenerator constructed in setUp)

		// Act
		final ManagementToken managementToken = managementTokenGenerator.generate();

		// Assert
		assertThat(managementToken.rawToken())
				.isNotBlank()
				.doesNotContain("+", "/", "=");
	}

	@Test
	void generate_shouldReturnTokenHashMatchingSha256OfRawToken_whenCalled() throws NoSuchAlgorithmException {
		// Arrange
		// (managementTokenGenerator constructed in setUp)

		// Act
		final ManagementToken managementToken = managementTokenGenerator.generate();

		// Assert
		final MessageDigest digest = MessageDigest.getInstance("SHA-256");
		final byte[] expectedHashBytes = digest.digest(managementToken.rawToken().getBytes(StandardCharsets.UTF_8));
		final String expectedHash = HexFormat.of().formatHex(expectedHashBytes);
		assertThat(managementToken.tokenHash()).isEqualTo(expectedHash);
	}

	@Test
	void generate_shouldReturnUniqueRawTokens_whenCalledRepeatedly() {
		// Arrange
		final Set<String> rawTokens = new HashSet<>();

		// Act
		for (int i = 0; i < 1000; ++i) {
			rawTokens.add(managementTokenGenerator.generate().rawToken());
		}

		// Assert
		assertThat(rawTokens).hasSize(1000);
	}

}
