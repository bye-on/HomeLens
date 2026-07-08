package com.homelens.common;

import lombok.Getter;

@Getter
public class ErrorResponse {
	private int code;
	private String status;
	private String message;
	
	public ErrorResponse(int code) {
		this.code = code;
	}

	public ErrorResponse(int code, String status) {
		this.code = code;
		this.status = status;
	}

	public ErrorResponse(int code, String status, String message) {
		this.code = code;
		this.status = status;
		this.message = message;
	}
}
