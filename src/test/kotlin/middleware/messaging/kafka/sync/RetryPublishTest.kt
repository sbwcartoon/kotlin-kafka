package middleware.messaging.kafka.sync

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.verify
import middleware.messaging.kafka.common.config.RetryableConfig
import middleware.messaging.kafka.sync.config.EmbeddedKafkaForTest
import middleware.messaging.kafka.sync.testfixture.IntegrationTestFixture
import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.adapter.out.kafka.exception.OrderEventPublishErrorException
import middleware.messaging.kafka.sync.application.port.`in`.CreateOrderUseCase
import middleware.messaging.kafka.sync.application.port.out.OrderRepository
import org.apache.kafka.common.KafkaException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate

@Import(RetryableConfig::class)
@EmbeddedKafkaForTest
class RetryPublishTest(
    @Autowired private val useCase: CreateOrderUseCase,
    @MockkBean private val orderRepository: OrderRepository,
    @MockkBean(relaxed = true) private val kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
    @Autowired private val testFixture: IntegrationTestFixture,
) : BehaviorSpec({

    Given("OrderCreatedEvent publish에 오류가 있을 때") {
        val command = testFixture.generateCreateOrderCommand()

        every { orderRepository.save(any()) } answers { firstArg() }
        every { kafkaTemplate.send(any(), any(), any()).get() } throws KafkaException("fail")

        When("publish하면") {
            Then("최대 3회 publish를 시도하고, OrderEventPublishErrorException가 발생한다") {
                shouldThrow<OrderEventPublishErrorException> {
                    useCase.createOrder(command)
                }

                verify(exactly = 3) {
                    kafkaTemplate.send(any(), any(), any()).get()
                }
            }
        }
    }
})