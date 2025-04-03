package com.commerce.common.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventConsumer {

    private final ObjectMapper objectMapper;

    /**
     * 이벤트 메시지를 처리하는 추상 메서드입니다.
     * 구현 클래스에서 이벤트 타입에 따른 처리 로직을 구현해야 합니다.
     */
    protected abstract void processEvent(EventMessageDto<?> eventMessage);

    /**
     * 특정 페이로드 타입의 메시지를 처리합니다.
     * 제네릭 타입 파라미터를 사용하여 페이로드를 원하는 타입으로 변환합니다.
     */
    protected <T> void processMessage(String message, Class<T> payloadClass) {
        if (message == null) {
            log.warn("수신된 메시지가 null입니다.");
            return;
        }

        try {
            JavaType type = objectMapper.getTypeFactory().constructParametricType(EventMessageDto.class, payloadClass);
            EventMessageDto<T> eventMessage = objectMapper.readValue(message, type);

            String traceId = eventMessage.getTraceId();
            String spanId = eventMessage.getSpanId();
            log.debug("메시지 수신: eventType={}, eventId={}, traceId={}, spanId={}",
                eventMessage.getEventType(), eventMessage.getEventId(), traceId, spanId);

            // 추적 컨텍스트 설정
            setTraceContext(eventMessage);

            try {
                // 이벤트 처리
                handleEvent(eventMessage);
                log.info("메시지 처리 성공: eventType={}, eventId={}, traceId={}, spanId={}",
                    eventMessage.getEventType(), eventMessage.getEventId(), traceId, spanId);
            } catch (Exception e) {
                log.error("메시지 처리 실패: eventType={}, eventId={}, traceId={}, spanId={}, error={}",
                    eventMessage.getEventType(), eventMessage.getEventId(), traceId, spanId, e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("메시지 역직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 이벤트 메시지를 처리합니다.
     * 이 메서드는 handleEvent 메서드를 호출하여 실제 처리를 위임합니다.
     */
    protected <T> void handleEvent(EventMessageDto<T> eventMessage) {
        processEvent(eventMessage);
    }

    /**
     * Kafka 토픽에서 메시지를 수신하는 리스너 메서드입니다.
     * 이 메서드는 자식 클래스에서 오버라이드할 수 있습니다.
     */
    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${kafka.consumer.group-id}")
    public void consume(String message) {
        if (message == null || message.trim().isEmpty() || "null".equalsIgnoreCase(message.trim())) {
            log.warn("KafkaNull 또는 유효하지 않은 메시지 수신됨. 무시합니다.");
            return;
        }

        consumeInternal(message);
    }
    /**
     * 내부 메시지 처리 로직입니다.
     */
    private void consumeInternal(String message) {
        brave.Span kafkaSpan = null;

        if (message == null || message.isEmpty() || "null".equals(message)) {
            log.warn("수신된 메시지가 null 또는 빈 문자열입니다.");
            return;
        }

        try {
            if (message.startsWith("\"{") && message.endsWith("}\"")) {
                message = objectMapper.readValue(message, String.class);
            }

            final EventMessageDto<?> eventMessage = objectMapper.readValue(message,
                new TypeReference<EventMessageDto<?>>() {});

            // 메시지에서 추적 정보 복원
            final String traceId = eventMessage.getTraceId();
            final String parentSpanId = eventMessage.getSpanId(); // 발신자의 spanId가 수신자의 parentSpanId가 됨
            final Boolean sampled = eventMessage.getSampled();

            // 명시적인 span 생성
            brave.Tracing tracing = brave.Tracing.current();
            if (tracing != null && traceId != null) {
                // 기존 traceId를 사용하여 새 span 생성
                // 먼저 추적 컨텍스트를 복원하여 동일한 트레이스에 속하도록 함
                brave.propagation.TraceContext parentContext = extractTraceContext(traceId, parentSpanId, sampled);

                if (parentContext != null) {
                    // 부모 컨텍스트를 기반으로 새 스팬 생성
                    kafkaSpan = tracing.tracer().newChild(parentContext)
                        .name("kafka-consume")
                        .tag("kafka.event_type", eventMessage.getEventType())
                        .tag("kafka.source", eventMessage.getSource())
                        .tag("kafka.event_id", eventMessage.getEventId())
                        .start();
                } else {
                    // 부모 컨텍스트를 복원할 수 없는 경우 새 스팬 생성
                    kafkaSpan = tracing.tracer().nextSpan()
                        .name("kafka-consume")
                        .tag("kafka.event_type", eventMessage.getEventType())
                        .tag("kafka.source", eventMessage.getSource())
                        .tag("kafka.event_id", eventMessage.getEventId())
                        .start();
                }

                // 새 spanId 설정
                String newSpanId = kafkaSpan.context().spanIdString();
                eventMessage.setParentSpanId(parentSpanId);
                eventMessage.setSpanId(newSpanId);

                // 현재 스레드의 MDC에 추적 정보 설정
                MDC.put("traceId", traceId);
                MDC.put("spanId", newSpanId);
                if (parentSpanId != null) {
                    MDC.put("parentId", parentSpanId);
                }
            }

            try {
                // 이벤트 타입에 따라 처리
                processEvent(eventMessage);

                log.info("이벤트 처리 완료: eventType={}, eventId={}, traceId={}, spanId={}",
                    eventMessage.getEventType(), eventMessage.getEventId(), traceId, newSpanId());
            } catch (Exception e) {
                log.error("이벤트 처리 중 오류 발생: eventType={}, eventId={}, traceId={}, spanId={}, error={}",
                    eventMessage.getEventType(), eventMessage.getEventId(), traceId, newSpanId(), e.getMessage(), e);
                throw e;
            } finally {
                // 스팬 종료 및 MDC 정리
                if (kafkaSpan != null) {
                    kafkaSpan.finish();
                }
                MDC.remove("traceId");
                MDC.remove("spanId");
                MDC.remove("parentId");
            }
        } catch (IOException e) {
            log.error("메시지 역직렬화 중 오류 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("메시지 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 추적 ID와 부모 스팬 ID를 기반으로 Brave 추적 컨텍스트를 생성합니다.
     */
    private brave.propagation.TraceContext extractTraceContext(String traceId, String parentSpanId, Boolean sampled) {
        if (traceId == null) {
            return null;
        }

        try {
            // TraceContext 빌더 생성
            brave.propagation.TraceContext.Builder builder = brave.propagation.TraceContext.newBuilder();

            // traceId 설정
            long[] traceIdLong = convertHexToLongArray(traceId);
            if (traceIdLong.length == 1) {
                builder.traceId(traceIdLong[0]);
            } else if (traceIdLong.length == 2) {
                builder.traceIdHigh(traceIdLong[0]).traceId(traceIdLong[1]);
            }

            // 부모 스팬 ID가 있으면 설정
            if (parentSpanId != null) {
                builder.parentId(convertHexToLong(parentSpanId));
            }

            // 새로운 스팬 ID 생성
            long spanId = Math.abs(new Random().nextLong());
            builder.spanId(spanId);

            // 샘플링 여부 설정
            if (sampled != null) {
                builder.sampled(sampled);
            } else {
                builder.sampled(true); // 기본적으로 샘플링
            }

            return builder.build();
        } catch (Exception e) {
            log.warn("추적 컨텍스트 생성 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 16진수 문자열을 long 값으로 변환합니다.
     */
    private long convertHexToLong(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0L;
        }

        // 16자리를 넘어가는 경우 자르기
        if (hex.length() > 16) {
            hex = hex.substring(hex.length() - 16);
        }

        return Long.parseUnsignedLong(hex, 16);
    }

    /**
     * 16진수 문자열을 long 배열로 변환합니다.
     * 32자리 이하의 16진수 문자열을 처리하며, 결과는 1개 또는 2개의 long 값을 포함합니다.
     */
    private long[] convertHexToLongArray(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new long[]{0L};
        }

        // 16자리 이하인 경우 단일 long 값으로 변환
        if (hex.length() <= 16) {
            return new long[]{convertHexToLong(hex)};
        }
        // 32자리 이하인 경우 상위/하위 64비트로 분할
        else if (hex.length() <= 32) {
            String highHex = hex.substring(0, hex.length() - 16);
            String lowHex = hex.substring(hex.length() - 16);

            if (highHex.isEmpty()) {
                highHex = "0";
            }

            return new long[]{
                convertHexToLong(highHex),
                convertHexToLong(lowHex)
            };
        }
        // 32자리를 초과하는 경우 자르기
        else {
            String highHex = hex.substring(hex.length() - 32, hex.length() - 16);
            String lowHex = hex.substring(hex.length() - 16);

            return new long[]{
                convertHexToLong(highHex),
                convertHexToLong(lowHex)
            };
        }
    }

    /**
     * 추적 ID를 기반으로 Brave 추적 컨텍스트를 설정합니다.
     */
    private void setTraceContext(EventMessageDto<?> eventMessage) {
        String traceId = eventMessage.getTraceId();
        String parentSpanId = eventMessage.getParentSpanId();
        Boolean sampled = eventMessage.getSampled();

        if (traceId != null) {
            MDC.put("traceId", traceId);
            if (parentSpanId != null) {
                MDC.put("parentId", parentSpanId);
            }
            String spanId = eventMessage.getSpanId();
            if (spanId != null) {
                MDC.put("spanId", spanId);
            }
        }
    }

    /**
     * 페이로드를 지정된 타입으로 변환합니다.
     */
    protected <T> T convertPayload(EventMessageDto<?> eventMessage, Class<T> targetClass) {
        Object payload = eventMessage.getPayload();
        if (payload == null) {
            return null;
        }

        if (targetClass.isInstance(payload)) {
            return targetClass.cast(payload);
        } else {
            return objectMapper.convertValue(payload, targetClass);
        }
    }

    /**
     * 현재 스팬 ID를 가져옵니다. MDC에서 먼저 확인하고, 없으면 새로 생성합니다.
     */
    private String newSpanId() {
        String spanId = MDC.get("spanId");
        if (spanId == null) {
            spanId = UUID.randomUUID().toString().replace("-", "");
        }
        return spanId;
    }
}