package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.generator.ManagementToken;
import io.urlshortener.urlservice.generator.ManagementTokenGenerator;
import io.urlshortener.urlservice.generator.ShortCodeGenerator;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateLinkServiceTest {

	@Mock
	private LinkRepository linkRepository;

	@Mock
	private ShortCodeGenerator shortCodeGenerator;

	@Mock
	private ManagementTokenGenerator managementTokenGenerator;

	private CreateLinkService createLinkService;

	@BeforeEach
	void setUp() {
		createLinkService = new CreateLinkService(linkRepository, shortCodeGenerator, managementTokenGenerator);
	}

	@Test
	void createLink_shouldUseGeneratedShortCode_whenNoCustomAliasIsProvided() {
		// Arrange
		final ShortLink link = ShortLink.builder().longUrl("https://example.com").build();
		given(shortCodeGenerator.generateShortCode())
				.willReturn("generated1");
		given(managementTokenGenerator.generate())
				.willReturn(new ManagementToken("raw-token", "token-hash"));
		given(linkRepository.save(any(ShortLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// Act
		createLinkService.createLink(link);

		// Assert
		verify(shortCodeGenerator).generateShortCode();
		assertThat(link.getShortCode()).isEqualTo("generated1");
	}

	@Test
	void createLink_shouldUseCustomAliasWithoutCallingGenerator_whenCustomAliasIsProvided() {
		// Arrange
		final ShortLink link = ShortLink.builder().longUrl("https://example.com").customAlias("myAlias").build();
		given(managementTokenGenerator.generate())
				.willReturn(new ManagementToken("raw-token", "token-hash"));
		given(linkRepository.save(any(ShortLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// Act
		createLinkService.createLink(link);

		// Assert
		verify(shortCodeGenerator, never()).generateShortCode();
		assertThat(link.getShortCode()).isEqualTo("myAlias");
	}

	@Test
	void createLink_shouldPopulateManagementTokenHashAndCreatedAt_beforeSavingTheLink() {
		// Arrange
		final ShortLink link = ShortLink.builder().longUrl("https://example.com").customAlias("myAlias").build();
		given(managementTokenGenerator.generate())
				.willReturn(new ManagementToken("raw-token", "token-hash"));
		given(linkRepository.save(any(ShortLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// Act
		createLinkService.createLink(link);

		// Assert
		final ArgumentCaptor<ShortLink> savedLinkCaptor = ArgumentCaptor.forClass(ShortLink.class);
		verify(linkRepository).save(savedLinkCaptor.capture());
		final ShortLink savedLink = savedLinkCaptor.getValue();
		assertThat(savedLink.getManagementTokenHash()).isEqualTo("token-hash");
		assertThat(savedLink.getCreatedAt()).isNotNull();
	}

	@Test
	void createLink_shouldReturnRawManagementTokenAlongsideSavedLink_whenSaveSucceeds() {
		// Arrange
		final ShortLink link = ShortLink.builder().longUrl("https://example.com").customAlias("myAlias").build();
		final ShortLink savedLink = ShortLink.builder()
				.shortCode("myAlias")
				.longUrl("https://example.com")
				.managementTokenHash("token-hash")
				.build();
		given(managementTokenGenerator.generate())
				.willReturn(new ManagementToken("raw-token", "token-hash"));
		given(linkRepository.save(any(ShortLink.class)))
				.willReturn(savedLink);

		// Act
		final CreateLinkResult result = createLinkService.createLink(link);

		// Assert
		assertThat(result.shortLink()).isSameAs(savedLink);
		assertThat(result.rawManagementToken()).isEqualTo("raw-token");
	}

	@Test
	void createLink_shouldPropagateAliasAlreadyExistsException_whenRepositorySaveDetectsAConflict() {
		// Arrange
		final ShortLink link = ShortLink.builder().longUrl("https://example.com").customAlias("myAlias").build();
		given(managementTokenGenerator.generate())
				.willReturn(new ManagementToken("raw-token", "token-hash"));
		given(linkRepository.save(any(ShortLink.class)))
				.willThrow(new AliasAlreadyExistsException(new RuntimeException("conflict"), "myAlias"));

		// Act & Assert
		assertThatThrownBy(() -> createLinkService.createLink(link))
				.isInstanceOf(AliasAlreadyExistsException.class);
	}

}
