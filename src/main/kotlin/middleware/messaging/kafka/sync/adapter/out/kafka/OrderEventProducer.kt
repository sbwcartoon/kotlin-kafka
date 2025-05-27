package middleware.messaging.kafka.sync.adapter.out.kafka

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import middleware.messaging.kafka.sync.adapter.out.kafka.exception.OrderEventPublishErrorException
import middleware.messaging.kafka.sync.application.port.out.OrderEventPublisherPort
import org.apache.kafka.common.KafkaException
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class OrderEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, OrderCreatedEvent>,
    @Value("\${kafka.topic.order-created}") private val topic: String,
) : OrderEventPublisherPort {

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(delay = 1000),
        include = [KafkaException::class],
    )
    override fun publish(event: OrderCreatedEvent) {
        kafkaTemplate.send(
            topic,
            event.orderId.toString(),
            event,
        ).get()
    }

    @Recover
    fun recover(e: KafkaException, event: OrderCreatedEvent) {
        throw OrderEventPublishErrorException(e, event)
    }
}