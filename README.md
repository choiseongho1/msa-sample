# 🛒 E-Commerce Microservice Project

## 🧱 Architecture

```
                 [ Client ]
                     ↓
             ┌───────────────┐
             │ API Gateway   │  ← JWT 인증, 라우팅, 필터
             └─────┬─────────┘
                   ↓
 ┌────────────────────────────────────────┐
 │ Eureka        Config        Zipkin     │ ← Infra Services
 └───┬────────────┬────────────┬──────────┘
     ↓            ↓            ↓
[User]    [Product]    [Order]   [Cart]   [Payment]
 Service   Service     Service   Service  Service
   ↓           ↓           ↓         ↓         ↓
Kafka ←───── Event 기반 비동기 통신 ─────→ Kafka
   ↑           ↑           ↑         ↑         ↑
 MySQL       MySQL       MySQL     Redis     MySQL

 + Image Service, Batch Service, Prometheus/Grafana 등
```

---

## 🧩 Microservices

| 서비스 이름            | 설명                                                  |
|------------------------|-----------------------------------------------------|
| **API Gateway**        | 클라이언트와 내부 서비스 간의 라우팅 및 인증 필터 처리                     |
| **Config Server**      | 중앙 집중형 설정 관리 서버 (Spring Cloud Config)               |
| **Eureka Server**      | 서비스 등록 및 탐색 (Service Discovery)                     |
| **User Service**       | 회원가입, 로그인, 사용자 정보, 토큰 재발급 등 사용자 관리                  |
| **Product Service**    | 상품 등록, 조회, 옵션, 가격 등 상품 관련 기능 / 사용자 위시리스트 (추가/삭제/조회) |
| **Image Service**      | 상품 이미지 처리 (URL 제공 및 관리)                             |
| **Order Service**      | 주문 생성, 주문 상세 조회, 주문 취소 등 주문 처리                      |
| **Cart Service**       | 로그인 사용자 장바구니, 비로그인 세션 장바구니 기능 제공                    |
| **Payment Service**    | 결제 처리 및 주문 상태 연동                                    |
| **Batch Service**      | Spring Batch 기반 주기 작업 처리 (예: 위시리스트 정리 등)            |
| **Zipkin**             | 분산 추적(Tracing) 시스템으로 Kafka 이벤트 포함 전체 트레이스 시각화       |
| **Prometheus**         | 메트릭 수집 도구, Grafana와 연동                              |
| **Grafana**            | Prometheus 수집 데이터를 시각화하여 대시보드 제공                    |
| **Kafka**              | 서비스 간 비동기 메시지 처리 (주문/결제/상품 재고 등)                    |
| **Redis**              | Refresh Token 저장소, 캐시 저장소로 사용됨                      |
| **MySQL**              | 각 도메인 서비스의 데이터 저장소 (서비스별 분리 운영)                     |

---

## 🛠️ 기술 스택 및 구성

- **Backend Framework**: Spring Boot 3.x
- **Cloud**: Spring Cloud Gateway, Eureka, Config Server
- **Event Streaming**: Kafka
- **Database**: MySQL, Redis
- **Monitoring**: Zipkin, Prometheus, Grafana
- **Auth**: JWT (토큰 기반 인증)
- **CI/CD**: Docker, Docker Compose
- **Async**: Kafka Producer/Consumer
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Timeout)

---

## 🔁 통신 방식

- **동기 통신**: Feign Client (서비스 간 HTTP)
- **비동기 통신**: Kafka (주문 이벤트 등)
- **복원력**: Circuit Breaker + Retry + Timeout (Gateway/Service Layer)

---

## 🔐 보안

- JWT 기반 인증/인가
- Gateway에서 인증 필터 처리
- Swagger를 통한 API 문서화 및 보안 구간 구분

#

## 🔐 JWT 인증 구조 및 흐름

e-commerce 프로젝트는 **Access Token + Refresh Token** 조합의 JWT 인증 방식을 사용합니다.  
**Refresh Token은 Redis에 저장**하여 관리되며, 만료 전에 갱신이 필요한 경우 자동으로 재발급됩니다.

