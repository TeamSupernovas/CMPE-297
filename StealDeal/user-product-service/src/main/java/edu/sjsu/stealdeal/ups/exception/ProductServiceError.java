package edu.sjsu.stealdeal.ups.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductServiceError {
	
	private LocalDateTime date;
	private String message;
	private Map<String, String> errors;

}
