package middleware.messaging.kafka.sync.application.port.`in`

import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderCommand

interface CreateOrderUseCase {
    fun createOrder(command: CreateOrderCommand)
}