#### 🔑 로그인 시 토큰 발급

```java
public TokenDto createToken(String userId) {
    String accessToken = generateAccessToken(userId);
    String refreshToken = generateRefreshToken(userId);

    redisTemplate.opsForValue().set(
        "RT:" + userId,
        refreshToken,
        refreshTokenValidTime,
        TimeUnit.MILLISECONDS
    );

    return new TokenDto(accessToken, refreshToken);
}
```

| 토큰 종류        | 저장 위치                    | 유효 기간 | 전송 방식             |
|------------------|-------------------------------|-----------|------------------------|
| Access Token     | 클라이언트 (Authorization 헤더) | 30분      | Authorization: Bearer  |
| Refresh Token    | Redis (`RT:{userId}`)         | 7일       | Body 또는 Cookie 등     |

#### 🔁 Access Token 재발급

```java
public TokenDto refreshToken(String refreshToken) {
    String userId = getUserId(refreshToken);
    String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);

    if (!refreshToken.equals(savedRefreshToken)) {
        throw new IllegalArgumentException("Invalid refresh token");
    }

    String newAccessToken = generateAccessToken(userId);
    String newRefreshToken = refreshToken;

    long remainingTime = redisTemplate.getExpire("RT:" + userId, TimeUnit.MILLISECONDS);
    if (remainingTime < TimeUnit.DAYS.toMillis(3)) {
        newRefreshToken = generateRefreshToken(userId);
        redisTemplate.opsForValue().set(
            "RT:" + userId, newRefreshToken, refreshTokenValidTime, TimeUnit.MILLISECONDS
        );
    }

    return new TokenDto(newAccessToken, newRefreshToken);
}
```

- Redis에 저장된 Refresh Token과 일치하는지 확인
- 남은 유효 시간이 3일 미만이면 Refresh Token도 재발급 및 저장

#### 🚪 로그아웃 처리

```java
public void logout(String userId) {
    redisTemplate.delete("RT:" + userId);
}
```

#### ⚙️ JwtTokenUtil 기능 요약

| 기능              | 메서드명              | 설명                                      |
|-------------------|-----------------------|-------------------------------------------|
| 토큰 생성         | `createToken`         | Access + Refresh Token 생성 및 저장       |
| Access 재발급     | `refreshToken`        | 유효성 확인 후 Access Token 재발급         |
| 토큰 검증         | `validateToken`       | JWT 서명 검증 및 파싱                      |
| 토큰 만료 여부     | `isExpired`           | 토큰 만료 여부 체크                        |
| 사용자 ID 추출     | `getUserId`           | JWT Claims에서 사용자 ID 추출              |
| 현재 사용자 조회   | `getCurrentUserId`    | SecurityContext에서 현재 로그인 사용자 조회 |
| 로그아웃 처리     | `logout`              | Redis에서 Refresh Token 삭제               |

### 🛡️ Gateway 인증 필터 처리 흐름

모든 요청은 Spring Cloud Gateway를 통해 통과하며, **AuthorizationHeaderFilter**에서 JWT를 검사합니다.

#### 🔍 필터 적용 순서

1. **제외 경로 확인**
2. **Authorization 헤더 확인**
3. **Access Token 유효성 검사**
4. **만료 시 Refresh Token으로 재발급 요청**

#### 🧪 예외 처리

| 상황 | 응답 코드 | 설명 |
|------|----------|------|
| Authorization 헤더 없음 | 401 | `No authorization header` |
| JWT 유효하지 않음 | 401 | `Invalid token` |
| Refresh Token 없음 | 401 | `Refresh token not found` |
| Refresh Token 재발급 실패 | 401 | `Token refresh failed` |

#### 🔄 Access Token 자동 재발급 흐름

```java
catch (ExpiredJwtException e) {
    return handleExpiredToken(exchange, request, chain); // Refresh 토큰으로 재발급
}
```

