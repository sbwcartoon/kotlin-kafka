package middleware.messaging.kafka.integration

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.verify
import middleware.messaging.kafka.integration.config.EmbeddedKafkaForTest
import middleware.messaging.kafka.integration.testfixture.IntegrationTestFixture
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
    @Value("\${kafka.consumer.retry.max-attempts}") private val maxAttempts: Int,
) : BehaviorSpec({

    Given("OrderCreatedEvent를 publish할 때") {
        val event = testFixture.generateOrderCreatedEvent()

        When("consumer에서 오류가 발생하면") {
            every { useCase.process(any()) } throws RuntimeException("fail")

            orderEventPublisher.publish(event)

            then("최소 2회 ~ 최대 (설정된 retry 수 + 1회)까지 consume을 재시도한다") {
                eventually(5.seconds) {
                    verify(atLeast = 2, atMost = maxAttempts + 1) { useCase.process(any()) }
                }
            }
        }
    }
})