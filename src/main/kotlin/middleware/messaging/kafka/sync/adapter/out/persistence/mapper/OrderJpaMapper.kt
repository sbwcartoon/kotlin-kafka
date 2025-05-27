package middleware.messaging.kafka.sync.adapter.out.persistence.mapper

import middleware.messaging.kafka.sync.adapter.out.persistence.entity.OrderJpaEntity
import middleware.messaging.kafka.sync.domain.model.Order
import middleware.messaging.kafka.sync.domain.vo.OrderId
import middleware.messaging.kafka.sync.domain.vo.ProductId
import middleware.messaging.kafka.sync.domain.vo.Quantity
import middleware.messaging.kafka.sync.domain.vo.UserId
import java.util.*

object OrderJpaMapper {
    fun toEntity(order: Order): OrderJpaEntity {
        return OrderJpaEntity(
            id = order.id.value.toString(),
            userId = order.userId.value.toString(),
            productId = order.productId.value.toString(),
            quantity = order.quantity.value,
        )
    }

    fun toDomain(entity: OrderJpaEntity): Order {
        return Order(
            id = OrderId(UUID.fromString(entity.id)),
            userId = UserId(UUID.fromString(entity.userId)),
            productId = ProductId(UUID.fromString(entity.productId)),
            quantity = Quantity(entity.quantity),
        )
    }
}