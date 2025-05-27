package middleware.messaging.kafka.sync.adapter.out.kafka.event

import middleware.messaging.kafka.sync.domain.model.Order
import middleware.messaging.kafka.sync.domain.vo.OrderId
import middleware.messaging.kafka.sync.domain.vo.ProductId
import middleware.messaging.kafka.sync.domain.vo.Quantity
import middleware.messaging.kafka.sync.domain.vo.UserId
import java.time.LocalDateTime

data class OrderCreatedEvent(
    val orderId: OrderId,
    val userId: UserId,
    val productId: ProductId,
    val quantity: Quantity,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(order: Order): OrderCreatedEvent {
            return OrderCreatedEvent(
                orderId = order.id,
                userId = order.userId,
                productId = order.productId,
                quantity = order.quantity,
                createdAt = LocalDateTime.now(),
            )
        }
    }
}