package io.urlshortener.urlservice.exception;

import lombok.Getter;

/**
 * Thrown when no link exists for a given short code, or the link exists but has expired.
 */
public class ShortLinkNotFoundException extends RuntimeException {

	/**
	 * The short code that could not be resolved to a link.
	 */
	@Getter
	private final String shortCode;

	/**
	 * Creates the exception for a short code that could not be resolved to a link.
	 *
	 * @param shortCode the short code that could not be resolved.
	 */
	public ShortLinkNotFoundException(final String shortCode) {
		super("Short Link: %s not found".formatted(shortCode));
		this.shortCode = shortCode;
	}

}
