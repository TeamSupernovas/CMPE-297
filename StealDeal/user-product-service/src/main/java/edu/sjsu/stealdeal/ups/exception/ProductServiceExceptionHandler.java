package edu.sjsu.stealdeal.ups.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ProductServiceExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProductServiceError> handleConstraintViolationException(
			ConstraintViolationException exception) {
		Map<String, String> errors = exception.getConstraintViolations().stream().collect(Collectors
				.toMap(violation -> violation.getPropertyPath().toString(), violation -> violation.getMessage()));

		ProductServiceError productServiceError = ProductServiceError.builder().date(LocalDateTime.now())
				.message("Validation error ").errors(errors).build();
		return new ResponseEntity<ProductServiceError>(productServiceError, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ProductServiceError> handleResourceNotFoundException(ResourceNotFoundException exception) {

		ProductServiceError productServiceError = ProductServiceError.builder().date(LocalDateTime.now())
				.message(exception.getMessage()).build();
		return new ResponseEntity<ProductServiceError>(productServiceError, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProductServiceError> handleException(Exception exception) {
		exception.printStackTrace();
		ProductServiceError productServiceError = ProductServiceError.builder().date(LocalDateTime.now())
				.message(exception.getMessage()).build();
		return new ResponseEntity<ProductServiceError>(productServiceError, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
