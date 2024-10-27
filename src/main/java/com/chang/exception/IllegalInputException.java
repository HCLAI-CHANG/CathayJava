package com.chang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalInputException extends RuntimeException{
	
	static final long serialVersionUID = 1L;

	public IllegalInputException() {

	}

	public IllegalInputException(String message) {
		super(message);
	}
}
