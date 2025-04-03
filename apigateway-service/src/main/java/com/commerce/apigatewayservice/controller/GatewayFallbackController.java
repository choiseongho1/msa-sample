package com.commerce.apigatewayservice.controller;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {
    private static final Logger log = LoggerFactory.getLogger(GatewayFallbackController.class);


    @GetMapping
    public Mono<ResponseEntity<String>> fallback(ServerWebExchange exchange) {
        Throwable exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        log.info("예외: {}", exception != null ? exception.getMessage() : "Unknown error");

        return Mono.just(exception)
            .map(ex -> {
                if (ex instanceof ResponseStatusException && ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    return new ResponseEntity<>("내부 서버 오류가 발생했습니다. 관리자에게 문의해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);
                } else if (ex instanceof TimeoutException) {
                    return new ResponseEntity<>("서비스 응답이 지연되었습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.GATEWAY_TIMEOUT);
                } else if (ex instanceof CallNotPermittedException) {
                    return new ResponseEntity<>("서비스가 현재 사용 불가능합니다. 잠시 후 다시 시도해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);
                } else {
                    return new ResponseEntity<>("내부 서버 오류가 발생했습니다. 관리자에게 문의해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);
                }
            })
            .defaultIfEmpty(new ResponseEntity<>("내부 서버 오류가 발생했습니다. 관리자에게 문의해 주세요.", HttpStatus.SERVICE_UNAVAILABLE));
    }
}