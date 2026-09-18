package io.urlshortener.urlservice.mapper.responseMapper;

import io.urlshortener.urlservice.model.dataTransferObject.GetLinkResponse;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class GetLinkResponseMapperTest {

	private final GetLinkResponseMapper mapper = new GetLinkResponseMapper();

	@Test
	void toDto_shouldMapShortCodeLongUrlAndStatus_whenLinkHasAllFieldsPopulated() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com/path")
				.createdAt(Instant.now())
				.status("FLAGGED")
				.build();

		// Act
		final GetLinkResponse response = mapper.toDto(link);

		// Assert
		assertThat(response.getShortCode()).isEqualTo("abc1234");
		assertThat(response.getLongUrl()).isEqualTo(URI.create("https://example.com/path"));
		assertThat(response.getStatus()).isEqualTo("FLAGGED");
	}

	@Test
	void toDto_shouldConvertCreatedAtToUtcOffsetDateTime_whenLinkHasACreatedAtInstant() {
		// Arrange
		final Instant createdAt = Instant.parse("2030-01-01T00:00:00Z");
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(createdAt)
				.build();

		// Act
		final GetLinkResponse response = mapper.toDto(link);

		// Assert
		assertThat(response.getCreatedAt()).isEqualTo(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));
	}

	@Test
	void toDto_shouldConvertExpiresAtToUtcOffsetDateTime_whenLinkHasAnExpiresAtInstant() {
		// Arrange
		final Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.expiresAt(expiresAt)
				.build();

		// Act
		final GetLinkResponse response = mapper.toDto(link);

		// Assert
		assertThat(response.getExpiresAt()).isEqualTo(OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
	}

	@Test
	void toDto_shouldLeaveExpiresAtNull_whenLinkHasNoExpiresAt() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();

		// Act
		final GetLinkResponse response = mapper.toDto(link);

		// Assert
		assertThat(response.getExpiresAt()).isNull();
	}

}
