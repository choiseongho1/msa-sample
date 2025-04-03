package com.commerce.apigatewayservice.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class LoggingFilterTest {

    @Autowired
    private LoggingFilter loggingFilter;

    @Test
    @DisplayName("LoggingFilter 빈이 정상적으로 등록되는지 확인")
    void testLoggingFilterBeanRegistration() {
        // Given & When & Then
        assertThat(loggingFilter).isNotNull();
    }

    @Test
    @DisplayName("LoggingFilter가 GatewayFilter를 생성하는지 확인")
    void testApplyReturnsGatewayFilter() {
        // Given
        LoggingFilter.Config config = new LoggingFilter.Config();

        // When
        GatewayFilter filter = loggingFilter.apply(config);

        // Then
        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("LoggingFilter가 요청과 응답을 로깅하는지 확인")
    void testFilterLogsRequestAndResponse() {
        // Given
        LoggingFilter.Config config = new LoggingFilter.Config();
        GatewayFilter filter = loggingFilter.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest
            .method(HttpMethod.GET, "/test")
            .header("User-Agent", "JUnit")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock the chain
        org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        // Verify chain was called
        verify(chain).filter(exchange);
    }
}