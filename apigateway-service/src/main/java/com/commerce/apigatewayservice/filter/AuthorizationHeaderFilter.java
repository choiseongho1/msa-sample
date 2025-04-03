package com.commerce.apigatewayservice.filter;

import com.commerce.apigatewayservice.security.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${jwt.token.secretKey}")
    private String secretKey;

    public AuthorizationHeaderFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Getter
    @Setter
    public static class Config {
        private List<String> excludePaths = new ArrayList<>();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. 제외 경로 체크
            if (isExcludedPath(path, config.getExcludePaths())) {
                return chain.filter(exchange);
            }

            // 2. Authorization 헤더 존재 확인
            if (!hasAuthorizationHeader(request)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            // 3. 토큰 추출 및 검증
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = extractJwtToken(authorizationHeader);

            try {
                // 4. JWT 유효성 검사
                if (isJwtValid(jwt)) {
                    return chain.filter(exchange);
                }
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);

            } catch (ExpiredJwtException e) {
                // 5. Access Token 만료 시 Refresh Token으로 재발급 처리
                return handleExpiredToken(exchange, request, chain);
            }
        };
    }

    // 제외 경로 체크 메소드
    private boolean isExcludedPath(String path, List<String> excludePaths) {
        return excludePaths.stream().anyMatch(path::startsWith);
    }

    // Authorization 헤더 체크 메소드
    private boolean hasAuthorizationHeader(ServerHttpRequest request) {
        return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    // JWT 토큰 추출 메소드
    private String extractJwtToken(String authorizationHeader) {
        return authorizationHeader.replace("Bearer ", "");
    }

    // 만료된 토큰 처리 메소드
    private Mono<Void> handleExpiredToken(ServerWebExchange exchange,
        ServerHttpRequest request,
        GatewayFilterChain chain) {
        String refreshToken = request.getHeaders().getFirst("RefreshToken");
        if (refreshToken == null) {
            return onError(exchange, "Refresh token not found", HttpStatus.UNAUTHORIZED);
        }

        return refreshAccessToken(exchange, refreshToken)
            .flatMap(newTokens -> updateTokensAndContinue(exchange, request, chain, newTokens))
            .onErrorResume(error -> {
                log.error("Token refresh failed: {}", error.getMessage());
                return onError(exchange, "Token refresh failed", HttpStatus.UNAUTHORIZED);
            });
    }

    // 새 토큰으로 요청/응답 업데이트 메소드
    private Mono<Void> updateTokensAndContinue(ServerWebExchange exchange,
        ServerHttpRequest request,
        GatewayFilterChain chain,
        TokenDto newTokens) {
        // 응답 헤더에 새 토큰 추가
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Authorization", "Bearer " + newTokens.getAccessToken());
        response.getHeaders().add("RefreshToken", newTokens.getRefreshToken());

        // 새 토큰으로 요청 헤더 업데이트
        ServerHttpRequest newRequest = request.mutate()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens.getAccessToken())
            .header("RefreshToken", newTokens.getRefreshToken())
            .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private boolean isJwtValid(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwt)
                    .getBody();

            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw e;  // 만료된 토큰은 재발급을 위해 예외를 그대로 던짐
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Mono<TokenDto> refreshAccessToken(ServerWebExchange exchange, String refreshToken) {
        return webClientBuilder.build()
                .post()
                .uri("lb://USER-SERVICE/api/v1/user/refresh")  // LoadBalancer 사용 시
                .header("RefreshToken", refreshToken)
                .retrieve()
                .bodyToMono(TokenDto.class)
                .onErrorResume(error -> {
                    log.error("Token refresh request failed: {}", error.getMessage());
                    return Mono.error(error);
                });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Authorization error: {}", err);
        return response.setComplete();
    }
}