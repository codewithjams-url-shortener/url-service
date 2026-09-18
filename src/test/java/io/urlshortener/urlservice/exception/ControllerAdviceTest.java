package io.urlshortener.urlservice.exception;

import io.urlshortener.urlservice.model.dataTransferObject.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerAdviceTest {

	private final ControllerAdvice controllerAdvice = new ControllerAdvice();

	@Test
	void handleMethodArgumentValidationError_shouldReturn400_whenRequestBodyFailsValidation() {
		// Arrange
		final MethodArgumentNotValidException exception = methodArgumentNotValidException(
				new FieldError(
						"createLinkRequest",
						"customAlias",
						"size must be between 3 and 32"
				)
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleMethodArgumentValidationError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void handleMethodArgumentValidationError_shouldIncludeFieldNameAndViolation_whenSingleFieldFailsValidation() {
		// Arrange
		final MethodArgumentNotValidException exception = methodArgumentNotValidException(
				new FieldError(
						"createLinkRequest",
						"customAlias",
						"size must be between 3 and 32"
				)
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleMethodArgumentValidationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("customAlias: size must be between 3 and 32");
	}

	@Test
	void handleMethodArgumentValidationError_shouldJoinAllFieldViolations_whenMultipleFieldsFailValidation() {
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
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleMethodArgumentValidationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage())
				.isEqualTo("longUrl: must not be null, customAlias: must match \"^[a-zA-Z0-9]+$\"");
	}

	@Test
	void handleConstraintViolationError_shouldReturn400_whenPathParameterFailsValidation() {
		// Arrange
		final ConstraintViolationException exception = constraintViolationException(
				constraintViolation("getLink.shortCode", "size must be between 3 and 2147483647")
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleConstraintViolationError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void handleConstraintViolationError_shouldStripMethodNamePrefixFromField_whenPropertyPathIncludesMethodName() {
		// Arrange
		final ConstraintViolationException exception = constraintViolationException(
				constraintViolation("getLink.shortCode", "size must be between 3 and 2147483647")
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleConstraintViolationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("shortCode: size must be between 3 and 2147483647");
	}

	@Test
	void handleConstraintViolationError_shouldUsePropertyPathAsIs_whenPropertyPathHasNoMethodNamePrefix() {
		// Arrange
		final ConstraintViolationException exception = constraintViolationException(
				constraintViolation("shortCode", "size must be between 3 and 2147483647")
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleConstraintViolationError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("shortCode: size must be between 3 and 2147483647");
	}

	@Test
	void handleConstraintViolationError_shouldJoinAllViolations_whenMultipleParametersFailValidation() {
		// Arrange
		final ConstraintViolationException exception = constraintViolationException(
				constraintViolation("getLink.shortCode", "size must be between 3 and 2147483647"),
				constraintViolation("getLink.status", "must not be blank")
		);

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleConstraintViolationError(exception);

		// Assert
		// ConstraintViolationException does not preserve insertion order of its violations, so this
		// asserts both entries are present rather than a fixed concatenation order.
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage())
				.contains("shortCode: size must be between 3 and 2147483647")
				.contains("status: must not be blank")
				.contains(", ");
	}

	@Test
	void handleNoUpdatableFieldProvidedError_shouldReturn400_whenPatchHasNoChanges() {
		// Arrange
		final NoUpdatableFieldsProvidedException exception =
				new NoUpdatableFieldsProvidedException("No Fields provided for update");

		// Act
		final ResponseEntity<ErrorResponse> response =
				controllerAdvice.handleNoUpdatableFieldProvidedError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void handleNoUpdatableFieldProvidedError_shouldReturnExceptionMessage_whenPatchHasNoChanges() {
		// Arrange
		final NoUpdatableFieldsProvidedException exception =
				new NoUpdatableFieldsProvidedException("No Fields provided for update");

		// Act
		final ResponseEntity<ErrorResponse> response =
				controllerAdvice.handleNoUpdatableFieldProvidedError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("No Fields provided for update");
	}

	@Test
	void handleManagementTokenMismatchError_shouldReturn403_whenTokenDoesNotMatch() {
		// Arrange
		final ManagementTokenMismatchException exception = new ManagementTokenMismatchException("abc1234");

		// Act
		final ResponseEntity<ErrorResponse> response =
				controllerAdvice.handleManagementTokenMismatchError(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void handleManagementTokenMismatchError_shouldReturnExceptionMessage_whenTokenDoesNotMatch() {
		// Arrange
		final ManagementTokenMismatchException exception = new ManagementTokenMismatchException("abc1234");

		// Act
		final ResponseEntity<ErrorResponse> response =
				controllerAdvice.handleManagementTokenMismatchError(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage())
				.isEqualTo("Management Token did not match for Short Code: abc1234");
	}

	@Test
	void handleNotFound_shouldReturn404_whenShortLinkIsNotFound() {
		// Arrange
		final ShortLinkNotFoundException exception = new ShortLinkNotFoundException("missing");

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleNotFound(exception);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void handleNotFound_shouldReturnExceptionMessage_whenShortLinkIsNotFound() {
		// Arrange
		final ShortLinkNotFoundException exception = new ShortLinkNotFoundException("missing");

		// Act
		final ResponseEntity<ErrorResponse> response = controllerAdvice.handleNotFound(exception);

		// Assert
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("Short Link: missing not found");
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

	private ConstraintViolationException constraintViolationException(final ConstraintViolation<?>... violations) {
		final Set<ConstraintViolation<?>> violationSet = new LinkedHashSet<>(Arrays.asList(violations));
		return new ConstraintViolationException(violationSet);
	}

	@SuppressWarnings("unchecked")
	private ConstraintViolation<?> constraintViolation(final String propertyPath, final String message) {
		final ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
		final Path path = mock(Path.class);
		when(path.toString()).thenReturn(propertyPath);
		when(violation.getPropertyPath()).thenReturn(path);
		when(violation.getMessage()).thenReturn(message);
		return violation;
	}

}
