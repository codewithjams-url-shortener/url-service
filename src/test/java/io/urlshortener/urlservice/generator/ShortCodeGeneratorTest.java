package io.urlshortener.urlservice.generator;

import io.urlshortener.urlservice.property.UrlShortenerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

	private static final String BASE62_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private final SecureRandom secureRandom = new SecureRandom();

	private UrlShortenerProperties shortenerProperties;

	private ShortCodeGenerator shortCodeGenerator;

	@BeforeEach
	void setUp() {
		shortenerProperties = new UrlShortenerProperties();
		shortenerProperties.setShortCodeLength(7);
		shortCodeGenerator = new ShortCodeGenerator(secureRandom, shortenerProperties);
	}

	@Test
	void generateShortCode_shouldReturnCodeOfConfiguredLength_whenLengthIsSevenCharacters() {
		// Arrange
		// (shortenerProperties configured with length 7 in setUp)

		// Act
		final String shortCode = shortCodeGenerator.generateShortCode();

		// Assert
		assertThat(shortCode).hasSize(7);
	}

	@Test
	void generateShortCode_shouldUseOnlyBase62Characters_whenGeneratingACode() {
		// Arrange
		// (shortenerProperties configured with length 7 in setUp)

		// Act
		final String shortCode = shortCodeGenerator.generateShortCode();

		// Assert
		assertThat(shortCode).matches("^[" + BASE62_ALPHABET + "]+$");
	}

	@Test
	void generateShortCode_shouldReturnUniqueCodes_whenCalledRepeatedly() {
		// Arrange
		final Set<String> generatedCodes = new HashSet<>();

		// Act
		for (int i = 0; i < 1000; ++i) {
			generatedCodes.add(shortCodeGenerator.generateShortCode());
		}

		// Assert
		assertThat(generatedCodes).hasSize(1000);
	}

	@Test
	void generateShortCode_shouldReturnCodeOfReconfiguredLength_whenLengthIsChangedAtRuntime() {
		// Arrange
		shortenerProperties.setShortCodeLength(16);

		// Act
		final String shortCode = shortCodeGenerator.generateShortCode();

		// Assert
		assertThat(shortCode).hasSize(16);
	}

}
