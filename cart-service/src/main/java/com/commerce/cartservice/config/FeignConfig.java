package com.commerce.cartservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
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

            // Authorization 헤더가 존재하면 Feign 요청에 추가
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                requestTemplate.header("Authorization", authorizationHeader);
            }

            // 트레이싱 관련 헤더 추가 (X-B3-TraceId, X-B3-SpanId 등)
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName.toLowerCase().startsWith("x-b3-")) {
                    requestTemplate.header(headerName, request.getHeader(headerName));
                }
            }
        };
    }
}