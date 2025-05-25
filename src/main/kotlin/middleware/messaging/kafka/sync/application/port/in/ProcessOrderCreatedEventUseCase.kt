package middleware.messaging.kafka.sync.application.port.`in`

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent

interface ProcessOrderCreatedEventUseCase {
    fun process(event: OrderCreatedEvent)
}