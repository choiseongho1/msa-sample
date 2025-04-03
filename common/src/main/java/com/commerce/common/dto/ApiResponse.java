package com.commerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(200)
            .status("OK")
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> error(int code, String status, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .status(status)
            .message(message)
            .build();
    }
}