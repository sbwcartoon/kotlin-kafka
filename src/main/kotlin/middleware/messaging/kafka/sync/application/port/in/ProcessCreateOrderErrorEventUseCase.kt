package middleware.messaging.kafka.sync.application.port.`in`

import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderErrorEvent

interface ProcessCreateOrderErrorEventUseCase {
    fun process(event: CreateOrderErrorEvent)
}