package com.commerce.apigatewayservice.config;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class RateLimiterConfigTest {


    @Test
    @DisplayName("remoteAddressKeyResolver 빈이 정상적으로 등록되는지 확인")
    void testRemoteAddressKeyResolver() {
        // Given
        RateLimiterConfig config = new RateLimiterConfig();
        KeyResolver keyResolver = config.remoteAddressKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
            .remoteAddress(new InetSocketAddress("127.0.0.1", 8080)) // InetSocketAddress로 변환
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<String> result = keyResolver.resolve(exchange);

        // Then
        StepVerifier.create(result)
            .assertNext(key -> assertEquals("127.0.0.1", key))
            .verifyComplete();
    }
}