package middleware.messaging.kafka.sync.application.port.out

import middleware.messaging.kafka.sync.domain.model.Order
import middleware.messaging.kafka.sync.domain.vo.OrderId

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(orderId: OrderId): Order?
}