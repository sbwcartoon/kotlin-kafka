# Kafka 테스트
### 프로젝트 참고
- Topic 이름은 application.yml에 정의한 kafka.topic.order-created를 사용함
  - 해당 키 이름은 통합 테스트에서도 동일하게 사용하므로 변경 시 테스트 코드에도 적용 필요.(테스트 코드에서는 테스트 구동 시 annotation class에서 해당 키를 application.yml에 설정하고 있음)
- 오류 핸들링
  - Publish 오류는 메서드마다 상황별 오류 처리 방식이 다를 수 있으므로 일반화하지 않음
  - Consumer 오류는 KafkaConsumerConfig 파일에서 공통 핸들링하고 필요한 경우 Consumer마다 조치 방법을 다르게 적용할 수 있도록 함
    - Consume에서 오류 발생 시 application.yml의 kafka.Consumer.retry에 정의된 최대 시도 수(max-attempts), 재시도 인터벌 milliseconds(backoff-interval-ms)에 따라 재시도를 수행
    - 해당 파일에 정의한 메서드 이름을 @KafkaListener가 설정된 Consumer 메서드의 containerFactory 항목에 기재하면 적용됨. 만약 기재하지 않은 경우에는 kafkaListenerContainerFactory Bean을 사용
    - DLT 처리
      - 특정 Topic을 특정 Consumer가 Consume할 때 설정한 재시도 횟수를 모두 채웠는데도 오류가 발생할 경우 오류 조치를 위해 {해당 Topic 이름 + .DLT}라는 이름의 새 Topic을 Publish
      - 만약 DLT Consumer에서도 오류가 발생하면 fallback 정보로 저장
        - DLT는 기본적으로 운영 개입이 필요한 경우이므로 DLT라는 Topic을 Publish하지 않고 fallback만으로 처리해도 큰 문제는 없으나, Consumer마다의 처리가 필요한 경우가 있을 수 있으므로 DLT Topic을 일단 Publish 한 후 여기서도 오류가 발생할 경우에 fallback을 사용
      - 현재는 해당 Topic을 처리하는 Partition에 Publish하지만 필요할 경우 DLT 처리 전용 Partition 사용 가능(ex. TopicPartition(record.topic() + ".DLT", 0) // 0번 Partition에서 처리)
- transaction 테스트를 위해 jpa entity 사용

---

# 아키텍처 패턴

## 동기 전송 방식
- 경로: sync

### 프로세스
1. 한 트랜잭션 안에서 다음을 함께 수행
  - 서비스 핵심 로직 수행
  - Kafka Publish
2. Consumer는 해당하는 Topic을 Consume

### 장점
- 구현 간단
- 핵심 로직 및 Publish 발생을 한 트랜잭션으로 묶어 Publish 발생이 보장되고, 오류 발생 시 롤백이 쉬움
- Publish 재시도 처리가 쉽고 직관적임(Spring의 @Retryable 사용 가능)
- 메시지 전송이 중요한 작업 흐름일 때 성공/실패를 명시적으로 감지할 수 있어서 기능 안정성 높아짐

### 단점
- 서비스 간 결합도가 비동기 방식에 비해 높아짐
- 블로킹 방식이므로 대량 메시지 처리에는 부적합

### 사용처: 즉각적인 메시징 및 상황별 오류 처리가 필요하고 간단히 구현할 때(주로 같은 BC 안에서 메시징이 일어날 때사용)
- 결제 완료 후 알림 전송


## 비동기 전송 방식
### 프로세스
1. 서비스 핵심 로직 수행 및 비동기적으로 Kafka Publish
2. Consumer는 해당하는 Topic을 Consume

### 장점
- Spring Kafka의 가장 일반적인 사용 방식
- 요청 처리 성공/오류에 대한 콜백을 정의할 수 있으므로 후속 처리에 유연하게 대응 가능

### 단점
- 장애 발생 시 메시지 유실 가능. 그래서 마이크로서비스 간 불일치 문제 발생 가능(dual-write)
  - ex. Kafka Publish는 비동기로 작동하므로 DB에 데이터 저장(핵심 서비스 로직) 후 Kafka로 Publish하기 전에 오류가 발생 가능

### 사용처: 비동기 메시징이 필요하고 간단히 구현해도 무방한 서비스(메시지 유실이 큰 문제가 아닌 서비스)
- 실시간 로깅
- 사용자 행동 이벤트 수집


## Outbox 패턴 without SAGA
### 프로세스
1. 한 트랜잭션 안에서 다음을 함께 수행
  - 서비스 핵심 로직 수행
  - Kafka Publish를 위한 정보를 DB의 Outbox 테이블에 저장
2. 관련된 Kafka 서비스는 상기 Outbox 테이블에서 처리되지 않은 정보를 Polling 방식으로 주기적으로 읽어오거나 CDC로 DB의 변경 사항을 즉각 반영하여 Publish 처리
3. Consumer는 해당하는 Topic을 Consume

### 장점
- 서비스 운영 안정성이 높아짐. 핵심 로직 및 Publish 발생을 한 트랜잭션으로 묶어 Publish 발생이 보장됨
- 장애 처리에 강함. Outbox 테이블에서 Publish 상태 정보를 관리하면 Publish 재시도 등 부가적인 처리 가능

### 단점
- Kafka만 사용하는 것에 비해 구현 복잡
- 메시징을 위한 테이블을 사용해야 함
- DB의 Outbox 테이블에 커맨드 쿼리를 사용하는 과정이 추가되므로 서비스 성능이 조금 저하됨
  - Publish할 정보를 Outbox 테이블에 저장
  - 상태 정보를 사용한다면 Publish 성공 후 해당 정보를 수정, 상태 정보를 사용하지 않을 경우 해당 정보를 삭제
- 관련된 Kafka 서비스는 기본적으로 처리되지 않은 정보를 Polling 방식으로 일정 주기로 읽어오므로 Publish에 지연 발생 가능. 단, Debezium처럼 CDC 방식으로 작동할 경우 즉각 반응 가능(다만 DB를 읽어오는 과정이 추가되는 것은 변함 없음)
- 현실적으로 Debezium 등의 3rd party를 추가로 사용하거나 publish 지연을 막기 위해 CDC를 추가로 구현하는 게 권장됨

### 사용처: 비동기 메시징이 필요하고 publish까지는 보장하되, 오류 발생 시에도 롤백은 불필요한 서비스
- 실시간 로깅(실패 시에도 로깅을 유발한 행위는 되돌릴 필요 없음)
- 사용자 액션에 따라 발생하는 이벤트(실패 시에도 사용자 액션을 되돌릴 필요 없음)
- 회원 가입 후 이메일 발송(실패 시에도 회원 가입을 취소할 필요 없음)


## Outbox 패턴 with SAGA
### 프로세스
- Outbox 패턴과 전체 프로세스 동일
- Publish 과정에 오류 발생 시 보상 트랜잭션(SAGA) 추가

### 장점
- Outbox 패턴의 장점을 모두 가짐
- 비동기적으로 작동하더라도 오류 발생 시 롤백이 필요한 서비스에 사용 가능
- 적용하는 SAGA 패턴의 방식에 따라
  - Orchestration 방식은 메시지(Kafka의 Topic)를 중앙 집중적으로 제어하므로 메시지 흐름을 한 눈에 파악할 수 있어서 통합 관리가 쉬움
  - Choreography 방식은 메시지(Kafka의 Topic)를 해당 publisher 및 consumer끼리만 제어하므로 분산 시스템에 적합(확장성 좋음, 유지관리성 좋음). 마이크로서비스에 적합

### 단점
- Outbox 패턴의 단점을 모두 가짐
- 구현 복잡도는 더욱 올라감
- 적용하는 SAGA 패턴의 방식에 따라
  - Orchestration 방식은 중앙 관리 시스템의 오류는 곧 전체 메시징 시스템에 장애(단일 장애점). 중앙에서 메시징을 사용하는 서비스를 한 눈에 관리할 수 있으므로 서비스 간 결합도가 높아질 가능성이 높음
  - Choreography 방식은 전체적인 비즈니스 흐름의 추적이 어렵고, 비즈니스 단위에서 디버깅이 어려움

### 상황별 오류 처리
- 이벤트 생성 불가
  - 서비스 핵심 로직과 이벤트 생성이 한 트랜잭션에서 수행되므로 핵심 로직에 의한 DB 변경도 자동으로 롤백 처리됨
  - 추가적인 처리가 필요할 때만 추가 조치
- Publish 불가
  - Publish가 불가능한 것은 인프라 수준에서 Kafka에 오류가 발생했을 가능성이 높음. 평상시 Kafka가 꺼지지 않고 정상 작동 중인지 모니터링 필요
  - 재시도 횟수만큼 시도했는데도 오류가 계속되는 경우 보상 트랜잭션 작동
- Consume 불가
  - 필요 시 Consume을 재시도하되 이벤트의 멱등성을 확보하는 것이 전제되어야 함(ex. 특정 order id가 발생했다는 이벤트가 여러 개일 경우 1개만 처리하고 나머지는 무시)
  - 재시도 횟수만큼 시도했는데도 오류가 계속되는 경우 보상 트랜잭션 작동(Publish가 정상 작동했더라도 Consume에 오류가 나면 의미가 없으므로 롤백 필요)

### 사용처: 비동기 메시징이 필요하고 publish를 보장하고, 오류 발생 시 롤백이 필수적인 서비스
- 결제 시스템
- 주문에 연계된 재고 관리
- 마이크로서비스 간 연동

---

# Consumer 참고
### 리밸런스 설정
- Consumer를 동적으로 추가/삭제하는 등 리밸런스가 잦을 경우 리밸런스 딜레이 설정 추가 가능
  - docker compose의 environment 설정에 추가 가능하나, 리밸런스를 유발하는 요인은 서비스 시스템의 유스케이스에 있으므로 application.yml에 설정하는 것이 나을 것으로 보임

### 동일 요청 처리의 멱등성 보장
- Kafka는 메시지를 Consume 했더라도 곧장 삭제하지 않으므로 동일한 메시지를 중복 Consume할 수 있고, 메시지의 ack가 commit되지 않았다고 판단할 때 Consume을 재시도하여 같은 메시지를 여러 번 Consume 가능. 그러므로 Consume을 여러 번 시도할 때도 최종 결과는 항상 같도록 보장해야 함. 아래는 Consume 재시도 상황
  - Consumer가 메시지를 정상적으로 받은 후 해당 로직을 완료하기 전에 오류 발생한 경우 해당 Consumer에서 재시도
  - Consumer가 메시지를 정상적으로 받은 후 해당 로직을 완료하기 전에 리밸런스가 발생할 경우 다른 Consumer에서 재시도
  (리밸런스는 해당 Consumer group에 Consumer가 추가/삭제되는 등의 사유로 발생)  
- Consumer의 로직이 한 트랜잭션 안에서 작동하도록 하고 처리 상태를 관리하면 멱등성 확보 가능
  - 처리된 이벤트 아이디를 저장하는 별도 테이블을 사용하는 방식
  ```kotlin
  @Transactional
  fun handleEvent(event: OrderConfirmedEvent) {
      try {
          processedEventRepository.save(ProcessedEvent(eventId = event.id))  // id에 UNIQUE 제약 필수
          entityManager.flush() // 아래 orderService.confirmOrder 로직에서 processedEventRepository를 조회할 경우에는 조회하기 전에 DB에 변경 사항을 반영해야 하므로 flush. 조회하지 않을 경우는 flush 불필요
      } catch (e: DataIntegrityViolationException) {
          return // 이미 처리된 이벤트
      }
      
      // 여기서 예외 발생 시 전체 롤백
      orderService.confirmOrder(event.orderId)
  }
  ```
  - 조건부 update. 원본 데이터의 처리 상태를 update하는 방식
  ```kotlin
  interface ConsumerState : JpaRepository<ConsumerStateJpaEntity, Long>, QuerydslConsumerStateRepository
  
  interface QuerydslConsumerStateRepository {
      fun markAsPaidIfCreated(orderId: Long): Int
  }
  
  class QuerydslConsumerStateRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager
  ) : QuerydslConsumerStateRepository {
      
      override fun markAsPaidIfCreated(orderId: Long): Int {
          val updatedCount = queryFactory
              .update(order)
              .set(order.status, OrderStatus.PAID)
              .where(order.id.eq(orderId), order.status.eq(OrderStatus.CREATED))
              .execute() // 변경사항을 DB 트랜잭션에 직접 반영(커밋은 아님)
          
          if (updatedCount > 0) {
              val updatedEntity = entityManager.find(OrderJpaEntity::class.java, orderId)
              entityManager.refresh(updatedEntity) // 본 트랜잭션 내 DB 변경 사항을 영속성으로 동기화(본 트랜잭션이 커밋되기 전 다른 트랜잭션(@Transactional(propagation = REQUIRES_NEW))이 시작될 경우 해당 트랜잭션은 본 변경 사항이 반영되지 않은 채로 작동)
          }
        
          return updatedCount
      }
  }
  
  @Component
  class Consumer(val consumerState: ConsumerState) {
      @Transactional
      @KafkaListener(topics=["some"], groupId="some-group")
      fun consume(orderId: Long) {
          val affectedRows = consumerState.markAsPaidIfCreated(orderId)
          if (affectedRows != 1) {
              return // 이미 처리된 이벤트
          }
          
          // update가 수행된 트랜잭션만 logic 수행
          // 여기서 예외 발생 시 전체 롤백
          orderService.confirmOrder(orderId)
      }
  }
  ```

### 병렬 처리
- KafkaConsumerConfig에서 setConcurrency(n)으로 인스턴스 1개당 n개로 병렬처리 설정 가능
```kotlin
// ex. @Configuration 클래스 내 정의한 병렬처리 설정
@Bean
fun containerFactory(
  consumerFactory: ConsumerFactory<String, String>
): ConcurrentKafkaListenerContainerFactory<String, String> {
  return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
    this.consumerFactory = consumerFactory
    this.setConcurrency(3) // 3개의 Consumer가 같은 groupId로 병렬 처리
  }
}
```
  - Topic당 Partition 수는 Kafka에 별도 설정해야 함. 해당 Topic을 처리하는 Consumer 개수와 동일할 때 가장 효율적
  - 만약 동일한 Consumer 인스턴스를 Docker 등으로 여러 개 작동시킬 경우 Topic당 Consumer 수(setConcurrency(n)의 n개 * 인스턴스 수)만큼 병렬처리 됨. 즉 설정해야 할 Partition 수는 Consumer 수(n개 * 인스턴스 수) + 여분(Consumer 수의 10%~30% (Consumer 추가 시 즉시 병렬처리에 반영 가능하므로 여분을 추가))
- Publish는 여러 데이터를 1개씩 send하여 Consumer에서 병렬처리 함
```kotlin
// ex. 이메일 전송 병렬처리를 위한 Publish 방법
fun sendBulkEmails(emails: List<EmailData>) {
  emails.forEach { email: Any ->
    kafkaTemplate.send("email-send", email.userId, email)
  }
}
```
