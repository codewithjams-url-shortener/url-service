package io.urlshortener.urlservice.mapper.responseMapper;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GetQrCodeResponseMapperTest {

	private final GetQrCodeResponseMapper mapper = new GetQrCodeResponseMapper();

	@Test
	void toDto_shouldWrapTheGivenBytes_whenGivenQrCodeBytes() throws IOException {
		// Arrange
		final byte[] qrCodeBytes = {1, 2, 3, 4};

		// Act
		final Resource resource = mapper.toDto(qrCodeBytes);

		// Assert
		assertThat(resource.getContentAsByteArray()).isEqualTo(qrCodeBytes);
	}

}
