package com.homelens.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;
	
	public CustomException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
