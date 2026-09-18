package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetLinkServiceTest {

	@Mock
	private LinkRepository linkRepository;

	private GetLinkService getLinkService;

	@BeforeEach
	void setUp() {
		getLinkService = new GetLinkService(linkRepository);
	}

	@Test
	void getLink_shouldThrowShortLinkNotFoundException_whenNoLinkExistsForShortCode() {
		// Arrange
		given(linkRepository.findByShortCode("missing")).willReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> getLinkService.getLink("missing"))
				.isInstanceOf(ShortLinkNotFoundException.class);
	}

	@Test
	void getLink_shouldReturnLink_whenLinkHasNoExpiresAt() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();
		given(linkRepository.findByShortCode("abc1234")).willReturn(Optional.of(link));

		// Act
		final ShortLink result = getLinkService.getLink("abc1234");

		// Assert
		assertThat(result).isSameAs(link);
	}

	@Test
	void getLink_shouldReturnLink_whenExpiresAtIsInTheFuture() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
				.build();
		given(linkRepository.findByShortCode("abc1234")).willReturn(Optional.of(link));

		// Act
		final ShortLink result = getLinkService.getLink("abc1234");

		// Assert
		assertThat(result).isSameAs(link);
	}

	@Test
	void getLink_shouldThrowShortLinkNotFoundException_whenExpiresAtIsInThePast() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
				.expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
				.build();
		given(linkRepository.findByShortCode("abc1234")).willReturn(Optional.of(link));

		// Act & Assert
		assertThatThrownBy(() -> getLinkService.getLink("abc1234"))
				.isInstanceOf(ShortLinkNotFoundException.class);
	}

}
