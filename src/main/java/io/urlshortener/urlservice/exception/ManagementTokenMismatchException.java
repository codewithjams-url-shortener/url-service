package io.urlshortener.urlservice.exception;

import lombok.Getter;

/**
 * Thrown when a management operation's supplied token (or its absence) does not match the
 * target link's stored management token hash.
 */
public class ManagementTokenMismatchException extends RuntimeException {

	/**
	 * The short code of the link the mismatched token was presented against.
	 */
	@Getter
	private final String shortCode;

	/**
	 * Creates the exception for a short code whose management token did not match.
	 *
	 * @param shortCode the short code the mismatched token was presented against.
	 */
	public ManagementTokenMismatchException(final String shortCode) {
		super("Management Token did not match for Short Code: %s".formatted(shortCode));
		this.shortCode = shortCode;
	}

}
