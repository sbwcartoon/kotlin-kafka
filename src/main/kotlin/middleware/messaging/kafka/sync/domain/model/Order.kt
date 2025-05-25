package middleware.messaging.kafka.sync.domain.model

import middleware.messaging.kafka.sync.domain.vo.OrderId
import middleware.messaging.kafka.sync.domain.vo.ProductId
import middleware.messaging.kafka.sync.domain.vo.Quantity
import middleware.messaging.kafka.sync.domain.vo.UserId

data class Order(
    val id: OrderId,
    val userId: UserId,
    val productId: ProductId,
    val quantity: Quantity,
)