package com.commerce.apigatewayservice.etc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class RateLimiterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void rateLimiter_should_allow_initial_requests_then_block() {
        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i <= 10; i++) {
            HttpStatusCode status = webTestClient.get()
                .uri("/product-service/product/rateLimit-test")
                .exchange()
                .returnResult(String.class)
                .getStatus();

            System.out.printf("요청 #%d → 상태: %s\n", i, status);

            if (status.is2xxSuccessful()) {
                successCount++;
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                failCount++;
            }

            try {
                Thread.sleep(200); // 0.2초 간격
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.printf("✅ 성공 요청 수: %d\n", successCount);
        System.out.printf("🚫 차단된 요청 수: %d\n", failCount);

        Assertions.assertTrue(failCount > 0, "429 Too Many Requests 가 발생해야 함");
    }
}
