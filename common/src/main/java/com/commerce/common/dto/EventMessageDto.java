package com.commerce.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessageDto<T> {
    private String eventId;
    private String eventType;
    private String source;
    private LocalDateTime timestamp;
    private T payload;
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private Boolean sampled;


    public static <T> EventMessageDto<T> of(String eventType, String source, T payload) {

        // 현재 스레드의 traceId 가져오기 (없으면 새로 생성)
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        return EventMessageDto.<T>builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .source(source)
            .timestamp(LocalDateTime.now())
            .payload(payload)
            .traceId(traceId)
            .build();
    }
}