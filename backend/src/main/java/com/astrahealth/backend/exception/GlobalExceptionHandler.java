package com.astrahealth.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.astrahealth.backend.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
			DuplicateResourceException exception,
			HttpServletRequest request) {
		return createResponse(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return createResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
			NoResourceFoundException exception,
			HttpServletRequest request) {
		return createResponse(HttpStatus.NOT_FOUND, "The requested resource was not found.", request, Map.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(
			AccessDeniedException exception,
			HttpServletRequest request) {
		return createResponse(
				HttpStatus.FORBIDDEN,
				"You are not authorized to access this resource.",
				request,
				Map.of());
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
			InvalidRequestException exception,
			HttpServletRequest request) {
		return createResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
			InvalidCredentialsException exception,
			HttpServletRequest request) {
		return createResponse(
				HttpStatus.UNAUTHORIZED,
				"Invalid credentials or inactive account.",
				request,
				Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationFailure(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				validationErrors.putIfAbsent(
						error.getField(),
						error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage()));

		return createResponse(
				HttpStatus.BAD_REQUEST,
				"Request validation failed.",
				request,
				validationErrors);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
			Exception exception,
			HttpServletRequest request) {
		return createResponse(
				HttpStatus.BAD_REQUEST,
				"Request body or parameter is invalid.",
				request,
				Map.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataConflict(
			DataIntegrityViolationException exception,
			HttpServletRequest request) {
		return createResponse(
				HttpStatus.CONFLICT,
				"The operation conflicts with existing data constraints.",
				request,
				Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedError(
			Exception exception,
			HttpServletRequest request) {
		logger.error("Unexpected error while handling request to {}", request.getRequestURI(), exception);
		return createResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected server error occurred.",
				request,
				Map.of());
	}

	private ResponseEntity<ApiErrorResponse> createResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			Map<String, String> validationErrors) {
		ApiErrorResponse body = new ApiErrorResponse(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				validationErrors);
		return ResponseEntity.status(status).body(body);
	}
}
