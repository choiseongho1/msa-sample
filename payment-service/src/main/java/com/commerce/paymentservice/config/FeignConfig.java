package com.commerce.paymentservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 현재 HTTP 요청의 Authorization 헤더 가져오기
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String authorizationHeader = request.getHeader("Authorization");

            // Authorization 헤더가 존재하면 Feign 요청에 추가
            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                requestTemplate.header("Authorization", authorizationHeader);
            }
        };
    }
}