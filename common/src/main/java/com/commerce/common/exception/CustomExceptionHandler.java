package com.commerce.common.exception;

import com.commerce.common.dto.ApiResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        log.info("Unexpected error occurred: ", e);  // 로그 추가
        ApiResponse<?> errorResponse = ApiResponse.error(
            e.getHttpStatus().value(),
            e.getHttpStatus().getReasonPhrase(),
            e.getMessage()
        );

        return ResponseEntity.status(e.getHttpStatus())
            .header("X-Error-Message", Base64.getEncoder().encodeToString(e.getMessage().getBytes(
                StandardCharsets.UTF_8)))  // Base64로 인코딩하여 헤더에 추가
            .body(errorResponse);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.info("Validation error occurred: ", ex);  // 로그 추가
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.debug("Validation error - field: {}, message: {}", fieldName, errorMessage);  // 로그 추가
        });

        ApiResponse<?> errorResponse = ApiResponse.<Map<String, String>>builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("입력값 검증에 실패했습니다")
            .data(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }


    // 모든 예외를 잡는 핸들러 추가
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex) {
        log.info("Unexpected error occurred: ", ex);  // 로그 추가
        ApiResponse<?> errorResponse = ApiResponse.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "서버 내부 오류가 발생했습니다"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

}