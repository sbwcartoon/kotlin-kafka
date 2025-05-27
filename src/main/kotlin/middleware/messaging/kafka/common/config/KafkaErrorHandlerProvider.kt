package middleware.messaging.kafka.common.config

import middleware.messaging.kafka.common.exception.DltPublishException
import middleware.messaging.kafka.common.fallback.KafkaFallbackLoggingCommand
import middleware.messaging.kafka.common.fallback.KafkaFallbackLoggingUseCase
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.stereotype.Component
import org.springframework.util.backoff.FixedBackOff

@Component
class KafkaErrorHandlerProvider(
    @Value("\${kafka.consumer.retry.max-attempts}") val maxAttempts: Long,
    @Value("\${kafka.consumer.retry.backoff-interval-ms}") val interval: Long,
    private val kafkaFallbackLoggingUseCase: KafkaFallbackLoggingUseCase,
) {
    fun createDefaultErrorHandler(kafkaTemplate: KafkaTemplate<Any, Any>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, e ->
            if (record.topic().endsWith(".DLT")) {
                kafkaFallbackLoggingUseCase.save(KafkaFallbackLoggingCommand(record, e))
                throw DltPublishException(e)
            }

            TopicPartition("${record.topic()}.DLT", record.partition())
        }
        val backoff = FixedBackOff(interval, maxAttempts)
        val errorHandler = DefaultErrorHandler(recoverer, backoff)
        errorHandler.addNotRetryableExceptions(DltPublishException::class.java)

        return errorHandler
    }
}