package middleware.messaging.kafka.sync.application.service

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.mapper.OrderCommandMapper
import middleware.messaging.kafka.sync.application.port.`in`.CreateOrderUseCase
import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderCommand
import middleware.messaging.kafka.sync.application.port.out.OrderEventPublisherPort
import middleware.messaging.kafka.sync.application.port.out.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateOrderService(
    private val orderEventPublisherPort: OrderEventPublisherPort,
    private val orderRepository: OrderRepository,
) : CreateOrderUseCase {

    @Transactional
    override fun createOrder(command: CreateOrderCommand) {
        val order = OrderCommandMapper.toDomain(command)
        orderRepository.save(order)

        val event = OrderCreatedEvent.from(order)
        orderEventPublisherPort.publish(event)
    }
}