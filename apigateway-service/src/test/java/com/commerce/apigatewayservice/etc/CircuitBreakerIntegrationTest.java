package com.commerce.apigatewayservice.etc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CircuitBreakerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void APIGateway_circuitBreakerTest() {
        // 실패를 유도할 라우트 호출 (예: product-service 꺼져있거나, 강제로 503 반환)
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                .uri("/product-service/product/error")
                .exchange()
                .expectStatus().is5xxServerError();
        }

        // 일정 횟수 이상 실패 후 → Circuit Breaker OPEN → fallback 동작 확인
        webTestClient.get()
            .uri("/product-service/product/error")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody(String.class)
            .value(body -> assertThat(body).contains("서비스가 현재 사용 불가능합니다"));
    }
}
