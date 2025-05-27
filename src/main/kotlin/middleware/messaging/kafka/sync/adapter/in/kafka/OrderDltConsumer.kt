package middleware.messaging.kafka.sync.adapter.`in`.kafka

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.application.port.`in`.ProcessCreateOrderErrorEventUseCase
import middleware.messaging.kafka.sync.application.port.`in`.command.CreateOrderErrorEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderDltConsumer(
    private val processCreateOrderErrorEventUseCase: ProcessCreateOrderErrorEventUseCase
) {

    @KafkaListener(
        topics = ["\${kafka.topic.order-created}.DLT"],
        groupId = "\${spring.kafka.consumer.dlt-group-id}",
    )
    fun consume(record: ConsumerRecord<String, OrderCreatedEvent>) {
        val event = CreateOrderErrorEvent(event = record.value())
        processCreateOrderErrorEventUseCase.process(event)
    }
}