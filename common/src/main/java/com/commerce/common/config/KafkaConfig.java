package com.commerce.common.config;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.kafka.clients.KafkaTracing;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import com.commerce.common.dto.EventMessageDto;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.application.name:product-service}")
    private String serviceName;

    @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}")
    private String zipkinUrl;

    @Value("${spring.kafka.producer.retries:5}")
    private int producerRetries;

    @Value("${spring.kafka.producer.request-timeout-ms:30000}")
    private int producerRequestTimeoutMs;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private int consumerMaxPollRecords;

    @Value("${spring.kafka.consumer.fetch-max-wait-ms:500}")
    private int consumerFetchMaxWaitMs;

    @Value("${spring.kafka.listener.concurrency:3}")
    private int listenerConcurrency;

    @Value("${spring.kafka.listener.poll-timeout:3000}")
    private int listenerPollTimeout;


    // Zipkin 전송을 위한 OkHttpSender 설정
    @Bean
    public OkHttpSender sender() {
        return OkHttpSender.create(zipkinUrl);
    }

    // Zipkin 리포터 설정
    @Bean
    public Reporter<Span> spanReporter(OkHttpSender sender) {
        return AsyncReporter.builder(sender)
            .closeTimeout(500, TimeUnit.MILLISECONDS)
            .messageTimeout(500, TimeUnit.MILLISECONDS)
            .queuedMaxSpans(10000)
            .build();
    }

    // Zipkin SpanHandler 설정
    @Bean(name = "customZipkinSpanHandler")
    public SpanHandler customZipkinSpanHandler(Reporter<Span> spanReporter) {
        return AsyncZipkinSpanHandler.create(spanReporter);
    }

    // 현재 트레이스 컨텍스트 설정
    @Bean
    public CurrentTraceContext currentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
            .build();
    }

    // Brave Tracing 설정
    @Bean
    public Tracing tracing(@Qualifier("customZipkinSpanHandler") SpanHandler spanHandler, CurrentTraceContext currentTraceContext) {
        return Tracing.newBuilder()
            .localServiceName(serviceName)
            .addSpanHandler(spanHandler)
            .currentTraceContext(currentTraceContext)
            .propagationFactory(B3Propagation.FACTORY)
            .sampler(Sampler.ALWAYS_SAMPLE)
            .supportsJoin(true)
            .traceId128Bit(true)
            .build();
    }

    // Kafka Tracing 설정
    @Bean
    public KafkaTracing kafkaTracing(Tracing tracing) {
        return KafkaTracing.newBuilder(tracing)
            .remoteServiceName("kafka")
            .writeB3SingleFormat(false) // B3 멀티 헤더 형식 사용
            .build();
    }

    // Producer 설정
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 재시도 및 타임아웃 설정 추가
        props.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, producerRequestTimeoutMs);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, producerRequestTimeoutMs + 5000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaTracing kafkaTracing) {
        return new DefaultKafkaProducerFactory<String, String>(producerConfigs()) {
            @Override
            public Producer<String, String> createProducer() {
                // 일반 Producer를 생성한 후 추적 기능이 추가된 Producer로 래핑
                return kafkaTracing.producer(super.createProducer());
            }
        };
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        return template;
    }

    // Consumer 설정
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, consumerFetchMaxWaitMs);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaTracing kafkaTracing) {
        return new DefaultKafkaConsumerFactory<String, String>(consumerConfigs()) {
            @Override
            public Consumer<String, String> createConsumer() {
                // 일반 Consumer를 생성한 후 추적 기능이 추가된 Consumer로 래핑
                return kafkaTracing.consumer(super.createConsumer());
            }
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // ✅ 병렬 처리 설정
        factory.setConcurrency(listenerConcurrency);
        factory.getContainerProperties().setPollTimeout(listenerPollTimeout);

        // ✅ 단일 메시지 모드 (배치 OFF)
        factory.setBatchListener(false);  // 이 줄만 추가하면 끝!
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // ✅ 에러 핸들러
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    // 오류 처리기 설정
    @Bean
    public DefaultErrorHandler errorHandler() {
        // 지수 백오프 설정 (재시도 간격이 점점 길어짐)
        ExponentialBackOff backOff = new ExponentialBackOff(
            1000, // 초기 간격 (1초)
            2.0); // 배수 (각 재시도마다 간격이 2배로 증가)
        backOff.setMaxInterval(60000); // 최대 간격 (60초)
        backOff.setMaxElapsedTime(300000); // 최대 재시도 시간 (5분)

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // 오류 발생 시 로깅 및 처리 로직
            System.err.println("메시지 처리 중 오류 발생: " + exception.getMessage());
            // 필요한 경우 DLQ(Dead Letter Queue)로 메시지 전송 등의 처리 추가
        }, backOff);

        // 특정 예외는 재시도하지 않도록 설정
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            IllegalStateException.class,
            org.springframework.messaging.converter.MessageConversionException.class
        );

        return errorHandler;
    }
}
