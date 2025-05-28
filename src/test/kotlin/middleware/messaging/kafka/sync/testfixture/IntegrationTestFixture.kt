package middleware.messaging.kafka.integration.testfixture

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderCommand
import middleware.messaging.kafka.sync.domain.vo.OrderId
import middleware.messaging.kafka.sync.domain.vo.ProductId
import middleware.messaging.kafka.sync.domain.vo.Quantity
import middleware.messaging.kafka.sync.domain.vo.UserId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class IntegrationTestFixture {
    fun generateOrderCreatedEvent(): OrderCreatedEvent {
        return OrderCreatedEvent(
            orderId = OrderId.generate(),
            userId = UserId.generate(),
            productId = ProductId.generate(),
            quantity = Quantity(2),
            createdAt = LocalDateTime.now(),
        )
    }

    fun generateCreateOrderCommand(): CreateOrderCommand {
        return CreateOrderCommand(
            userId = UserId.generate(),
            productId = ProductId.generate(),
            quantity = Quantity(2),
        )
    }
}