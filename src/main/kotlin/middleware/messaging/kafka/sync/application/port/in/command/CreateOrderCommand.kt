package middleware.messaging.kafka.sync.application.port.`in`.command

import middleware.messaging.kafka.sync.domain.vo.ProductId
import middleware.messaging.kafka.sync.domain.vo.Quantity
import middleware.messaging.kafka.sync.domain.vo.UserId

data class CreateOrderCommand(
    val userId: UserId,
    val productId: ProductId,
    val quantity: Quantity,
)