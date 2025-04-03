package com.commerce.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class TraceIdLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String B3_TRACE_HEADER = "X-B3-TraceId";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // 먼저 X-Trace-Id 헤더 확인
        String traceId = request.getHeader(TRACE_HEADER);

        // 없으면 X-B3-TraceId 헤더 확인 (API Gateway에서 전달된 경우)
        if (traceId == null) {
            traceId = request.getHeader(B3_TRACE_HEADER);
        }

        // 둘 다 없으면 새로 생성
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        // MDC에 traceId 설정 및 응답 헤더에 추가
        MDC.put("traceId", traceId);
        response.setHeader(TRACE_HEADER, traceId);
        response.setHeader(B3_TRACE_HEADER, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        long startTime = System.currentTimeMillis();

        LocalDateTime startDateTime = LocalDateTime.now();

        // API 호출 시작 로깅 - API Gateway와 형식 통일
        log.info("[{}] [API] START {} [traceId: {}]", method, uri, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            LocalDateTime endDateTime = LocalDateTime.now();
            String formattedEndTime = endDateTime.format(formatter);

            // API 호출 종료 로깅 - API Gateway와 형식 통일
            log.info("[{}] [API] END {} {} → {} [traceId: {}] ({}ms)",
                formattedEndTime, method, uri, status, traceId, duration);

            MDC.remove("traceId");
        }
    }
}