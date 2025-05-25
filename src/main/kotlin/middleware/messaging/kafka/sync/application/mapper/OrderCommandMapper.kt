package middleware.messaging.kafka.sync.application.mapper

import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderCommand
import middleware.messaging.kafka.sync.domain.model.Order
import middleware.messaging.kafka.sync.domain.vo.OrderId

object OrderCommandMapper {
    fun toDomain(command: CreateOrderCommand): Order {
        return Order(
            id = OrderId.generate(),
            userId = command.userId,
            productId = command.productId,
            quantity = command.quantity,
        )
    }
}