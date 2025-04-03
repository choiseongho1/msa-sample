package com.commerce.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String TRACE_HEADER = "X-Trace-Id";

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            HttpMethod httpMethod = request.getMethod();
            String method = (httpMethod != null) ? httpMethod.name() : "UNKNOWN";
            String path = request.getURI().getPath();

            // traceId 가져오기 및 없을 경우 생성
            String traceId = request.getHeaders().getFirst(TRACE_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
                // 새로 생성한 traceId를 요청에 추가
                request = request.mutate()
                    .header(TRACE_HEADER, traceId)
                    .header("X-B3-TraceId", traceId) // Spring Cloud Sleuth 호환성 유지
                    .build();
                // 변경된 요청으로 새 exchange 생성
                final ServerWebExchange finalExchange = exchange.mutate().request(request).build();
                exchange = finalExchange;
            }

            // 람다에서 사용할 final 변수
            final String finalTraceId = traceId;
            final ServerWebExchange finalExchange = exchange;

            LocalDateTime startDateTime = LocalDateTime.now();
            String startTime = startDateTime.format(formatter);

            // API 호출 시작 로깅
            log.info("[{}] [API] START {} {} [traceId={}]", startTime, method, path, finalTraceId);

            long startTimeMillis = System.currentTimeMillis();

            return chain.filter(finalExchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = finalExchange.getResponse();
                long duration = System.currentTimeMillis() - startTimeMillis;

                int statusCode = (response.getStatusCode() != null) ? response.getStatusCode().value() : 0;

                LocalDateTime endDateTime = LocalDateTime.now();
                String endTime = endDateTime.format(formatter);

                // API 호출 종료 로깅
                log.info("[{}] [API] END {} {} → {} [traceId={}] ({}ms)",
                    endTime, method, path, statusCode, finalTraceId, duration);
            }));
        }, Ordered.HIGHEST_PRECEDENCE);
    }

    @Data
    public static class Config {
        // 향후 로그 레벨 설정 등 확장 가능
    }
}