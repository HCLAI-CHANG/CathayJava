package com.chang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicatedNameException extends RuntimeException{
	
	static final long serialVersionUID = 1L;

	public DuplicatedNameException() {

	}

	public DuplicatedNameException(String message) {
		super(message);
	}
}
