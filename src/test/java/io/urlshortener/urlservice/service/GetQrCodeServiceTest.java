package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.generator.QrCodeGenerator;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.property.ShortUrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetQrCodeServiceTest {

	@Mock
	private GetLinkService getLinkService;

	@Mock
	private ShortUrlBuilder shortUrlBuilder;

	@Mock
	private QrCodeGenerator qrCodeGenerator;

	private GetQrCodeService getQrCodeService;

	@BeforeEach
	void setUp() {
		getQrCodeService = new GetQrCodeService(getLinkService, shortUrlBuilder, qrCodeGenerator);
	}

	@Test
	void getQrCode_shouldReturnGeneratorOutput_whenLinkExists() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();
		final byte[] pngBytes = {1, 2, 3};
		given(getLinkService.getLink("abc1234")).willReturn(link);
		given(shortUrlBuilder.build("abc1234")).willReturn(URI.create("https://shortify.com/abc1234"));
		given(qrCodeGenerator.generate("https://shortify.com/abc1234", 300)).willReturn(pngBytes);

		// Act
		final byte[] result = getQrCodeService.getQrCode("abc1234", 300);

		// Assert
		assertThat(result).isSameAs(pngBytes);
	}

	@Test
	void getQrCode_shouldEncodeTheResolvedShortUrl_whenLinkExists() {
		// Arrange
		final ShortLink link = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.build();
		given(getLinkService.getLink("abc1234")).willReturn(link);
		given(shortUrlBuilder.build("abc1234")).willReturn(URI.create("https://shortify.com/abc1234"));

		// Act
		getQrCodeService.getQrCode("abc1234", 500);

		// Assert
		verify(qrCodeGenerator).generate("https://shortify.com/abc1234", 500);
	}

	@Test
	void getQrCode_shouldPropagateShortLinkNotFoundException_whenLinkIsMissingOrExpired() {
		// Arrange
		given(getLinkService.getLink("missing")).willThrow(new ShortLinkNotFoundException("missing"));

		// Act & Assert
		assertThatThrownBy(() -> getQrCodeService.getQrCode("missing", 300))
				.isInstanceOf(ShortLinkNotFoundException.class);
	}

}
