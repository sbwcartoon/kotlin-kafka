package middleware.messaging.kafka.integration

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import middleware.messaging.kafka.integration.config.EmbeddedKafkaForTest
import middleware.messaging.kafka.integration.testfixture.IntegrationTestFixture
import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.CreateOrderUseCase
import middleware.messaging.kafka.sync.application.port.out.OrderRepository
import org.apache.kafka.common.KafkaException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate

@EmbeddedKafkaForTest
class PublishTransactionTest(
    @Autowired private val useCase: CreateOrderUseCase,
    @Autowired private val orderRepository: OrderRepository,
    @MockkBean(relaxed = true) private val kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
    @Autowired private val testFixture: IntegrationTestFixture,
) : BehaviorSpec({

    Given("OrderCreatedEvent publish에 오류가 있을 때") {
        every { kafkaTemplate.send(any(), any(), any()).get() } throws KafkaException("fail")

        When("publish하면 오류가 발생하고") {
            val existingOrders = orderRepository.findAll()

            shouldThrow<KafkaException> {
                useCase.createOrder(testFixture.generateCreateOrderCommand())
            }

            Then("orderRepository에 변경사항은 없다") {
                val newOrders = orderRepository.findAll()
                existingOrders shouldContainExactly newOrders
            }
        }
    }
})