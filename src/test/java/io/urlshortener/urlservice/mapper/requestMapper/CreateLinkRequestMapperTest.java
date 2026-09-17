package io.urlshortener.urlservice.mapper.requestMapper;

import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkRequest;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateLinkRequestMapperTest {

	private final CreateLinkRequestMapper mapper = new CreateLinkRequestMapper();

	@Test
	void toDomain_shouldThrowInvalidConversionInputException_whenRequestBodyIsNull() {
		// Arrange
		// (null is the input under test)

		// Act & Assert
		assertThatThrownBy(() -> mapper.toDomain(null))
				.isInstanceOf(InvalidConversionInputException.class);
	}

	@Test
	void toDomain_shouldMapAllFields_whenRequestBodyHasCustomAliasAndExpiresAt() {
		// Arrange
		final OffsetDateTime expiresAt = OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		final CreateLinkRequest dto = CreateLinkRequest.builder()
				.longUrl(URI.create("https://example.com/path?query=value"))
				.customAlias("myAlias")
				.expiresAt(expiresAt)
				.build();

		// Act
		final ShortLink domain = mapper.toDomain(dto);

		// Assert
		assertThat(domain.getLongUrl()).isEqualTo("https://example.com/path?query=value");
		assertThat(domain.getCustomAlias()).isEqualTo("myAlias");
		assertThat(domain.getExpiresAt()).isEqualTo(expiresAt.toInstant());
	}

	@Test
	void toDomain_shouldLeaveExpiresAtNull_whenRequestBodyHasNoExpiresAt() {
		// Arrange
		final CreateLinkRequest dto = CreateLinkRequest.builder()
				.longUrl(URI.create("https://example.com"))
				.build();

		// Act
		final ShortLink domain = mapper.toDomain(dto);

		// Assert
		assertThat(domain.getExpiresAt()).isNull();
	}

	@Test
	void toDomain_shouldLeaveCustomAliasNull_whenRequestBodyHasNoCustomAlias() {
		// Arrange
		final CreateLinkRequest dto = CreateLinkRequest.builder()
				.longUrl(URI.create("https://example.com"))
				.build();

		// Act
		final ShortLink domain = mapper.toDomain(dto);

		// Assert
		assertThat(domain.getCustomAlias()).isNull();
	}

	@Test
	void toDomain_shouldLeaveShortCodeCreatedAtAndManagementTokenHashUnset_whenMappingARequestBody() {
		// Arrange
		final CreateLinkRequest dto = CreateLinkRequest.builder()
				.longUrl(URI.create("https://example.com"))
				.build();

		// Act
		final ShortLink domain = mapper.toDomain(dto);

		// Assert
		assertThat(domain.getShortCode()).isNull();
		assertThat(domain.getCreatedAt()).isNull();
		assertThat(domain.getManagementTokenHash()).isNull();
	}

	@Test
	void toDomain_shouldPreserveUrlExactly_whenUrlHasSchemeHostQueryAndFragment() {
		// Arrange
		final String fullUrl = "https://user:pass@example.com:8443/a/b?x=1&y=2#frag";
		final CreateLinkRequest dto = CreateLinkRequest.builder()
				.longUrl(URI.create(fullUrl))
				.build();

		// Act
		final ShortLink domain = mapper.toDomain(dto);

		// Assert
		assertThat(domain.getLongUrl()).isEqualTo(fullUrl);
	}

}
