package edu.sjsu.stealdeal.ups.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public ResourceNotFoundException(String message) {
		super(message);
		this.message = message;
	}

}
