package com.commerce.common.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    // 기본 생성자에서 HTTP 상태 코드를 NOT_ACCEPTABLE로 설정
    public CustomException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.NOT_ACCEPTABLE; // 기본값을 406으로 설정
    }

    public CustomException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
