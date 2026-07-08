package com.homelens.common;

import lombok.Getter;

@Getter
public class SuccessResponse<T> {
	private String message;
	
	private T data;
	
	public SuccessResponse(String message) {
		this.message = message;
	}

	public SuccessResponse(String message, T data) {
		this.message = message;
		this.data = data;
	}

}
