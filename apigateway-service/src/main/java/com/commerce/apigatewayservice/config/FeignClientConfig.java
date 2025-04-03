package com.commerce.apigatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = RequestContextHolder.currentRequestAttributes()
                .getAttribute("TOKEN", RequestAttributes.SCOPE_REQUEST)
                .toString();
            if (token != null) {
                requestTemplate.header("Authorization", token);
            }
        };
    }
}