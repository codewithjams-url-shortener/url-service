package io.urlshortener.urlservice.exception;

import io.urlshortener.urlservice.model.dataTransferObject.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Translates domain exceptions raised by controllers into HTTP error responses.
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

	/**
	 * Maps a request body validation failure (e.g. a field violating an OpenAPI-declared constraint)
	 * to a structured error response instead of Spring's default error body.
	 *
	 * @param e the validation exception raised when the request body fails bean validation.
	 * @return a {@code 400 Bad Request} response listing each invalid field and its violation.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationError(final MethodArgumentNotValidException e) {
		final String message = e.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(fieldError -> "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
				.collect(Collectors.joining(", "));
		log.atError()
				.addKeyValue("reason", message)
				.log("Request validation failed");
		final ErrorResponse response = ErrorResponse.builder().message(message).build();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	/**
	 * Maps an alias conflict to an HTTP conflict response.
	 *
	 * @param e the alias-conflict exception raised by a controller.
	 * @return a {@code 409 Conflict} response carrying the exception's message.
	 */
	@ExceptionHandler(AliasAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleAliasAlreadyExistingError(final AliasAlreadyExistsException e) {
		log.atError()
				.setCause(e)
				.addKeyValue("shortCode", e.getShortCode())
				.log("Alias in conflict");
		final ErrorResponse response = ErrorResponse.builder().message(e.getMessage()).build();
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	/**
	 * Maps an internal invariant violation (bad conversion input or corrupted persisted data) to a
	 * generic server error response. The real message is logged, but kept out of the response body
	 * to avoid leaking internal details to API callers.
	 *
	 * @param e the internal exception raised by a controller.
	 * @return a {@code 500 Internal Server Error} response with a generic error message.
	 */
	@ExceptionHandler({InvalidConversionInputException.class, CorruptedDataException.class})
	public ResponseEntity<ErrorResponse> handleInternalError(final RuntimeException e) {
		log.atError()
				.setCause(e)
				.addKeyValue("reason", e.getMessage())
				.log("Unexpected internal error");
		final ErrorResponse response = ErrorResponse.builder().message("An internal error occurred").build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
