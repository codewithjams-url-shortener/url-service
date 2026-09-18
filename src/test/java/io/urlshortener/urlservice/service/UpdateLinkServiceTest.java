package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.exception.NoUpdatableFieldsProvidedException;
import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.generator.TokenHasher;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateLinkServiceTest {

	@Mock
	private LinkRepository linkRepository;

	private UpdateLinkService updateLinkService;

	@BeforeEach
	void setUp() {
		updateLinkService = new UpdateLinkService(linkRepository);
	}

	@Test
	void updateLink_shouldThrowNoUpdatableFieldsProvidedExceptionAndNotCallRepository_whenPatchHasNoChanges() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, false, null);

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", "raw-token", patch))
				.isInstanceOf(NoUpdatableFieldsProvidedException.class);
		verify(linkRepository, never()).update(any(), any(), any());
	}

	@Test
	void updateLink_shouldThrowManagementTokenMismatchException_whenRawTokenIsNull() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", null, patch))
				.isInstanceOf(ManagementTokenMismatchException.class);
	}

	@Test
	void updateLink_shouldThrowManagementTokenMismatchException_whenRawTokenIsBlank() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", "   ", patch))
				.isInstanceOf(ManagementTokenMismatchException.class);
	}

	@Test
	void updateLink_shouldNotCallRepository_whenRawTokenIsNull() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", null, patch))
				.isInstanceOf(ManagementTokenMismatchException.class);
		verify(linkRepository, never()).update(any(), any(), any());
	}

	@Test
	void updateLink_shouldCallRepositoryWithHashedToken_whenTokenAndPatchAreValid() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);
		final ShortLink updatedLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();
		given(linkRepository.update(eq("abc1234"), any(), eq(patch))).willReturn(updatedLink);

		// Act
		updateLinkService.updateLink("abc1234", "raw-token", patch);

		// Assert
		final ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
		verify(linkRepository).update(eq("abc1234"), hashCaptor.capture(), eq(patch));
		assertThat(hashCaptor.getValue()).isEqualTo(TokenHasher.hash("raw-token"));
	}

	@Test
	void updateLink_shouldReturnTheRepositoryResult_whenUpdateSucceeds() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);
		final ShortLink updatedLink = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();
		given(linkRepository.update(eq("abc1234"), any(), eq(patch))).willReturn(updatedLink);

		// Act
		final ShortLink result = updateLinkService.updateLink("abc1234", "raw-token", patch);

		// Assert
		assertThat(result).isSameAs(updatedLink);
	}

	@Test
	void updateLink_shouldPropagateShortLinkNotFoundException_whenRepositoryReportsNoSuchLink() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);
		given(linkRepository.update(eq("abc1234"), any(), eq(patch)))
				.willThrow(new ShortLinkNotFoundException("abc1234"));

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", "raw-token", patch))
				.isInstanceOf(ShortLinkNotFoundException.class);
	}

	@Test
	void updateLink_shouldPropagateManagementTokenMismatchException_whenRepositoryReportsAMismatch() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);
		given(linkRepository.update(eq("abc1234"), any(), eq(patch)))
				.willThrow(new ManagementTokenMismatchException("abc1234"));

		// Act & Assert
		assertThatThrownBy(() -> updateLinkService.updateLink("abc1234", "raw-token", patch))
				.isInstanceOf(ManagementTokenMismatchException.class);
	}

}
