package com.commerce.orderservice.feign;

import com.commerce.orderservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Order-Service에서 타 서비스로 feign client를 요청하기 위한 클래스
 *
 * @author : choiseongho
 * @fileName : OrderFeignClient.java
 * @since : 2025-01-09
 */
@FeignClient(name = "user-service", url="${api.apigateway}/user-service", configuration = FeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/user/validate/{userId}")
    boolean validateUser(@PathVariable("userId") String userId);
}