```java
private Mono<TokenDto> refreshAccessToken(...) {
    return webClientBuilder.build()
        .post()
        .uri("lb://USER-SERVICE/api/v1/user/refresh")
        .header("RefreshToken", refreshToken)
        .retrieve()
        .bodyToMono(TokenDto.class);
}
```

- Access Token 만료 시 → USER-SERVICE로 재발급 요청
- 새로 받은 토큰은 응답 헤더에 포함됨

#### 📥 응답 헤더 예시

```http
Authorization: Bearer {newAccessToken}
RefreshToken: {newRefreshToken}
```

- JWT 기반 인증/인가
- Gateway에서 인증 필터 처리
- Swagger를 통한 API 문서화 및 보안 구간 구분

---

## 📦 배포 및 실행

1. **환경 구성**
   - `application.yml` 설정: `config-server`, `eureka`, `kafka`, `db` 연결 정보
   - 서비스별 Dockerfile 준비

2. **Docker Compose 실행**
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose-dev.yml up --build
   ```

3. **로컬 접속**
   - Gateway: http://localhost:8000
   - Swagger UI: http://localhost:8000/swagger-ui.html
   - Eureka: http://localhost:8761
   - Zipkin: http://localhost:9411
   - Grafana: http://localhost:3000

---

## ⚡ Wishlist 캐싱 최적화

위시리스트 조회 성능 개선을 위해 **Caffeine Cache**를 적용하였습니다.  
사용자 ID를 캐시 키로 사용하며, 등록 및 삭제 시 자동으로 캐시 무효화가 발생합니다.

### ✅ 적용 방식

- 조회 시: `@Cacheable("wishlist")`
- 저장/삭제 시: `@CacheEvict("wishlist")`
- Java 기반 설정 (`CaffeineConfig.java`)으로 캐시 구성

### 📂 서비스 코드 예시

```java
@Cacheable(cacheNames = "wishlist", key = "#p0", condition = "#p0 != null")
public List<WishlistListDto> findWishlist(String userId) {
    return wishlistRepository.findWishlistList(userId);
}

@CacheEvict(cacheNames = "wishlist", key = "#p0.userId")
public void saveWishlist(WishlistSaveDto wishlistSaveDto) { ... }

@CacheEvict(cacheNames = "wishlist", key = "#p0")
public void removeFromWishlist(String userId, Long wishlistId) { ... }
```

### ⚙️ Caffeine 설정 (Java Config)

```java
@Configuration
@EnableCaching
public class CaffeineConfig {

    @Bean
    public Caffeine<Object, Object> caffeineBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(1000) // 최대 1000개 항목 캐시
            .expireAfterWrite(1, TimeUnit.HOURS); // 1시간 후 만료
    }

    @Bean
    @Primary
    public CacheManager cacheManager(Caffeine<Object, Object> caffeineBuilder) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("wishlist");
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }
}
```
---

## 🔁 SAGA 패턴 (분산 트랜잭션 처리)

Kafka 기반 이벤트 흐름을 통해 **분산 트랜잭션을 SAGA 패턴**으로 처리하고 있습니다.  
`Order → Product → Payment` 순서의 서비스 호출에서, 실패 시에는 **상태 기반 보상 트랜잭션**으로 처리됩니다.

### 📦 주문 처리 흐름 예시

```java
@Transactional
public void processOrder(OrderSaveDto orderSaveDto) {
    boolean isValidUser = userServiceClient.validateUser(orderSaveDto.getUserId());
    if (!isValidUser) throw new CustomException("유효하지 않은 사용자입니다.");

    Order savedOrder = saveOrder(orderSaveDto);
    decreaseProductStock(savedOrder, orderSaveDto);

    orderEventProducer.publishOrderCreatedEvent(
        savedOrder.getId(),
        savedOrder.getUserId(),
        savedOrder.getOrderPrice()
    );
}
```

### 🧨 실패 시 상태 보상 처리

```java
switch (eventMessage.getEventType()) {
    case "PAYMENT_COMPLETED":
        updateOrderStatus(COMPLETED);
        break;
    case "PAYMENT_FAILED":
        updateOrderStatus(CANCELED);
        break;
    case "PAYMENT_CANCELLED":
        updateOrderStatus(CANCELED);
        break;
}
```

---

## 📡 Kafka 트레이싱 (Zipkin 연동)

Zipkin + Brave + KafkaTracing을 통해 Kafka 이벤트 흐름도 전체 트레이스로 추적되도록 구성하였습니다.

- `KafkaConfig`에서 KafkaTracing 설정
- `AbstractEventProducer` → 메시지 발행 시 traceId 삽입
- `AbstractEventConsumer` → 메시지 수신 시 context 복원

### 예시

```java
Span span = tracing.tracer().nextSpan().name("kafka-produce").start();
traceContext.setTraceId(span.context().traceIdString());
```

```java
TraceContext parentContext = extractTraceContext(...);
Span kafkaSpan = tracing.tracer().newChild(parentContext).name("kafka-consume").start();
```

### 🗺️ Kafka 포함 호출 흐름 (Mermaid 다이어그램)

```mermaid
sequenceDiagram
    participant Client
    participant API Gateway
    participant Order
    participant Kafka
    participant Product
    participant Payment

    Client->>API Gateway: POST /order
    API Gateway->>Order: createOrder
    Order->>Kafka: ORDER_CREATED
    Kafka->>Product: DECREASE
    Kafka->>Payment: ORDER_CREATED
    Payment->>Kafka: PAYMENT_COMPLETED
    Kafka->>Order: PAYMENT_COMPLETED
