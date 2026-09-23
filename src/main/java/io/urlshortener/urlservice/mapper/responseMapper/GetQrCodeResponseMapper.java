package io.urlshortener.urlservice.mapper.responseMapper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Converts a raw QR code image into the outgoing {@link Resource} the generated controller interface expects.
 */
@Component
public class GetQrCodeResponseMapper implements ResponseMapper<byte[], Resource> {

	/**
	 * Wraps raw QR code image bytes as a {@link Resource}.
	 *
	 * @param qrCodeBytes the PNG-encoded QR code image bytes.
	 * @return the equivalent {@link Resource}.
	 */
	@Override
	public Resource toDto(final byte[] qrCodeBytes) {
		return new ByteArrayResource(qrCodeBytes);
	}

}
