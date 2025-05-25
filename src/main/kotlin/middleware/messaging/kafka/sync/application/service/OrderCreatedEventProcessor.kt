package middleware.messaging.kafka.sync.application.service

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.ProcessOrderCreatedEventUseCase
import org.springframework.stereotype.Service

@Service
class OrderCreatedEventProcessor : ProcessOrderCreatedEventUseCase {

    override fun process(event: OrderCreatedEvent) {
        println("decreaseStock productId: ${event.productId}, quantity: ${event.quantity}")
        println("updateStatus orderId: ${event.orderId}, status: CONFIRMED")
    }
}