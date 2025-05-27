package middleware.messaging.kafka.sync.application.port.out

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent

interface OrderEventPublisherPort {
    fun publish(event: OrderCreatedEvent)
}