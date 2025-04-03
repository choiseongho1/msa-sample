package com.commerce.common.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String serviceName;

    protected <T> void publishEvent(String topic, String eventType, T payload) {
        brave.Span kafkaSpan = null;
        try {
            final String initialTraceId = getTraceId();
            log.debug("이벤트 발행 시작: topic={}, eventType={}, traceId={}", topic, eventType, initialTraceId);

            final EventMessageDto<T> eventMessage = EventMessageDto.of(eventType, serviceName, payload);
            eventMessage.setTraceId(initialTraceId);

            brave.Tracing tracing = brave.Tracing.current();
            if (tracing != null) {
                kafkaSpan = tracing.tracer().nextSpan().name("kafka-produce")
                    .tag("kafka.topic", topic)
                    .tag("kafka.event_type", eventType)
                    .start();

                try (brave.Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(kafkaSpan)) {
                    eventMessage.setSpanId(kafkaSpan.context().spanIdString());
                    eventMessage.setParentSpanId(kafkaSpan.context().parentIdString());
                    eventMessage.setSampled(kafkaSpan.context().sampled());

                    sendMessage(topic, eventMessage, kafkaSpan);
                }
            } else {
                eventMessage.setSpanId(UUID.randomUUID().toString());
                eventMessage.setSampled(true);
                sendMessage(topic, eventMessage, null);
            }

        } catch (Exception e) {
            log.error("이벤트 직렬화 실패: eventType={}, error={}", eventType, e.getMessage(), e);
            if (kafkaSpan != null) {
                kafkaSpan.tag("error", e.getMessage());
                kafkaSpan.error(e);
                kafkaSpan.finish();
            }
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    private <T> void sendMessage(String topic, EventMessageDto<T> eventMessage, brave.Span span) throws Exception {
        String messageJson = objectMapper.writeValueAsString(eventMessage);
        log.debug("보낼 메시지 JSON: {}", messageJson);

//        ProducerRecord<String, String> record = new ProducerRecord<>(topic, messageJson);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, messageJson);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("이벤트 발행 성공: topic={}, eventType={}, eventId={}, traceId={}, spanId={}, offset={}",
                    topic, eventMessage.getEventType(), eventMessage.getEventId(),
                    eventMessage.getTraceId(), eventMessage.getSpanId(),
                    result.getRecordMetadata().offset());
                if (span != null) {
                    span.tag("kafka.offset", String.valueOf(result.getRecordMetadata().offset()));
                    span.finish();
                }
            } else {
                log.error("이벤트 발행 실패: topic={}, eventType={}, traceId={}, error={}",
                    topic, eventMessage.getEventType(), eventMessage.getTraceId(), ex.getMessage(), ex);
                if (span != null) {
                    span.tag("error", ex.getMessage());
                    span.error(ex);
                    span.finish();
                }
            }
        });
    }

    private String getTraceId() {
        try {
            brave.Tracing tracing = brave.Tracing.current();
            if (tracing != null && tracing.currentTraceContext().get() != null) {
                return tracing.currentTraceContext().get().traceIdString();
            }
        } catch (Exception e) {
            log.debug("추적 ID 가져오기 실패: {}", e.getMessage());
        }

        String mdcTraceId = MDC.get("traceId");
        if (mdcTraceId != null && !mdcTraceId.isEmpty()) {
            return mdcTraceId;
        }

        return UUID.randomUUID().toString();
    }
}
