package com.commerce.apigatewayservice.filter;

import brave.Span;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
@RequiredArgsConstructor
public class TraceFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 현재 활성화된 span 가져오기 또는 새로 생성
        final Span currentSpan = tracer.currentSpan() != null ?
            tracer.currentSpan() :
            tracer.newTrace().start();

        String traceId = currentSpan.context().traceIdString();
        String spanId = currentSpan.context().spanIdString();

        // B3 헤더 추가
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-B3-TraceId", traceId)
            .header("X-B3-SpanId", spanId)
            .header("X-B3-Sampled", "1")
            .build();

        return chain.filter(exchange.mutate().request(request).build())
            .doFinally(signalType -> {
                // 현재 스팬이 우리가 시작한 스팬인 경우에만 종료
                if (tracer.currentSpan() != null && tracer.currentSpan().context().spanId() == currentSpan.context().spanId()) {
                    currentSpan.finish();
                }
            });
    }

    @Override
    public int getOrder() {
        return -10; // LoggingFilter보다 먼저 실행되도록
    }
}