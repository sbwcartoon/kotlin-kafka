package middleware.messaging.kafka.sync.adapter.out.persistence

import middleware.messaging.kafka.sync.adapter.out.persistence.mapper.OrderJpaMapper
import middleware.messaging.kafka.sync.adapter.out.persistence.repository.OrderJpaRepository
import middleware.messaging.kafka.sync.application.port.out.OrderRepository
import middleware.messaging.kafka.sync.domain.model.Order
import middleware.messaging.kafka.sync.domain.vo.OrderId
import org.springframework.stereotype.Component

@Component
class OrderPersistenceAdapter(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = OrderJpaMapper.toEntity(order)
        val saved = orderJpaRepository.save(entity)
        return OrderJpaMapper.toDomain(saved)
    }

    override fun findById(orderId: OrderId): Order? {
        return orderJpaRepository.findById(orderId.value.toString())
            .map { OrderJpaMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findAll(): List<Order> {
        return orderJpaRepository.findAll()
            .map { OrderJpaMapper.toDomain(it) }
    }
}