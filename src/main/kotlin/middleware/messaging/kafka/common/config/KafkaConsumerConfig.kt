package middleware.messaging.kafka.common.config

import middleware.messaging.kafka.common.exception.DltPublishException
import middleware.messaging.kafka.common.fallback.KafkaFallbackLoggingCommand
import middleware.messaging.kafka.common.fallback.KafkaFallbackLoggingUseCase
import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConsumerConfig(
    @Value("\${kafka.consumer.retry.max-attempts}") val maxAttempts: Long,
    @Value("\${kafka.consumer.retry.backoff-interval-ms}") val interval: Long,
    private val kafkaFallbackLoggingUseCase: KafkaFallbackLoggingUseCase,
) {

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        kafkaTemplate: KafkaTemplate<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate))
        return factory
    }

    @Bean
    fun kafkaListenerContainerFactoryForOrderCreatedEvent(
        consumerFactory: ConsumerFactory<String, OrderCreatedEvent>,
        kafkaTemplate: KafkaTemplate<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>()
        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate))
        return factory
    }

    fun defaultErrorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, consumerException ->
            val topicPartition = try {
                TopicPartition(record.topic() + ".DLT", record.partition())
            } catch (dltException: RuntimeException) {
                kafkaFallbackLoggingUseCase.save(KafkaFallbackLoggingCommand(record, consumerException, dltException))
                throw DltPublishException(dltException)
            }

            topicPartition
        }
        val backoff = FixedBackOff(interval, maxAttempts)
        val errorHandler = DefaultErrorHandler(recoverer, backoff)
        errorHandler.addNotRetryableExceptions(DltPublishException::class.java)

        return errorHandler
    }
}