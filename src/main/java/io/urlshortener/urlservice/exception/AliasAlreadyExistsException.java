package io.urlshortener.urlservice.exception;

import lombok.Getter;

/**
 * Thrown when a requested short code or custom alias is already in use by an existing link.
 */
public class AliasAlreadyExistsException extends RuntimeException {

	/**
	 * The short code that was already in use.
	 */
	@Getter
	private final String shortCode;

	/**
	 * Creates the exception for a conflicting short code, with a message describing it.
	 *
	 * @param cause     the underlying error that revealed the conflict (e.g. a conditional-write failure).
	 * @param shortCode the short code that was already in use.
	 */
	public AliasAlreadyExistsException(Throwable cause, String shortCode) {
		super("Alias '%s' already exists".formatted(shortCode), cause);
		this.shortCode = shortCode;
	}

}
