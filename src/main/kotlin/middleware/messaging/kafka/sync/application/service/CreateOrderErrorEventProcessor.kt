package middleware.messaging.kafka.sync.application.service

import middleware.messaging.kafka.sync.application.port.`in`.ProcessCreateOrderErrorEventUseCase
import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderErrorEvent
import org.springframework.stereotype.Service

@Service
class CreateOrderErrorEventProcessor : ProcessCreateOrderErrorEventUseCase {

    override fun process(event: CreateOrderErrorEvent) {
        println(event)
    }
}