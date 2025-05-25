package middleware.messaging.kafka.sync.adapter.`in`.kafka

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.ProcessOrderCreatedEventUseCase
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(private val processOrderCreatedEventUseCase: ProcessOrderCreatedEventUseCase) {

    @KafkaListener(
        topics = ["\${kafka.topic.order-created}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactoryForOrderCreatedEvent",
    )
    fun consume(event: OrderCreatedEvent) {
        processOrderCreatedEventUseCase.process(event)
    }
}