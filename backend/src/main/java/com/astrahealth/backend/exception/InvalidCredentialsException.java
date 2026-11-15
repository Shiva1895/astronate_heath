package com.astrahealth.backend.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Authentication failed.");
	}
}
