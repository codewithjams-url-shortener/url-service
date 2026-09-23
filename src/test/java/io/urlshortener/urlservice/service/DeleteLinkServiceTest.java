package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.generator.TokenHasher;
import io.urlshortener.urlservice.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteLinkServiceTest {

	@Mock
	private LinkRepository linkRepository;

	private DeleteLinkService deleteLinkService;

	@BeforeEach
	void setUp() {
		deleteLinkService = new DeleteLinkService(linkRepository);
	}

	@Test
	void deleteLink_shouldThrowManagementTokenMismatchExceptionAndNotCallRepository_whenRawTokenIsNull() {
		// Arrange
		// (no repository stubbing needed, since the guard should short-circuit)

		// Act & Assert
		assertThatThrownBy(() -> deleteLinkService.deleteLink("abc1234", null))
				.isInstanceOf(ManagementTokenMismatchException.class);
		verify(linkRepository, never()).delete(any(), any());
	}

	@Test
	void deleteLink_shouldThrowManagementTokenMismatchExceptionAndNotCallRepository_whenRawTokenIsBlank() {
		// Arrange
		// (no repository stubbing needed, since the guard should short-circuit)

		// Act & Assert
		assertThatThrownBy(() -> deleteLinkService.deleteLink("abc1234", "   "))
				.isInstanceOf(ManagementTokenMismatchException.class);
		verify(linkRepository, never()).delete(any(), any());
	}

	@Test
	void deleteLink_shouldCallRepositoryWithHashedToken_whenTokenIsValid() {
		// Arrange
		willDoNothing().given(linkRepository).delete(eq("abc1234"), any());

		// Act
		deleteLinkService.deleteLink("abc1234", "raw-token");

		// Assert
		final ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
		verify(linkRepository).delete(eq("abc1234"), hashCaptor.capture());
		assertThat(hashCaptor.getValue()).isEqualTo(TokenHasher.hash("raw-token"));
	}

	@Test
	void deleteLink_shouldPropagateShortLinkNotFoundException_whenRepositoryReportsNoSuchLink() {
		// Arrange
		willThrow(new ShortLinkNotFoundException("abc1234"))
				.given(linkRepository).delete(eq("abc1234"), any());

		// Act & Assert
		assertThatThrownBy(() -> deleteLinkService.deleteLink("abc1234", "raw-token"))
				.isInstanceOf(ShortLinkNotFoundException.class);
	}

	@Test
	void deleteLink_shouldPropagateManagementTokenMismatchException_whenRepositoryReportsAMismatch() {
		// Arrange
		willThrow(new ManagementTokenMismatchException("abc1234"))
				.given(linkRepository).delete(eq("abc1234"), any());

		// Act & Assert
		assertThatThrownBy(() -> deleteLinkService.deleteLink("abc1234", "raw-token"))
				.isInstanceOf(ManagementTokenMismatchException.class);
	}

}
