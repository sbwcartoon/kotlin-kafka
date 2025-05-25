package middleware.messaging.kafka.integration

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import middleware.messaging.kafka.integration.config.EmbeddedKafkaForTest
import middleware.messaging.kafka.integration.testfixture.IntegrationTestFixture
import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.ProcessOrderCreatedEventUseCase
import middleware.messaging.kafka.sync.application.port.out.OrderEventPublisherPort
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds

@EmbeddedKafkaForTest
class EventFlowTest(
    @Autowired private val orderEventPublisher: OrderEventPublisherPort,
    @Autowired private val testFixture: IntegrationTestFixture,
    @MockkBean private val useCase: ProcessOrderCreatedEventUseCase,
) : BehaviorSpec({

    Given("OrderCreatedEvent가 있을 때") {
        val event = testFixture.generateOrderCreatedEvent()

        When("publish하면") {
            val receivedEvents = ConcurrentLinkedQueue<OrderCreatedEvent>()
            every { useCase.process(any()) } answers {
                receivedEvents.add(firstArg<OrderCreatedEvent>())
                Unit
            }

            orderEventPublisher.publish(event)

            Then("consumer는 해당 event를 1회 consume 한다") {
                eventually(duration = 5.seconds) {
                    receivedEvents.size shouldBe 1
                    receivedEvents.contains(event) shouldBe true
                }
            }
        }
    }
})