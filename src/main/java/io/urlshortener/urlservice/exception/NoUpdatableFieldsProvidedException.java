package io.urlshortener.urlservice.exception;

/**
 * Thrown when a patch request supplies no fields to actually update.
 */
public class NoUpdatableFieldsProvidedException extends RuntimeException {

	/**
	 * Creates the exception with a message describing the empty patch.
	 *
	 * @param message description of the failure.
	 */
	public NoUpdatableFieldsProvidedException(String message) {
		super(message);
	}

}
