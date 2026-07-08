package com.homelens.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.homelens.common.ErrorResponse;

@RestControllerAdvice
public class CustomExceptionHandler {
	// 설정한 CustomError들은 여기로...
	@ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getCode(), e.getMessage()));
    }
	
	// 설정하지 않은 오류들은 여기로...
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
	    return ResponseEntity.status(500)
	        .body(new ErrorResponse(500, e.getMessage()));
	}

}
