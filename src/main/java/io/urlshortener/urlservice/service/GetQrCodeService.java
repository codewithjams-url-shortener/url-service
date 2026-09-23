package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.generator.QrCodeGenerator;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.property.ShortUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Orchestrates generating a QR code for an existing short link's public URL. Reuses {@link GetLinkService}, so this is
 * subject to the same expiry rules as {@code GET} — an expired link's QR code is treated as not found, the same as
 * fetching its metadata directly would be.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetQrCodeService {

	/**
	 * Looks up the link, including expiry checking.
	 */
	private final GetLinkService getLinkService;

	/**
	 * Resolves the link's short code into its public short URL.
	 */
	private final ShortUrlBuilder shortUrlBuilder;

	/**
	 * Renders the short URL as a QR code image.
	 */
	private final QrCodeGenerator qrCodeGenerator;

	/**
	 * Generates a QR code encoding an existing short link's public URL.
	 *
	 * @param shortCode the short code to look up.
	 * @param size      the width and height of the generated image, in pixels.
	 * @return the PNG-encoded QR code image bytes.
	 * @throws io.urlshortener.urlservice.exception.ShortLinkNotFoundException if no link exists for that short code, or it exists but has expired.
	 */
	public byte[] getQrCode(final String shortCode, final int size) {
		final ShortLink shortLink = getLinkService.getLink(shortCode);
		final URI uri = shortUrlBuilder.build(shortLink.getShortCode());
		return qrCodeGenerator.generate(uri.toString(), size);
	}

}
