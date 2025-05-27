package middleware.messaging.kafka.integration

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import middleware.messaging.kafka.common.fallback.KafkaFallbackLoggingUseCase
import middleware.messaging.kafka.integration.config.EmbeddedKafkaForTest
import middleware.messaging.kafka.integration.testfixture.IntegrationTestFixture
import middleware.messaging.kafka.sync.application.port.`in`.ProcessCreateOrderErrorEventUseCase
import middleware.messaging.kafka.sync.application.port.`in`.ProcessOrderCreatedEventUseCase
import middleware.messaging.kafka.sync.application.port.out.OrderEventPublisherPort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import kotlin.time.Duration.Companion.seconds

@EmbeddedKafkaForTest
class RetryConsumeTest(
    @Autowired private val orderEventPublisher: OrderEventPublisherPort,
    @Autowired private val testFixture: IntegrationTestFixture,
    @MockkBean private val useCase: ProcessOrderCreatedEventUseCase,
    @MockkBean private val errorUseCase: ProcessCreateOrderErrorEventUseCase,
    @MockkBean private val fallbackUseCase: KafkaFallbackLoggingUseCase,
    @Value("\${kafka.consumer.retry.max-attempts}") private val maxAttempts: Int,
) : BehaviorSpec({

    Given("OrderCreatedEvent consumer에 오류가 있을 때") {
        every { useCase.process(any()) } throws RuntimeException("fail")
        val event = testFixture.generateOrderCreatedEvent()

        When("publish하면") {
            orderEventPublisher.publish(event)

            then("최대 (설정된 retry 수 + 1회)까지 consume을 재시도한다") {
                eventually(5.seconds) {
                    verify(exactly = maxAttempts + 1) {
                        useCase.process(any())
                    }
                }
            }

            then("consume이 모두 실패하면 dlt를 발행한다") {
                every { errorUseCase.process(any()) } just Runs

                eventually(5.seconds) {
                    verify(exactly = 1) {
                        errorUseCase.process(any())
                    }
                }
            }
        }

        When("dlt consumer에도 오류가 있을 때 publish하면") {
            every { errorUseCase.process(any()) } throws RuntimeException("fail")
            orderEventPublisher.publish(event)

            then("fallback이 작동한다") {
                every { fallbackUseCase.save(any()) } just Runs

                eventually(10.seconds) {
                    verify(exactly = 1) {
                        fallbackUseCase.save(any())
                    }
                }
            }
        }
    }
})