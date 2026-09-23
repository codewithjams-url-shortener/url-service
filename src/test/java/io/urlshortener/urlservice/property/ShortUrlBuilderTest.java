package io.urlshortener.urlservice.property;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlBuilderTest {

	@Test
	void build_shouldResolveShortCodeAgainstBaseUrl_whenBaseUrlHasTrailingSlash() {
		// Arrange
		final UrlShortenerProperties shortenerProperties = new UrlShortenerProperties();
		shortenerProperties.setBaseUrl("https://shortify.com/");
		final ShortUrlBuilder shortUrlBuilder = new ShortUrlBuilder(shortenerProperties);

		// Act
		final URI shortUrl = shortUrlBuilder.build("abc1234");

		// Assert
		assertThat(shortUrl).isEqualTo(URI.create("https://shortify.com/abc1234"));
	}

	@Test
	void build_shouldResolveShortCodeCorrectly_whenBaseUrlHasNoTrailingSlash() {
		// Arrange
		final UrlShortenerProperties shortenerProperties = new UrlShortenerProperties();
		shortenerProperties.setBaseUrl("https://shortify.com");
		final ShortUrlBuilder shortUrlBuilder = new ShortUrlBuilder(shortenerProperties);

		// Act
		final URI shortUrl = shortUrlBuilder.build("abc1234");

		// Assert
		assertThat(shortUrl.toString()).endsWith("/abc1234");
	}

}
