package io.urlshortener.urlservice.exception;

import io.urlshortener.urlservice.model.dataTransferObject.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ControllerAdviceTest {

	private final ControllerAdvice controllerAdvice = new ControllerAdvice();

	@Test
	void handleValidationError_shouldReturn400_whenRequestBodyFailsValidation() {
		// Arrange
		final MethodArgumentNotValidException exception = methodArgumentNotValidException(
				new FieldError(
						"createLinkRequest",
						"customAlias",
						"size must be between 3 and 32"
				)
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleValidationError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void handleValidationError_shouldIncludeFieldNameAndViolation_whenSingleFieldFailsValidation() {
		// Arrange
		final MethodArgumentNotValidException exception = methodArgumentNotValidException(
				new FieldError(
						"createLinkRequest",
						"customAlias",
						"size must be between 3 and 32"
				)
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleValidationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("customAlias: size must be between 3 and 32");
	}

	@Test
	void handleValidationError_shouldJoinAllFieldViolations_whenMultipleFieldsFailValidation() {
		// Arrange
		final MethodArgumentNotValidException exception = methodArgumentNotValidException(
				new FieldError(
						"createLinkRequest",
						"longUrl",
						"must not be null"
				),
				new FieldError(
						"createLinkRequest",
						"customAlias",
						"must match \"^[a-zA-Z0-9]+$\""
				)
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleValidationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage())
				.isEqualTo("longUrl: must not be null, customAlias: must match \"^[a-zA-Z0-9]+$\"");
	}

	@Test
	void handleAliasAlreadyExistingError_shouldReturn409_whenAliasIsAlreadyInUse() {
		// Arrange
		final AliasAlreadyExistsException exception = new AliasAlreadyExistsException(
				new RuntimeException("conditional check failed"), "myAlias"
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleAliasAlreadyExistingError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void handleAliasAlreadyExistingError_shouldReturnExceptionMessage_whenAliasIsAlreadyInUse() {
		// Arrange
		final AliasAlreadyExistsException exception = new AliasAlreadyExistsException(
				new RuntimeException("conditional check failed"), "myAlias"
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleAliasAlreadyExistingError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("Alias 'myAlias' already exists");
	}

	@Test
	void handleInternalError_shouldReturn500_whenInvalidConversionInputExceptionIsThrown() {
		// Arrange
		final InvalidConversionInputException exception =
				new InvalidConversionInputException("Domain Model not provided");

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleInternalError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void handleInternalError_shouldReturn500_whenCorruptedDataExceptionIsThrown() {
		// Arrange
		final CorruptedDataException exception = new CorruptedDataException("Expecting value at createdAt");

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleInternalError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void handleInternalError_shouldNotLeakExceptionMessageInResponseBody_whenAnInternalExceptionIsThrown() {
		// Arrange
		final InvalidConversionInputException exception =
				new InvalidConversionInputException("Domain Model not provided");

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleInternalError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isNotEqualTo(exception.getMessage());
	}

	private MethodArgumentNotValidException methodArgumentNotValidException(final FieldError... fieldErrors) {
		final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createLinkRequest");
		for (final FieldError fieldError : fieldErrors) {
			bindingResult.addError(fieldError);
		}
		final MethodParameter methodParameter = mock(MethodParameter.class);
		return new MethodArgumentNotValidException(methodParameter, bindingResult);
	}

}
