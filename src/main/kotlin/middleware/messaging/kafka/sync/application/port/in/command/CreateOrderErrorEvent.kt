package middleware.messaging.kafka.sync.application.port.`in`.command

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import java.time.LocalDateTime

data class CreateOrderErrorEvent(
    val event: OrderCreatedEvent,
    val loggedAt: LocalDateTime = LocalDateTime.now(),
)