```

Zipkin에서 traceId 하나로 이 전체 흐름이 추적 가능합니다.

---

## 📈 모니터링 및 관측성

- **Zipkin**: 서비스 간 트레이싱
- **Prometheus + Grafana**: 메트릭 수집 및 대시보드
- **Logstash (Optional)**: 중앙 집중형 로그 수집

---

## 📑 API Gateway 설정 및 설계 의도

Spring Cloud Gateway 기반의 API Gateway는 **보안 필터, 장애 복원력, Swagger 통합, 요청 라우팅** 등을 통합하여 모든 트래픽의 진입 지점을 제어합니다.

### 1️⃣ JWT 인증 필터 (`AuthorizationHeaderFilter`)

**🔧 설정 목적**  
JWT 기반 인증/인가를 Gateway 레벨에서 처리하여, 각 서비스에서는 인증 검증을 생략할 수 있도록 설계

**🛠️ 적용 내용**
```yaml
default-filters:
  - name: AuthorizationHeaderFilter
    args:
      excludePaths:
        - /user/auth/login
        - /user/signup
        - /**/swagger-ui/index.html/**
```

**✅ 효과**
- 인증이 필요한 요청만 JWT를 검사
- Swagger/로그인/회원가입은 인증 없이 접근 가능

---

### 2️⃣ Circuit Breaker + Retry 설정

**🔧 설정 목적**  
하위 서비스 장애 발생 시 빠르게 우회(fallback)하여 전체 시스템의 가용성을 보장하고, 재시도를 통해 일시적 오류 복구

**🛠️ 적용 내용**
```yaml
default-filters:
  - name: CircuitBreaker
    args:
      fallbackUri: forward:/fallback
      statusCodes:
        - INTERNAL_SERVER_ERROR
        - SERVICE_UNAVAILABLE
        - GATEWAY_TIMEOUT
  - name: Retry
    args:
      retries: 3
      methods: GET
      backoff:
        firstBackoff: 100ms
        factor: 2
```

**✅ 효과**
- 장애 전파를 차단하여 시스템 전체 다운 방지
- 비정상 상태에서도 빠르게 fallback 제공

---

### 3️⃣ Spring Cloud Config 연동

**🔧 설정 목적**  
환경 설정을 중앙 집중형으로 관리하고, 서비스 간 일관성 유지

**🛠️ 적용 내용**
```yaml
spring:
  config:
    import: "configserver:${CONFIG_SERVER_URI:http://localhost:8888}"
```

**✅ 효과**
- 설정 변경 시 서비스 재배포 없이 실시간 적용 가능
- 설정 일관성 및 관리 용이

---

### 4️⃣ Resilience4j Circuit Breaker

**🔧 설정 목적**  
서비스별로 개별 차단 상태를 관리하여 장애 전파 방지

**🛠️ 적용 내용**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      userCircuitBreaker:
        baseConfig: default
```

**✅ 효과**
- 각 서비스 회복 상태를 독립적으로 추적
- Prometheus & Grafana로 모니터링 가능

---

### 5️⃣ 모니터링 & 헬스체크 노출

**🔧 설정 목적**  
운영 상태 확인 및 Prometheus 수집을 위한 엔드포인트 노출

**🛠️ 적용 내용**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, circuitbreakers, prometheus
```

**✅ 효과**
- `/actuator/health`로 상태 확인 가능
- Prometheus가 수집하여 Grafana로 시각화 가능

## 📚 Swagger API Summary

### 🔐 Auth API (`/auth`)
| Method | Endpoint     | Description                  |
|--------|--------------|------------------------------|
| POST   | /login       | 로그인                        |
| POST   | /refresh     | JWT 토큰 재발급               |
| POST   | /logout      | 로그아웃 및 Refresh Token 삭제 |

### 👤 User API (`/user`)
| Method | Endpoint          | Description                  |
|--------|-------------------|------------------------------|
| POST   | /signup           | 회원가입                     |
| GET    | /{userId}         | 로그인 사용자 정보 조회       |
| GET    | /validate/{id}    | [Feign] 유효 사용자 검증      |

### 📦 Product API (`/product`)
| Method | Endpoint                           | Description                      |
|--------|------------------------------------|----------------------------------|
| POST   | /                                  | 상품 등록                         |
| GET    | /                                  | 상품 목록 조회                     |
| GET    | /{productId}                       | 상품 상세 조회                     |
| GET    | /validate/{id}                     | [Feign] 상품 ID 유효 검사         |
| GET    | /{productId}/image                 | [Feign] 상품 이미지 ID 조회        |
| GET    | /{productId}/price                 | [Feign] 상품 가격 조회             |
| GET    | /{productId}/option/{optionId}     | [Feign] 상품 옵션 조회             |
| GET    | /{productId}/{optionId}            | [Feign] 상품 상세 정보             |

### 🛒 Cart API (`/cart`)
| Method | Endpoint                                 | Description                      |
|--------|------------------------------------------|----------------------------------|
| PUT    | /cart/item                               | [로그인] 장바구니 수량 변경       |
| GET    | /cart/{userId}                           | [로그인] 사용자 장바구니 조회      |
| POST   | /cart/{userId}                           | [로그인] 사용자 장바구니 저장      |
| DELETE | /cart/{userId}/item/{cartItemId}         | [로그인] 장바구니 항목 삭제       |

### 🧑‍🦲 Guest Cart API (`/guest/cart`)
| Method | Endpoint                     | Description                      |
|--------|------------------------------|----------------------------------|
| POST   | /guest/cart                  | [비로그인] 장바구니 상품 추가     |
| DELETE | /guest/cart                  | [비로그인] 장바구니 전체 삭제     |
| GET    | /guest/cart/{sessionId}      | [비로그인] 세션 기반 장바구니 조회 |
| DELETE | /guest/cart/{sessionId}      | [비로그인] 세션 기반 장바구니 항목 삭제 |

### 🧾 Order API (`/order`)
| Method | Endpoint                | Description                      |
|--------|-------------------------|----------------------------------|
| POST   | /                       | 주문 저장                         |
| POST   | /{orderId}/cancel       | 주문 취소                         |
| GET    | /{orderId}              | 주문 상세 조회                     |
| GET    | /user/{userId}          | 사용자별 주문 목록 조회            |

### 💖 Wishlist API (`/wishlist`)
| Method | Endpoint                          | Description                      |
|--------|-----------------------------------|----------------------------------|
| POST   | /                                 | 위시리스트 추가                   |
| GET    | /user/{userId}                    | 위시리스트 조회                   |
| DELETE | /user/{userId}/{wishlistId}       | 위시리스트 항목 삭제              |