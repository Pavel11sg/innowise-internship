package com.example.tasks.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex,
															   WebRequest request) {
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.NOT_FOUND.value(),
				"User not found",
				ex.getMessage(),
				request.getDescription(false).replace("uri=", "")
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ErrorResponseDto> handleDuplicateEmail(DuplicateEmailException ex,
																 WebRequest request) {
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.CONFLICT.value(),
				"Duplicate email",
				ex.getMessage(),
				request.getDescription(false).replace("uri=", "")
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(DuplicateCardException.class)
	public ResponseEntity<ErrorResponseDto> handleDuplicateCard(DuplicateCardException ex,
																WebRequest request) {
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.CONFLICT.value(),
				"Duplicate card",
				ex.getMessage(),
				request.getDescription(false).replace("uri=", "")
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(CardNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleCardNotFound(CardNotFoundException ex,
															   WebRequest request) {
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.NOT_FOUND.value(),
				"Card not found",
				ex.getMessage(),
				request.getDescription(false).replace("uri=", "")
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex,
																	   WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.BAD_REQUEST.value(),
				"Validation failed",
				errors.toString(),
				request.getDescription(false).replace("uri=", "")
		);
		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDto(
						HttpStatus.UNAUTHORIZED.value(),
						"INVALID_TOKEN",
						ex.getMessage(),
						request.getDescription(false)
				));
	}
}
