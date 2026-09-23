package io.urlshortener.urlservice.mapper.responseMapper;

import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkResponse;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
import io.urlshortener.urlservice.property.ShortUrlBuilder;
import io.urlshortener.urlservice.property.UrlShortenerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CreateLinkResponseMapperTest {

	private UrlShortenerProperties shortenerProperties;

	private CreateLinkResponseMapper mapper;

	@BeforeEach
	void setUp() {
		shortenerProperties = new UrlShortenerProperties();
		shortenerProperties.setBaseUrl("https://shortify.com/");
		mapper = new CreateLinkResponseMapper(new ShortUrlBuilder(shortenerProperties));
	}

	@Test
	void toDto_shouldMapShortCodeAndRawManagementToken_whenResultHasAShortLinkAndRawToken() {
		// Arrange
		final ShortLink shortLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.managementTokenHash("hashed-value")
				.build();
		final CreateLinkResult result = new CreateLinkResult(shortLink, "raw-token-value");

		// Act
		final CreateLinkResponse response = mapper.toDto(result);

		// Assert
		assertThat(response.getShortCode()).isEqualTo("abc1234");
		assertThat(response.getManagementToken()).isEqualTo("raw-token-value");
	}

	@Test
	void toDto_shouldReturnRawTokenNotHash_whenResultCarriesBoth() {
		// Arrange
		final ShortLink shortLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.managementTokenHash("hashed-value")
				.build();
		final CreateLinkResult result = new CreateLinkResult(shortLink, "raw-token-value");

		// Act
		final CreateLinkResponse response = mapper.toDto(result);

		// Assert
		assertThat(response.getManagementToken()).isNotEqualTo(shortLink.getManagementTokenHash());
	}

	@Test
	void toDto_shouldResolveShortUrlAgainstBaseUrl_whenBaseUrlHasTrailingSlash() {
		// Arrange
		shortenerProperties.setBaseUrl("https://shortify.com/");
		final ShortLink shortLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();

		// Act
		final CreateLinkResponse response = mapper.toDto(new CreateLinkResult(shortLink, "token"));

		// Assert
		assertThat(response.getShortUrl()).isEqualTo(URI.create("https://shortify.com/abc1234"));
	}

	@Test
	void toDto_shouldResolveShortUrlCorrectly_whenBaseUrlHasNoTrailingSlash() {
		// Arrange
		shortenerProperties.setBaseUrl("https://shortify.com");
		final ShortLink shortLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();

		// Act
		final CreateLinkResponse response = mapper.toDto(new CreateLinkResult(shortLink, "token"));

		// Assert
		assertThat(response.getShortUrl().toString()).endsWith("/abc1234");
	}

}
