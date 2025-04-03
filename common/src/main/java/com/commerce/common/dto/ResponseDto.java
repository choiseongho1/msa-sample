package com.commerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * explain the Class File
 *
 * @author : choiseongho
 * @fileName : ResponseDto.java
 * @since : 2025-02-19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto <T>{
    private Integer code;
    private T data;

    // OK 응답 (데이터 없음)
    public static ResponseDto<?> ok() {
        return new ResponseDto<>(HttpStatus.OK.value(), null);
    }

    // OK 응답 (데이터 있음)
    public static <T> ResponseDto<T> ok(T data) {
        return new ResponseDto<>(HttpStatus.OK.value(), data);
    }

    // 생성자 (HTTP 상태 코드와 데이터)
    public ResponseDto(HttpStatus httpStatus, T data) {
        this.code = httpStatus.value();
        this.data = data;
    }

    // 생성자 (데이터만)
    public ResponseDto(T data) {
        this.code = HttpStatus.OK.value();
        this.data = data;
    }
}
