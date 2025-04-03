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
class TimeoutTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void APIGateway_timeoutTest() {
        webTestClient.get()
            .uri("/product-service/product/delay")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
            .expectBody(String.class)
            .value(body -> assertThat(body).contains("서비스 응답이 지연되었습니다. 잠시 후 다시 시도해 주세요."));
    }
}
