package com.commerce.common.config;


import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CommonCircuitBreakerConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)  // 실패율 50% 이상이면 Circuit Breaker Open
            .slowCallRateThreshold(50) // 느린 호출 비율 50% 이상이면 Open
            .slowCallDurationThreshold(Duration.ofSeconds(2)) // 2초 이상이면 느린 호출
            .permittedNumberOfCallsInHalfOpenState(3) // Half-Open 상태에서 3개 요청 허용
            .waitDurationInOpenState(Duration.ofSeconds(10)) // Open 상태에서 10초 후 Half-Open 전환
            .slidingWindowSize(10) // 최근 10개 요청 기준으로 실패율 계산
            .minimumNumberOfCalls(5) // 최소 5번 호출 이후 Circuit Breaker 작동
            .build();

        return CircuitBreakerRegistry.of(config);
    }
}